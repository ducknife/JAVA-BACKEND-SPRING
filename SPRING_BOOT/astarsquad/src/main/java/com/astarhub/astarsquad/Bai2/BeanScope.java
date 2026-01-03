package com.astarhub.astarsquad.Bai2;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// scope xác định bao nhiêu instance được tạo ra 
@Component
public class BeanScope implements CommandLineRunner {
    private final RoomService roomService1;
    private final RoomService roomService2;
    private final ServiceService serviceService1;
    private final ServiceService serviceService2;

    public BeanScope(RoomService roomService1, RoomService roomService2, ServiceService serviceService1,
            ServiceService serviceService2) {
        this.roomService1 = roomService1;
        this.roomService2 = roomService2;
        this.serviceService1 = serviceService1;
        this.serviceService2 = serviceService2;
    }

    @Override
    public void run(String... args) {
        System.out.println(roomService1 == roomService2); // true
        System.out.println(serviceService1 == serviceService2); // false
    }
}

@Service
@Scope("singleton") // thực tế, singleton + stateless là phổ biến nhất, chỉ cần truyền tham số.
// Default, dùng khi dùng chung instance;
// Stateless (ko lưu dữ liệu riêng tư tránh thread-safty issue);
// Dùng chung;
class RoomService {
    // không có các private ví dụ: private Order currentOrder của từng người dùng;
    // Dữ liệu được truyền qua tham số, không lưu trong class
    // ex:
    public void createOrder(String user // các tham số khác
    ) {
        System.out.println("Đặt phòng cho user " + user);
        // xử lí ở đây;
    }
    // ví dụ cụ thể: CartService là stateless (không lưu data), nên dữ liệu của mỗi
    // người được lưu riêng trong DB.
}

@Service
@Scope("prototype")
// Mỗi lần inject tạo một instance mới;
// Dùng khi cần lưu trạng thái, cần instance riêng: ví dụ mỗi người dùng cần 1
// giỏ riêng;
class ServiceService {
    private final String nameService; // thêm từ khóa final để nó không thể bị gán lại.
    private final boolean isActive;

    public ServiceService(String nameService, boolean isActive) {
        this.nameService = nameService;
        this.isActive = isActive;
    }
    // ví dụ cụ thể: khi được inject, nó tạo mỗi người dùng 1 instance,
    // dữ liệu thì vẫn lưu trong db như single + stateless.

    public String getNameService() {
        return nameService;
    }

}

@Service
class UserService {
    private final Optional<ServiceService> serviceService; // đây là 1 prototype
    // để dùng trong 1 singleton phải bọc trong Optional.

    public UserService(Optional<ServiceService> serviceService) { 
        this.serviceService = serviceService;
    }

    public String take() {
        return serviceService.map(s -> s.getNameService()).orElse("N/A");
    }
}

@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS) 
// Viết gọn: RequestScope, dùng khi cần lưu thông tin request  
// Tạo mới mỗi request, proxyMode giúp tìm đúng instance cho mỗi request;
// Một request có nhiều inject cùng instance;
class RequestContext {
    private final UserService userService;
    public RequestContext(UserService userService) {
        this.userService = userService;
    }

}

@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
// Viết gọn: SessionScope, dùng khi lưu thông tin user suốt phiên đăng nhập 
// Tạo mới mỗi session
// Một session có nhiều request cùng instance 
class SessionContext {
    private final String userName;
    private final ArrayList<String> recentViews = new ArrayList<>();
    public SessionContext(String userName) {
        this.userName = userName;
    }
    
}