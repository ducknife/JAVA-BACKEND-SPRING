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
import com.ducknife.project.modules.order.dto.OrderRequest;
import com.ducknife.project.modules.order.dto.OrderResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders() {
        return ApiResponse.ok(orderService.getOrders());
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countOrders() {
        return ApiResponse.ok(orderService.countOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> addOrder(@RequestBody @Valid OrderRequest order) {
        OrderResponse savedOrder = orderService.add(order);
        return ApiResponse.created(savedOrder);
    }

}
