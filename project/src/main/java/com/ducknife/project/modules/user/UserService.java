package com.ducknife.project.modules.user;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ducknife.project.common.exception.ResourceConflictException;
import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.order.Order;
import com.ducknife.project.modules.order.OrderRepository;
import com.ducknife.project.modules.order.dto.OrderResponse;
import com.ducknife.project.modules.role.Role;
import com.ducknife.project.modules.role.RoleRepository;
import com.ducknife.project.modules.user.dto.UserRequest;
import com.ducknife.project.modules.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

        private final UserRepository userRepository;
        private final OrderRepository orderRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;

        public UserResponse getMe(Long id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
                return UserResponse.from(user);
        }

        @PreAuthorize("hasAnyRole('ADMIN', 'COLLABORATOR')")
        public Page<UserResponse> getUsers(Pageable pageable) {
                return userRepository.findByNameLength(pageable)
                                .map(UserResponse::from);
        }

        @PreAuthorize("hasRole('ADMIN')")
        public List<UserResponse> getUsersByIdLessThan(Long id, Sort sort) {
                return userRepository.findByIdLessThan(id, sort)
                                .stream()
                                .map(UserResponse::from)
                                .collect(Collectors.toList());
        }

        @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
        public List<UserResponse> getUserByFullname(String keyword) {
                return userRepository.findByFullname(keyword)
                                .stream()
                                .map(UserResponse::from)
                                .collect(Collectors.toList());
        }

        @PreAuthorize("hasAnyRole('ADMIN', 'USER') or #userId == authentication.principal.id")
        public UserResponse getUserById(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                return UserResponse.from(user);
        }

        @PreAuthorize("hasAnyRole('ADMIN', 'COLLABORATOR', 'USER') or #userId == authenticated.principal.id")
        public List<OrderResponse> findOrdersById(Long userId) {
                if (!userRepository.existsById(userId)) {
                        throw new ResourceNotFoundException("Không tìm thấy người dùng");
                }
                List<Order> orders = orderRepository.findByUserId(userId);
                return orders.stream()
                                .map(OrderResponse::from)
                                .collect(Collectors.toList());
        }

        @PreAuthorize("hasAnyRole('ADMIN', 'COLLABORATOR')")
        @Transactional
        // @Transactional(propagation = Propagation.NEVER) // chạy ok vì method trong
        // controller không có transaction
        public UserResponse addUser(UserRequest user) {
                if (userRepository.existsByUsername(user.getUsername())) {
                        throw new ResourceConflictException("Username " + user.getUsername() + " đã tồn tại!");
                } // comment code này đi là bị ăn bom từ DB
                User newUser = User.from(user);
                newUser.setPassword(passwordEncoder.encode(user.getPassword()));
                Set<Role> roles = user.getRoles().stream()
                                .map(role -> roleRepository.findByName(role)
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Không tìm thấy role")))
                                .collect(Collectors.toSet());
                newUser.setRoles(roles);
                User savedUser = userRepository.save(newUser);
                return UserResponse.from(savedUser);
        }

        @PreAuthorize("@perm.canUpdateUser(#id, authentication)")
        @Transactional(propagation = Propagation.SUPPORTS)
        public void updateUser(Long id, UserRequest newUser) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
                user.setFullname(newUser.getFullname());
                user.setUsername(newUser.getUsername());
                user.setPassword(newUser.getPassword());
                Set<Role> roles = newUser.getRoles().stream()
                                .map(role -> roleRepository.findByName(role)
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Không tìm thấy role")))
                                .collect(Collectors.toSet());
                user.setRoles(roles);
                // nếu không gọi save thì nó sẽ không lưu được.
                userRepository.save(user);
        }

        // @Transactional(propagation = Propagation.MANDATORY, rollbackFor =
        // Exception.class) // do không có Transaction
        // cha nào đang hoạt động nên
        // nó bị lỗi
        @PreAuthorize("hasRole('ADMIN')")
        @Transactional(rollbackFor = Exception.class)
        public void deleteUserById(Long userId) {
                if (!userRepository.existsById(userId)) {
                        throw new ResourceConflictException("Không thể xóa người dùng không tồn tại!");
                }
                orderRepository.deleteByUserId(userId);
                userRepository.deleteById(userId);
        }
}

// Propagation định nghĩa hành vi của một hàm có transaction khi được gọi từ một
// method khác (có transaction hoặc không)
// 1. Required: nếu một transaction đang hoạt động thì nó sẽ được dùng chung,
// nếu ko nó sẽ tạo một transaction mới. (mặc định).
// 2. Support: nếu một transaction đang hoạt động, nó sẽ dùng chung, nếu không
// thì nó sẽ chạy mà không mở một transaction nào.
// 3. Mandatory: bắt buộc phải có 1 transaction đang hoạt động trước khi gọi nó,
// nếu không nó sẽ ném lỗi.
// 4. Never: bắt buộc hàm gọi không được có transaction, nếu có transaction thì
// nó sẽ báo lỗi.
// 5. Not supported: dừng transaction hiện tại và hoạt động mà không mở một
// transaction nào.
// 6. Nested: nếu có transaction đang hoạt động thì nó sẽ dùng chung và tạo 1
// savepoint trong đó, nếu gặp lỗi, nó sẽ rollback về savepoint này
// nếu ko có transaction nào, nó sẽ mở một transaction mới
// 7. Required new: nếu được gọi bởi transaction đang hoạt động, nó sẽ dừng
// transaction đó, mở một transaction độc lập,
// khi hoàn thành thì transaction cha sẽ được thực hiện tiếp. Nếu transaction
// cha lỗi, con không bị gì nếu đã commit xong.
// còn nếu không trong transaction đang hoạt động, nó sẽ tạo một transaction mới
// luôn.

// Isolation levels:
// tránh được dirty read, non-repeatable read, phantom read;
// READ_UNCOMMITED: đọc cả dữ liệu chưa commit
// READ_COMMITED: chỉ đọc khi dữ liệu đã commit, tuy nhiên vẫn bị non-repeatable
// read, phantom read;
// REPEATABLE_READ: trong sql-92, khi nó đọc 1 dòng, không cho phép transaction
// khác sửa/xóa;
// tuy nhiên, trong các db hiện đại, dùng cơ chế mvcc, nên nó không chặn các
// transaction khác truy cập, thay vào đó,
// nó vẫn truy cập dữ liệu với bản snapshot ban đầu. Có thể bị phantom read tùy
// db;
// SERIALIZABLE: đây là cấp độ cao nhất, chặn được mọi lỗi, biến mọi transaction
// thành tuần tự, tuy nhiên siêu chậm;