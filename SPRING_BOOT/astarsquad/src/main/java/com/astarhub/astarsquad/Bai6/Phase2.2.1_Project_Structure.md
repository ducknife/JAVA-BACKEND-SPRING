# 📚 Bài 6: Project Structure (Cấu trúc dự án Spring Boot)

---

## 📑 Mục Lục

- [🎯 Mục tiêu](#mục-tiêu)
- [1. Cấu trúc mặc định](#1-cấu-trúc-mặc-định)
- [2. Giải thích từng phần](#2-giải-thích-từng-phần)
  - [📁 src/main/java - Code chính](#srcmainjava-code-chính)
  - [📁 src/main/resources - Tài nguyên](#srcmainresources-tài-nguyên)
  - [📁 src/test/java - Unit tests](#srctestjava-unit-tests)
- [3. Package theo Layer (Phổ biến)](#3-package-theo-layer-phổ-biến)
  - [Cấu trúc:](#cấu-trúc)
  - [Ưu điểm:](#ưu-điểm)
  - [Nhược điểm:](#nhược-điểm)
- [4. Package theo Feature (Khuyên dùng cho dự án lớn)](#4-package-theo-feature-khuyên-dùng-cho-dự-án-lớn)
  - [Cấu trúc:](#cấu-trúc)
  - [Ưu điểm:](#ưu-điểm)
  - [Nhược điểm:](#nhược-điểm)
- [5. So sánh 2 cách tổ chức](#5-so-sánh-2-cách-tổ-chức)
  - [Khuyên dùng:](#khuyên-dùng)
- [6. Main Application Class](#6-main-application-class)
  - [@SpringBootApplication bao gồm:](#springbootapplication-bao-gồm)
  - [⚠️ Lưu ý quan trọng về @ComponentScan:](#️-lưu-ý-quan-trọng-về-componentscan)
- [7. Thư mục resources/](#7-thư-mục-resources)
  - [application.properties:](#applicationproperties)
  - [static/ - File tĩnh:](#static-file-tĩnh)
  - [templates/ - HTML templates (Thymeleaf):](#templates-html-templates-thymeleaf)
- [8. pom.xml - Maven Dependencies](#8-pomxml-maven-dependencies)
  - [Các starter phổ biến:](#các-starter-phổ-biến)
- [9. Maven Wrapper (mvnw)](#9-maven-wrapper-mvnw)
- [10. Cấu trúc dự án thực tế](#10-cấu-trúc-dự-án-thực-tế)
- [📌 Tóm tắt](#tóm-tắt)
  - [Cấu trúc cơ bản:](#cấu-trúc-cơ-bản)
  - [Package organization:](#package-organization)
  - [Quy tắc:](#quy-tắc)

---

## 🎯 Mục tiêu
Hiểu **cấu trúc thư mục** chuẩn của dự án Spring Boot và tổ chức code theo best practice.

---

## 1. Cấu trúc mặc định

```
my-project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── myproject/
│   │   │               └── MyProjectApplication.java  ← Main class
│   │   └── resources/
│   │       ├── application.properties  ← Cấu hình
│   │       ├── static/                  ← CSS, JS, images
│   │       └── templates/               ← HTML templates
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── myproject/
│                       └── MyProjectApplicationTests.java
├── pom.xml          ← Maven dependencies
└── mvnw / mvnw.cmd  ← Maven wrapper
```

---

## 2. Giải thích từng phần

### 📁 src/main/java - Code chính

```
src/main/java/com/example/myproject/
│
├── MyProjectApplication.java     ← Entry point (@SpringBootApplication)
├── controller/                   ← @RestController, @Controller
├── service/                      ← @Service
├── repository/                   ← @Repository
├── model/                        ← Entity, DTO
├── config/                       ← @Configuration
├── exception/                    ← Custom exceptions
└── util/                         ← Helper classes
```

### 📁 src/main/resources - Tài nguyên

```
src/main/resources/
│
├── application.properties        ← Cấu hình chính
├── application.yml               ← Hoặc dùng YAML
├── application-dev.properties    ← Cấu hình môi trường dev
├── application-prod.properties   ← Cấu hình môi trường prod
├── static/                       ← File tĩnh (CSS, JS, images)
│   ├── css/
│   ├── js/
│   └── images/
└── templates/                    ← Thymeleaf templates (HTML)
```

### 📁 src/test/java - Unit tests

```
src/test/java/com/example/myproject/
│
├── controller/
│   └── ProductControllerTest.java
├── service/
│   └── ProductServiceTest.java
└── repository/
    └── ProductRepositoryTest.java
```

---

## 3. Package theo Layer (Phổ biến)

### Cấu trúc:

```
com.example.myproject/
│
├── MyProjectApplication.java
│
├── controller/
│   ├── ProductController.java
│   ├── OrderController.java
│   └── UserController.java
│
├── service/
│   ├── ProductService.java
│   ├── OrderService.java
│   └── UserService.java
│
├── repository/
│   ├── ProductRepository.java
│   ├── OrderRepository.java
│   └── UserRepository.java
│
├── model/
│   ├── entity/
│   │   ├── Product.java
│   │   ├── Order.java
│   │   └── User.java
│   └── dto/
│       ├── ProductDTO.java
│       └── OrderDTO.java
│
├── config/
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
│
├── exception/
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
│
└── util/
    └── DateUtils.java
```

### Ưu điểm:

```
✅ Dễ tìm file theo loại (tất cả Service ở 1 chỗ)
✅ Phổ biến, nhiều người quen
✅ Phù hợp dự án nhỏ-vừa
```

### Nhược điểm:

```
❌ Dự án lớn → package service/ có quá nhiều file
❌ Khó tách module
```

---

## 4. Package theo Feature (Khuyên dùng cho dự án lớn)

### Cấu trúc:

```
com.example.myproject/
│
├── MyProjectApplication.java
│
├── product/
│   ├── ProductController.java
│   ├── ProductService.java
│   ├── ProductRepository.java
│   ├── Product.java
│   └── ProductDTO.java
│
├── order/
│   ├── OrderController.java
│   ├── OrderService.java
│   ├── OrderRepository.java
│   ├── Order.java
│   └── OrderDTO.java
│
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserRepository.java
│   ├── User.java
│   └── UserDTO.java
│
├── common/
│   ├── config/
│   ├── exception/
│   └── util/
│
└── security/
    ├── SecurityConfig.java
    └── JwtService.java
```

### Ưu điểm:

```
✅ Liên quan đến Product → vào package product/
✅ Dễ tách thành microservice sau này
✅ Mỗi feature độc lập
```

### Nhược điểm:

```
❌ Không quen với người mới
❌ Dự án nhỏ có thể overkill
```

---

## 5. So sánh 2 cách tổ chức

| | Package by Layer | Package by Feature |
|---|-----------------|-------------------|
| **Cấu trúc** | controller/, service/, repository/ | product/, order/, user/ |
| **Dự án nhỏ** | ✅ Phù hợp | Overkill |
| **Dự án lớn** | Khó quản lý | ✅ Phù hợp |
| **Tách microservice** | Khó | ✅ Dễ |
| **Tìm file** | Theo loại | Theo feature |

### Khuyên dùng:

```
Dự án nhỏ-vừa (< 20 entities) → Package by Layer
Dự án lớn (> 20 entities)     → Package by Feature
```

---

## 6. Main Application Class

```java
package com.example.myproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class MyProjectApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MyProjectApplication.class, args);
    }
}
```

### @SpringBootApplication bao gồm:

| Annotation | Chức năng |
|------------|-----------|
| **@Configuration** | Đánh dấu class là nguồn cấu hình |
| **@EnableAutoConfiguration** | Tự động cấu hình dựa trên dependencies |
| **@ComponentScan** | Scan tất cả package con để tìm Bean |

### ⚠️ Lưu ý quan trọng về @ComponentScan:

```
com.example.myproject/
├── MyProjectApplication.java    ← @SpringBootApplication ở đây
├── controller/                  ✅ Được scan
├── service/                     ✅ Được scan
└── repository/                  ✅ Được scan

com.other.package/
└── SomeService.java             ❌ KHÔNG được scan (khác package gốc)
```

**Main class phải ở package gốc để scan được tất cả package con!**

---

## 7. Thư mục resources/

### application.properties:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=123456

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### static/ - File tĩnh:

```
resources/static/
├── css/
│   └── style.css
├── js/
│   └── app.js
└── images/
    └── logo.png
```

**Truy cập:** `http://localhost:8080/css/style.css`

### templates/ - HTML templates (Thymeleaf):

```
resources/templates/
├── index.html
├── products/
│   ├── list.html
│   └── detail.html
└── layout/
    └── header.html
```

---

## 8. pom.xml - Maven Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Parent: Spring Boot -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <!-- Project info -->
    <groupId>com.example</groupId>
    <artifactId>my-project</artifactId>
    <version>1.0.0</version>
    <name>My Project</name>
    
    <!-- Java version -->
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <!-- Dependencies -->
    <dependencies>
        <!-- Spring Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <!-- Build -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Các starter phổ biến:

| Starter | Chức năng |
|---------|-----------|
| `spring-boot-starter-web` | REST API, MVC |
| `spring-boot-starter-data-jpa` | JPA, Hibernate |
| `spring-boot-starter-security` | Authentication, Authorization |
| `spring-boot-starter-validation` | Bean Validation |
| `spring-boot-starter-test` | JUnit, Mockito |

---

## 9. Maven Wrapper (mvnw)

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Lợi ích:** Không cần cài Maven, dùng version đúng của dự án.

---

## 10. Cấu trúc dự án thực tế

```
my-ecommerce/
├── src/
│   ├── main/
│   │   ├── java/com/example/ecommerce/
│   │   │   ├── EcommerceApplication.java
│   │   │   │
│   │   │   ├── product/
│   │   │   │   ├── Product.java
│   │   │   │   ├── ProductDTO.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── ProductService.java
│   │   │   │   └── ProductController.java
│   │   │   │
│   │   │   ├── order/
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── OrderDTO.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── OrderService.java
│   │   │   │   └── OrderController.java
│   │   │   │
│   │   │   ├── user/
│   │   │   │   ├── User.java
│   │   │   │   ├── UserDTO.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── UserService.java
│   │   │   │   └── UserController.java
│   │   │   │
│   │   │   ├── cart/
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── common/
│   │   │   │   ├── exception/
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   └── util/
│   │   │   │       └── DateUtils.java
│   │   │   │
│   │   │   └── config/
│   │   │       ├── SecurityConfig.java
│   │   │       └── SwaggerConfig.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   │
│   └── test/java/com/example/ecommerce/
│       ├── product/
│       │   └── ProductServiceTest.java
│       └── order/
│           └── OrderServiceTest.java
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── README.md
```

---

## 📌 Tóm tắt

### Cấu trúc cơ bản:

```
src/main/java/      → Code Java
src/main/resources/ → Cấu hình, static files
src/test/java/      → Unit tests
pom.xml             → Dependencies
```

### Package organization:

```
By Layer:   controller/, service/, repository/  → Dự án nhỏ
By Feature: product/, order/, user/             → Dự án lớn
```

### Quy tắc:

```
1. Main class ở package gốc (để @ComponentScan hoạt động)
2. Tách rõ các layer (Controller → Service → Repository)
3. Config riêng (application.properties, @Configuration)
4. Test theo cấu trúc giống main
```

---

**Bài tiếp theo:** application.properties / application.yml
