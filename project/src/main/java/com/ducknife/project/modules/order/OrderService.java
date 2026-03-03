package com.ducknife.project.modules.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.invoice.Invoice;
import com.ducknife.project.modules.invoice.InvoiceRepository;
import com.ducknife.project.modules.order.dto.OrderRequest;
import com.ducknife.project.modules.order.dto.OrderResponse;
import com.ducknife.project.modules.orderdetail.OrderDetail;
import com.ducknife.project.modules.product.Product;
import com.ducknife.project.modules.product.ProductRepository;
import com.ducknife.project.modules.user.User;
import com.ducknife.project.modules.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<OrderResponse> getOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderResponse::from) // không còn bị N + 1 vì đã JOIN FETCH
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));
        return OrderResponse.from(order);
    }

    public Long countOrders() {
        return orderRepository.count();
    }

    public Boolean OrderExistedById(Long id) {
        return orderRepository.existsById(id);
    }

    @Transactional
    public OrderResponse add(OrderRequest orderRequest) {
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
        Order order = Order.from(orderRequest, user);
        List<OrderDetail> orderDetails = orderRequest.getOrderDetails().stream()
                .map(od -> {
                    Product product = productRepository.findById(od.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại!"));
                    return OrderDetail.from(od, product, order);
                })
                .collect(Collectors.toList());
        BigDecimal totalPrice = orderDetails.stream()
                .map(od -> od.getPrice().multiply(BigDecimal.valueOf(od.getQuantity())))
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        Invoice invoice = Invoice.builder()
                .order(order)
                .totalPrice(totalPrice)
                .build();
        order.setOrderDetails(orderDetails);
        order.setInvoice(invoice);
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }
}
