package com.astarhub.astarsquad.Bai1;

import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final NotificationService notificationService;

    public OrderService (NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void createOrder(String productName) {
        System.out.println("Tạo đơn hàng: " + productName);
        notificationService.send("Đơn hàng " + productName + " đã tạo");
    }
}
