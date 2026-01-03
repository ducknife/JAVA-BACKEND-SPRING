package com.astarhub.astarsquad.Bai1;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoRunner implements CommandLineRunner {
    private final OrderService orderService;

    public DemoRunner(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) {
        orderService.createOrder("Iphone 17");
    }
}
