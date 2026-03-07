package com.jinjin.controller.admin;

import com.jinjin.dto.OrdersCancelDTO;
import com.jinjin.dto.OrdersConfirmDTO;
import com.jinjin.dto.OrdersPageQueryDTO;
import com.jinjin.dto.OrdersRejectionDTO;
import com.jinjin.result.PageResult;
import com.jinjin.result.Result;
import com.jinjin.service.OrderService;
import com.jinjin.vo.OrderStatisticsVO;
import com.jinjin.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Tag(name = "Admin order related interfaces")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @Operation(summary = "Search orders with conditions")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        return Result.success(orderService.pageQuery(ordersPageQueryDTO));
    }

    @GetMapping("/details/{id}")
    @Operation(summary = "Get order details")
    public Result<OrderVO> details(@PathVariable Long id) {
        return Result.success(orderService.details(id));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get order statistics")
    public Result<OrderStatisticsVO> statistics() {
        return Result.success(orderService.statistics());
    }

    @PutMapping("/confirm")
    @Operation(summary = "Confirm order")
    public Result<String> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/rejection")
    @Operation(summary = "Reject order")
    public Result<String> rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @Operation(summary = "Cancel order")
    public Result<String> cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @Operation(summary = "Mark order as out for delivery")
    public Result<String> delivery(@PathVariable Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @Operation(summary = "Complete order")
    public Result<String> complete(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }
}
