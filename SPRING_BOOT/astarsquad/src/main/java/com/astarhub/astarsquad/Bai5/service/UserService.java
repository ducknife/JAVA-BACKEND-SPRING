// package com.astarhub.astarsquad.Bai5.service;

// import java.util.List;

// import org.springframework.stereotype.Service;

// import com.astarhub.astarsquad.Bai5.User;
// import com.astarhub.astarsquad.Bai5.repository.UserRepository;

// @Service
// public class UserService {
//     private final UserRepository userRepository;

//     public UserService(UserRepository userRepository) {
//         this.userRepository = userRepository;
//     }

//     public boolean isValidAccount(User user) {
//         if (user.getUserName() == null || user.getUserName().isEmpty()) return false;
//         if (user.getUserPassword() == null || user.getUserPassword().isEmpty()) return false;
//         return true;
//     }

//     public List<User> findAllUser() {
//         return userRepository.findAll();
//     }
// }

// // gọi repository, được gọi từ controller, chỉ xử lí các logic nghiệp vụ, cú pháp đặt tên: XxxService.java 
