package com.astarhub.astarsquad.Bai2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
@Scope("singleton")
public class BeanExample {
    
}

@Service
@Scope("prototype")
class UserService {

}

@Controller
class UserController {

}

@Repository
class UserRepository {

}

@Configuration // đánh dấu đây là class cấu hình cài đặt, cũng là 1 bean nhưng không nên và không cần inject nó.
class corsConfig {
    @Bean // sử dụng class có sẵn, có thể custom 
    public WebMvcConfigurer corsConfigurer () {
        return new WebMvcConfigurer() {
            //...
        };
    }
}
