package com.ducknife.project.modules.order.dto;

import java.util.List;

import com.ducknife.project.modules.orderdetail.dto.OrderDetailRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class OrderRequest {
    @NotNull(message = "Id người dùng không được bỏ trống")
    private Long userId;
    private List<OrderDetailRequest> orderDetails;
}
