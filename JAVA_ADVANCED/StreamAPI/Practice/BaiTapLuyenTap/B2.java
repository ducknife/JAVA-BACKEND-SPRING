package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class B2 {
    public static void main(String[] args) {
        List<Product> products = Arrays.asList(
                new Product(1, "iPhone 15", "Điện thoại", 25000000, 50, true),
                new Product(2, "Samsung S24", "Điện thoại", 22000000, 30, true),
                new Product(3, "MacBook Pro", "Laptop", 45000000, 20, true),
                new Product(4, "Dell XPS 15", "Laptop", 35000000, 0, false),
                new Product(5, "iPad Pro", "Tablet", 28000000, 15, true),
                new Product(6, "AirPods Pro", "Phụ kiện", 5000000, 100, true),
                new Product(7, "Galaxy Buds", "Phụ kiện", 3000000, 80, true),
                new Product(8, "Xiaomi 14", "Điện thoại", 18000000, 45, true),
                new Product(9, "Asus ROG", "Laptop", 55000000, 10, true),
                new Product(10, "Apple Watch", "Phụ kiện", 12000000, 0, false));
        List<String> productsName = products.stream() 
                    .map(Product::getName)
                    .collect(Collectors.toList());
        productsName.forEach(System.out::println);
    }
}

// Bài 2: Lấy tên sản phẩm
// > Lấy danh sách TÊN của tất cả sản phẩm
