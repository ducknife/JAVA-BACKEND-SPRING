package com.ducknife.project.modules.orderdetail.dto;

import java.math.BigDecimal;

import com.ducknife.project.modules.order.dto.OrderResponse;
import com.ducknife.project.modules.orderdetail.OrderDetail;
import com.ducknife.project.modules.product.dto.ProductResponse;

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
public class OrderDetailResponse {
    private Long id;
    private ProductResponse product;
    private Long quantity;
    private BigDecimal price;

    public static OrderDetailResponse from(OrderDetail orderDetail) {
        return OrderDetailResponse.builder()
                .id(orderDetail.getId())
                .product(ProductResponse.from(orderDetail.getProduct()))
                .quantity(orderDetail.getQuantity())
                .price(orderDetail.getPrice())
                .build();
    }
}
