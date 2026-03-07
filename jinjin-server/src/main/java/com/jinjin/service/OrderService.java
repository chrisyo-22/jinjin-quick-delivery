package com.jinjin.service;

import com.jinjin.dto.*;
import com.jinjin.result.PageResult;
import com.jinjin.vo.OrderPaymentVO;
import com.jinjin.vo.OrderStatisticsVO;
import com.jinjin.vo.OrderSubmitVO;
import com.jinjin.vo.OrderVO;

public interface OrderService {

    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO);

    void paySuccess(String outTradeNo);

    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO details(Long id);

    void userCancelById(Long id);

    void repetition(Long id);

    void reminder(Long id);

    OrderStatisticsVO statistics();

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    void cancel(OrdersCancelDTO ordersCancelDTO);

    void delivery(Long id);

    void complete(Long id);
}
