package com.ducknife.project.modules.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;
import com.ducknife.project.modules.order.dto.OrderResponse;
import com.ducknife.project.modules.user.dto.UserRequest;
import com.ducknife.project.modules.user.dto.UserResponse;
import com.ducknife.project.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @PageableDefault(page = 0, size = 5, sort = "fullname", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(userService.getUsers(pageable));
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByFullname(
        @RequestParam String keyword
    ) {
        return ApiResponse.ok(userService.getUserByFullname(keyword));
    }

    @GetMapping("/sort")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUserByIdLessThan(
            @RequestParam(required = false) Long id,
            @SortDefault(sort = "id", direction = Sort.Direction.DESC) Sort sort) {
        return ApiResponse.ok(userService.getUsersByIdLessThan(id, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUserById(id));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> findOrdersById(@PathVariable Long id) {
        return ApiResponse.ok(userService.findOrdersById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getUserId();
        return ApiResponse.ok(userService.getMe(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> addUser(@RequestBody @Valid UserRequest user) {
        UserResponse savedUser = userService.addUser(user);
        return ApiResponse.created(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateUser(@PathVariable Long id, @RequestBody @Valid UserRequest user) {
        userService.updateUser(id, user);
        return ApiResponse.ok("Cập nhật người dùng thành công!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
