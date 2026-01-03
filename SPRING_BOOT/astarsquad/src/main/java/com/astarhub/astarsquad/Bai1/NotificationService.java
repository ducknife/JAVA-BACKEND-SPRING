package com.astarhub.astarsquad.Bai1;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public void send(String message) {
        System.out.println("Send: " + message);
    }
}
