package com.astarhub.astarsquad.Bai5.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.astarhub.astarsquad.Bai5.service.UserService;

@Controller // presentation layer: nhận http request/ response;
// trả về view (file .html)
public class UserController {
    private final UserService userService;

    public UserController (UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showAllUser() {
        // List<User> users = userService.findAllUser();
        // users.forEach((user) -> System.out.println(user.getUserName() + " " + user.getUserPassword()));
        return "user-list.html";
    }
}

// @Component là khi Bean không thuộc 1 trong 3 loại trên, ví dụ: Validation, Helper, ... 
// Controller gọi service, người dùng sẽ gửi request lên tầng này, và cũng trả lại response cho người dùng, 
// @Controller sẽ trả về View, tức là file .html, còn @RestController sẽ trả về dạng json. 

// Client → Controller → Service → Repository → Database
//                ↓         ↓           ↓
//             Validate   Business    CRUD
//              Input      Logic      Data