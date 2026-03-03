package com.ducknife.project.modules.invoice;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;
import com.ducknife.project.modules.invoice.dto.InvoiceResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices() {
        return ApiResponse.ok(invoiceService.getInvoices());
    }

    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByOrderId(
        @PathVariable Long orderId
    ) {
        return ApiResponse.ok(invoiceService.getInvoiceByOrderId(orderId));
    }
    
}
