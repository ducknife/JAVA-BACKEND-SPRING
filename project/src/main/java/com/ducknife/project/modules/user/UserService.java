package com.ducknife.project.modules.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.order.Order;
import com.ducknife.project.modules.order.OrderDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

        private final UserRepository userRepository;

        public List<UserDTO> getUsers() {
                return userRepository.findAll()
                                .stream()
                                .map(u -> UserDTO.builder()
                                                .fullName(u.getFullName())
                                                .userName(u.getUserName())
                                                .build())
                                .collect(Collectors.toList());
        }

        public List<OrderDTO> findOrdersById(Long id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                List<Order> orders = userRepository.findOrdersById(id);
                return orders.stream()
                                .map(o -> OrderDTO.builder()
                                                .id(o.getId())
                                                .userId(user.getId())
                                                .build())
                                .collect(Collectors.toList());
        }
}
