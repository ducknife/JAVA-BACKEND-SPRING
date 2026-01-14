package com.ducknife.project.modules.order;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrders() {
        return ApiResponse.ok(orderService.getOrders());
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countOrders() {
        return ApiResponse.ok(orderService.countOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> addOrder(@RequestBody OrderDTO order) {
        OrderDTO savedOrder = orderService.add(order);
        return ApiResponse.created(savedOrder);
    }

}
