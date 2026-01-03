package JAVA_ADVANCED.StreamAPI.Practice;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import JAVA_ADVANCED.StreamAPI.Product;

public class Lesson02 {
    public static void main(String[] args) {
        List<Product> products = Arrays.asList(
                new Product(1, "iPhone 15", "Điện thoại", 25000000, 50, true),
                new Product(2, "Samsung S24", "Điện thoại", 22000000, 30, true),
                new Product(3, "MacBook Pro", "Laptop", 45000000, 20, true),
                new Product(4, "Dell XPS 15", "Laptop", 35000000, 0, false),
                new Product(5, "iPad Pro", "Tablet", 28000000, 15, true),
                new Product(6, "AirPods Pro", "Phụ kiện", 5000000, 100, true),
                new Product(7, "Galaxy Buds", "Phụ kiện", 3000000, 80, true));

        // filter -> lọc các phần tử 
        products.stream()
                .filter(Product::isInStock)
                .limit(2)
                .forEach(System.out::println);

        products.stream()
                .filter(p -> p.getPrice() > 4500000)
                .limit(3)
                .forEach(System.out::println);

        // map -> biến đổi phần tử, tính toán, phảI có kiểu dữ liệu trả về 
        products.stream()
                .map(p -> "Product's Price: " + String.format("%.2f", p.getPrice()))
                .limit(3)
                .forEach(System.out::println);
        products.stream()
                .map(p -> String.format("Price add 10000.00: %.2f", p.getPrice() + 10000.00)) // trả về String 
                .limit(3)
                .forEach(System.out::println);

        products.stream()
                .map(p -> "Product's name: " + p.getName())
                .limit(3)
                .forEach(System.out::println);

        // sorted -> Không làm thay đổi, sort thì thay đổi cả dữ liệu gốc và tốn hiệu năng hơn sorted 
        products.stream()
                .sorted(Comparator.comparingDouble(Product::getPrice))
                .limit(3)
                .forEach(System.out::println);

        products.stream()
                .sorted(Comparator.comparing(Product::getName))
                .limit(3)
                .forEach(System.out::println);

        // distinct : loại trùng lặp
        products.stream()
                .map(p -> p.getCategory())
                .distinct()
                .forEach(System.out::println);

        products.stream()
                .map(p -> p.isInStock())
                .distinct()
                .forEach(System.out::println);

        // skip(n) : bỏ qua n phần từ đầu 
        products.stream()
                .map(p -> p.getCategory())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .skip(2)
                .forEach(System.out::println);
        
        double n;
        try (Scanner sc = new Scanner(System.in)) {
            n = sc.nextDouble();
        }
        products.stream()
                .map(p -> String.format("%.2f", p.getPrice()))
                .sorted()
                .filter(price -> Double.parseDouble(price) > n)
                .forEach(System.out::println);
    
    }
}
// Thao tác trung gian: trả về 1 stream mới, có thể nối nhiều operations như bài
// 1 đã demo limit