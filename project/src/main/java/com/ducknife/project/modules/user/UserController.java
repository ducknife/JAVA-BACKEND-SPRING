package com.ducknife.project.modules.user;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;
import com.ducknife.project.modules.order.OrderDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsers() {
        return ApiResponse.ok(userService.getUsers());
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> findOrdersById(@PathVariable Long id) {
        return ApiResponse.ok(userService.findOrdersById(id));
    }
}
