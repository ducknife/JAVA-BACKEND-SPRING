package com.ducknife.project.modules.orderdetail;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;
import com.ducknife.project.modules.orderdetail.dto.OrderDetailResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order-details")
@RequiredArgsConstructor
public class OrderDetailController {
    private final OrderDetailService orderDetailService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDetailResponse>>> getOrderDetails() {
        return ApiResponse.ok(orderDetailService.getOrderDetails());
    }
}
