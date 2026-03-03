package com.ducknife.project.modules.invoice.dto;

import java.math.BigDecimal;

import com.ducknife.project.modules.invoice.Invoice;
import com.ducknife.project.modules.order.dto.OrderResponse;

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
public class InvoiceResponse {
    private Long id;
    private OrderResponse order;
    private BigDecimal totalPrice;

    public static InvoiceResponse from(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .order(OrderResponse.from(invoice.getOrder()))
                .totalPrice(invoice.getTotalPrice())
                .build();
    }
}
