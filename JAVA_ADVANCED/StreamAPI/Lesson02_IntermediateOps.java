package JAVA_ADVANCED.StreamAPI;

import java.util.Arrays;
import java.util.List;

/**
 * =====================================================
 * BÀI 2: INTERMEDIATE OPERATIONS (Thao tác trung gian)
 * =====================================================
 * 
 * Intermediate Operations:
 * - Trả về một Stream MỚI
 * - Có thể chain (nối) nhiều operations
 * - LAZY: Không chạy cho đến khi gặp Terminal Operation
 * 
 * Các operations phổ biến:
 * - filter()  : Lọc phần tử theo điều kiện
 * - map()     : Biến đổi phần tử
 * - sorted()  : Sắp xếp
 * - distinct(): Loại bỏ trùng lặp
 * - limit()   : Giới hạn số phần tử
 * - skip()    : Bỏ qua n phần tử đầu
 */
public class Lesson02_IntermediateOps {

    public static void main(String[] args) {
        
        // Dữ liệu mẫu
        List<Product> products = Arrays.asList(
            new Product(1, "iPhone 15", "Điện thoại", 25000000, 50, true),
            new Product(2, "Samsung S24", "Điện thoại", 22000000, 30, true),
            new Product(3, "MacBook Pro", "Laptop", 45000000, 20, true),
            new Product(4, "Dell XPS 15", "Laptop", 35000000, 0, false),
            new Product(5, "iPad Pro", "Tablet", 28000000, 15, true),
            new Product(6, "AirPods Pro", "Phụ kiện", 5000000, 100, true),
            new Product(7, "Galaxy Buds", "Phụ kiện", 3000000, 80, true)
        );


        System.out.println("========== 1. FILTER - Lọc phần tử ==========");
        System.out.println("Lọc sản phẩm còn hàng (inStock = true):\n");
        
        products.stream()
                .filter(p -> p.isInStock())  // Lambda expression
                .forEach(System.out::println);
        
        
        System.out.println("\n========== 2. FILTER - Lọc theo giá ==========");
        System.out.println("Sản phẩm có giá trên 20 triệu:\n");
        
        products.stream()
                .filter(p -> p.getPrice() > 20000000)
                .forEach(System.out::println);


        System.out.println("\n========== 3. MAP - Biến đổi phần tử ==========");
        System.out.println("Lấy tên tất cả sản phẩm:\n");
        
        products.stream()
                .map(p -> p.getName())  // Product -> String
                .forEach(name -> System.out.println("- " + name));
        
        
        System.out.println("\n========== 4. MAP - Tính toán ==========");
        System.out.println("Tính giá trị tồn kho (price * quantity):\n");
        
        products.stream()
                .map(p -> p.getName() + ": " + String.format("%,.0f VNĐ", p.getPrice() * p.getQuantity()))
                .forEach(System.out::println);


        System.out.println("\n========== 5. SORTED - Sắp xếp ==========");
        System.out.println("Sắp xếp theo giá tăng dần:\n");
        
        products.stream()
                .sorted((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()))
                .forEach(System.out::println);


        System.out.println("\n========== 6. SORTED - Giảm dần ==========");
        System.out.println("Sắp xếp theo giá giảm dần:\n");
        
        products.stream()
                .sorted((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()))
                .limit(3)  // Lấy top 3
                .forEach(System.out::println);


        System.out.println("\n========== 7. DISTINCT - Loại bỏ trùng lặp ==========");
        List<String> categories = Arrays.asList(
            "Điện thoại", "Laptop", "Điện thoại", "Tablet", "Laptop", "Phụ kiện"
        );
        
        System.out.println("Danh sách category không trùng:");
        categories.stream()
                  .distinct()
                  .forEach(c -> System.out.println("- " + c));


        System.out.println("\n========== 8. LIMIT & SKIP ==========");
        System.out.println("Bỏ qua 2 sản phẩm đầu, lấy 3 sản phẩm tiếp theo:\n");
        
        products.stream()
                .skip(2)   // Bỏ 2 phần tử đầu
                .limit(3)  // Lấy 3 phần tử
                .forEach(System.out::println);


        System.out.println("\n========== 9. CHAINING - Kết hợp nhiều operations ==========");
        System.out.println("Lọc điện thoại còn hàng, sắp xếp theo giá, lấy tên:\n");
        
        products.stream()
                .filter(p -> p.getCategory().equals("Điện thoại"))  // Lọc category
                .filter(p -> p.isInStock())                          // Lọc còn hàng
                .sorted((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()))  // Sắp xếp
                .map(p -> p.getName() + " - " + String.format("%,.0f VNĐ", p.getPrice()))  // Format
                .forEach(System.out::println);

        
        System.out.println("\n✅ Bài 2 hoàn thành! Chạy Lesson03 để học Terminal Operations.");
    }
}
