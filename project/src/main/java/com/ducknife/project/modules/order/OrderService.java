package com.ducknife.project.modules.order;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.user.User;
import com.ducknife.project.modules.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public List<OrderDTO> getOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderDTO::from) // không còn bị N + 1 vì đã JOIN FETCH 
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));
        return OrderDTO.from(order);
    }

    public Long countOrders() {
        return orderRepository.count();
    }

    public Boolean OrderExistedById(Long id) {
        return orderRepository.existsById(id);
    }

    public OrderDTO add(OrderDTO order) {
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
        Order savedOrder = orderRepository.save(Order.from(order, user));
        return OrderDTO.from(savedOrder);
    }
}
