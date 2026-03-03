package com.ducknife.project.modules.order.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.ducknife.project.modules.order.Order;
import com.ducknife.project.modules.orderdetail.dto.OrderDetailResponse;
import com.ducknife.project.modules.user.dto.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class OrderResponse {
    private Long id;
    private UserResponse user;
    private List<OrderDetailResponse> orderDetails;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .user(UserResponse.from(order.getUser()))
                .orderDetails(
                        order.getOrderDetails()
                                .stream()
                                .map(OrderDetailResponse::from)
                                .collect(Collectors.toList()))
                .build();
    }
}
