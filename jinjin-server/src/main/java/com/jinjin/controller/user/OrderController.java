package com.jinjin.controller.user;

import com.jinjin.context.BaseContext;
import com.jinjin.dto.OrdersPageQueryDTO;
import com.jinjin.dto.OrdersPaymentDTO;
import com.jinjin.dto.OrdersSubmitDTO;
import com.jinjin.result.PageResult;
import com.jinjin.result.Result;
import com.jinjin.service.OrderService;
import com.jinjin.vo.OrderPaymentVO;
import com.jinjin.vo.OrderSubmitVO;
import com.jinjin.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Tag(name = "User order related interfaces")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @Operation(summary = "Submit order")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        return Result.success(orderService.submitOrder(ordersSubmitDTO));
    }

    @PutMapping("/payment")
    @Operation(summary = "Pay order with stub flow")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        return Result.success(orderService.payment(ordersPaymentDTO));
    }

    @GetMapping("/historyOrders")
    @Operation(summary = "List user history orders")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        return Result.success(orderService.pageQuery(ordersPageQueryDTO));
    }

    @GetMapping("/orderDetail/{id}")
    @Operation(summary = "Get order detail")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        return Result.success(orderService.details(id));
    }

    @PutMapping("/cancel/{id}")
    @Operation(summary = "Cancel order")
    public Result<String> cancel(@PathVariable Long id) {
        orderService.userCancelById(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @Operation(summary = "Re-order from existing order")
    public Result<String> repetition(@PathVariable Long id) {
        orderService.repetition(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @Operation(summary = "Send order reminder")
    public Result<String> reminder(@PathVariable Long id) {
        orderService.reminder(id);
        return Result.success();
    }
}
