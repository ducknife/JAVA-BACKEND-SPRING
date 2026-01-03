package com.astarhub.astarsquad.Bai3;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class BeanLifeCycle {
    
}

@Repository
class ServiceRepository {

}

@Service
class ServiceService {
	private final ServiceRepository serviceRepository;

	public ServiceService (ServiceRepository serviceRepository) {
		this.serviceRepository = serviceRepository;
	}

    // Câu hỏi phỏng vấn: Tại sao cần @PostConstruct mà không viết luôn trong Constructor?
    // Vì khi contructor chạy, Spring chưa kịp inject vào nên sẽ bị lỗi NullPointerException.
	@PostConstruct // chạy sau khi constructor chạy xong 
	public void init() {
		System.out.println("PostContruct chạy sau constructor");
	}

	@PreDestroy // dọn dẹp trước khi tắt server app 
	public void destroy() { 
		System.out.println("PreDestroy chạy trước khi hủy bean");
	}
}