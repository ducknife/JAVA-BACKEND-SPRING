package com.ducknife.project.modules.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ducknife.project.common.exception.ResourceConflictException;
import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.order.Order;
import com.ducknife.project.modules.order.OrderDTO;
import com.ducknife.project.modules.order.OrderRepository;
import com.ducknife.project.modules.user.dto.UserRequest;
import com.ducknife.project.modules.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

        private final UserRepository userRepository;
        private final OrderRepository orderRepository;

        public List<UserResponse> getUsers() {
                return userRepository.findByUserNameLengthOrderByFullNameDesc(8L)
                                .stream()
                                .map(UserResponse::from)
                                .collect(Collectors.toList());
        }

        public UserResponse getUserById(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                return UserResponse.from(user);
        }

        public List<OrderDTO> findOrdersById(Long userId) {
                if (!userRepository.existsById(userId)) {
                        throw new ResourceNotFoundException("Không tìm thấy người dùng");
                }
                List<Order> orders = orderRepository.findByUserId(userId);
                return orders.stream()
                                .map(OrderDTO::from)
                                .collect(Collectors.toList());
        }

        public UserResponse addUser(UserRequest user) {
                if (userRepository.existsByUserName(user.getUserName())) {
                        throw new ResourceConflictException("Username " + user.getUserName() + " đã tồn tại!");
                } // comment code này đi là bị ăn bom từ DB
                User savedUser = userRepository.save(User.from(user));
                return UserResponse.from(savedUser);
        }

        public void deleteUserById(Long userId) {
                if (!userRepository.existsById(userId)) {
                        throw new ResourceConflictException("Không thể xóa người dùng không tồn tại!");
                }
                orderRepository.deleteByUserId(userId);
                userRepository.deleteById(userId);
        }
}
