package com.jinjin.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jinjin.constant.MessageConstant;
import com.jinjin.context.BaseContext;
import com.jinjin.dto.*;
import com.jinjin.entity.AddressBook;
import com.jinjin.entity.OrderDetail;
import com.jinjin.entity.Orders;
import com.jinjin.entity.ShoppingCart;
import com.jinjin.exception.AddressBookBusinessException;
import com.jinjin.exception.OrderBusinessException;
import com.jinjin.exception.ShoppingCartBusinessException;
import com.jinjin.mapper.AddressBookMapper;
import com.jinjin.mapper.OrderDetailMapper;
import com.jinjin.mapper.OrderMapper;
import com.jinjin.mapper.ShoppingCartMapper;
import com.jinjin.result.PageResult;
import com.jinjin.service.OrderService;
import com.jinjin.vo.OrderPaymentVO;
import com.jinjin.vo.OrderStatisticsVO;
import com.jinjin.vo.OrderSubmitVO;
import com.jinjin.vo.OrderVO;
import com.jinjin.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        validateDeliveryRange(addressBook);

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCartQuery = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCartQuery);
        if (shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setUserId(userId);
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now());
        order.setPhone(addressBook.getPhone());
        order.setAddress(buildAddress(addressBook));
        order.setConsignee(addressBook.getConsignee());
        orderMapper.insert(order);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        shoppingCartMapper.deleteByUserId(userId);

        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
    }

    @Override
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        Orders order = requireOrderOwnedByCurrentUser(ordersPaymentDTO.getOrderNumber());
        if (!Orders.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        paySuccess(order.getNumber());

        return OrderPaymentVO.builder()
                .nonceStr("stub-nonce")
                .paySign("stub-sign")
                .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                .signType("STUB")
                .packageStr("stub-package")
                .build();
    }

    @Override
    public void paySuccess(String outTradeNo) {
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "Order number: " + outTradeNo);
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
        log.info("Stub payment marked order {} as paid", outTradeNo);
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = page.getResult().stream()
                .map(this::buildOrderVO)
                .toList();
        return new PageResult(page.getTotal(), orderVOList);
    }

    @Override
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return buildOrderVO(orders);
    }

    @Override
    @Transactional
    public void userCancelById(Long id) {
        Orders order = orderMapper.getById(id);
        validateUserOrderAccess(order);
        if (Orders.COMPLETED.equals(order.getStatus()) || Orders.CANCELLED.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders update = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelReason("User cancelled order")
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.PAID.equals(order.getPayStatus()) ? Orders.REFUND : order.getPayStatus())
                .build();
        orderMapper.update(update);
    }

    @Override
    @Transactional
    public void repetition(Long id) {
        Orders order = orderMapper.getById(id);
        validateUserOrderAccess(order);

        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        Long userId = BaseContext.getCurrentId();
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart query = ShoppingCart.builder()
                    .userId(userId)
                    .dishId(orderDetail.getDishId())
                    .setmealId(orderDetail.getSetmealId())
                    .dishFlavor(orderDetail.getDishFlavor())
                    .build();
            List<ShoppingCart> existing = shoppingCartMapper.list(query);
            if (existing.isEmpty()) {
                ShoppingCart shoppingCart = ShoppingCart.builder()
                        .name(orderDetail.getName())
                        .userId(userId)
                        .dishId(orderDetail.getDishId())
                        .setmealId(orderDetail.getSetmealId())
                        .dishFlavor(orderDetail.getDishFlavor())
                        .number(orderDetail.getNumber())
                        .amount(orderDetail.getAmount())
                        .image(orderDetail.getImage())
                        .createTime(LocalDateTime.now())
                        .build();
                shoppingCartMapper.insert(shoppingCart);
                continue;
            }

            ShoppingCart cart = existing.getFirst();
            cart.setNumber(cart.getNumber() + orderDetail.getNumber());
            shoppingCartMapper.updateNumberById(cart);
        }
    }

    @Override
    public void reminder(Long id) {
        Orders order = orderMapper.getById(id);
        validateUserOrderAccess(order);
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "Order number: " + order.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
        log.info("Reminder requested for order {}", id);
    }

    @Override
    public OrderStatisticsVO statistics() {
        LocalDateTime begin = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Map<String, Object> map = new HashMap<>();
        map.put("begin", begin);

        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer toBeConfirmed = orderMapper.countByMap(map);
        map.put("status", Orders.CONFIRMED);
        Integer confirmed = orderMapper.countByMap(map);
        map.put("status", Orders.DELIVERY_IN_PROGRESS);
        Integer deliveryInProgress = orderMapper.countByMap(map);

        OrderStatisticsVO vo = new OrderStatisticsVO();
        vo.setToBeConfirmed(toBeConfirmed);
        vo.setConfirmed(confirmed);
        vo.setDeliveryInProgress(deliveryInProgress);
        return vo;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders order = requireOrder(ordersConfirmDTO.getId());
        if (!Orders.TO_BE_CONFIRMED.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.update(Orders.builder().id(order.getId()).status(Orders.CONFIRMED).build());
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders order = requireOrder(ordersRejectionDTO.getId());
        if (!Orders.TO_BE_CONFIRMED.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.update(Orders.builder()
                .id(order.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelReason("Rejected by merchant")
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.PAID.equals(order.getPayStatus()) ? Orders.REFUND : order.getPayStatus())
                .build());
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders order = requireOrder(ordersCancelDTO.getId());
        if (Orders.COMPLETED.equals(order.getStatus()) || Orders.CANCELLED.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.update(Orders.builder()
                .id(order.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.PAID.equals(order.getPayStatus()) ? Orders.REFUND : order.getPayStatus())
                .build());
    }

    @Override
    public void delivery(Long id) {
        Orders order = requireOrder(id);
        if (!Orders.CONFIRMED.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.update(Orders.builder().id(id).status(Orders.DELIVERY_IN_PROGRESS).build());
    }

    @Override
    public void complete(Long id) {
        Orders order = requireOrder(id);
        if (!Orders.DELIVERY_IN_PROGRESS.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.update(Orders.builder().id(id).status(Orders.COMPLETED).deliveryTime(LocalDateTime.now()).build());
    }

    private void validateDeliveryRange(AddressBook addressBook) {
        log.info("Skipping real delivery range validation for address {}", addressBook.getId());
    }

    private String buildAddress(AddressBook addressBook) {
        return String.join("",
                safe(addressBook.getProvinceName()),
                safe(addressBook.getCityName()),
                safe(addressBook.getDistrictName()),
                safe(addressBook.getDetail()));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private Orders requireOrderOwnedByCurrentUser(String orderNumber) {
        Orders order = orderMapper.getByNumberAndUserId(orderNumber, BaseContext.getCurrentId());
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return order;
    }

    private Orders requireOrder(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return order;
    }

    private void validateUserOrderAccess(Orders order) {
        if (order == null || !BaseContext.getCurrentId().equals(order.getUserId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
    }

    private OrderVO buildOrderVO(Orders orders) {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        orderVO.setOrderDishes(orderDetails.stream()
                .map(detail -> detail.getName() + "*" + detail.getNumber())
                .collect(Collectors.joining("; ")));
        return orderVO;
    }
}
