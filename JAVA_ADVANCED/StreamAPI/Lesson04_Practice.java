package JAVA_ADVANCED.StreamAPI;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * =====================================================
 * BÀI 4: BÀI TẬP THỰC HÀNH TỔNG HỢP
 * =====================================================
 * 
 * Áp dụng tất cả kiến thức đã học để giải quyết các bài toán thực tế
 */
public class Lesson04_Practice {

    public static void main(String[] args) {
        
        List<Product> products = Arrays.asList(
            new Product(1, "iPhone 15", "Điện thoại", 25000000, 50, true),
            new Product(2, "Samsung S24", "Điện thoại", 22000000, 30, true),
            new Product(3, "Xiaomi 14", "Điện thoại", 15000000, 40, true),
            new Product(4, "MacBook Pro", "Laptop", 45000000, 20, true),
            new Product(5, "Dell XPS 15", "Laptop", 35000000, 0, false),
            new Product(6, "Asus ROG", "Laptop", 40000000, 10, true),
            new Product(7, "iPad Pro", "Tablet", 28000000, 15, true),
            new Product(8, "Samsung Tab S9", "Tablet", 20000000, 25, true),
            new Product(9, "AirPods Pro", "Phụ kiện", 5000000, 100, true),
            new Product(10, "Galaxy Buds", "Phụ kiện", 3000000, 0, false)
        );

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           BÀI TẬP THỰC HÀNH STREAM API                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");


        // ============================================================
        // BÀI 1: Lấy danh sách tên sản phẩm có giá > 20 triệu, sắp xếp A-Z
        // ============================================================
        System.out.println("📌 BÀI 1: Sản phẩm > 20 triệu (sắp xếp A-Z)");
        System.out.println("─".repeat(50));
        
        List<String> expensiveProducts = products.stream()
                .filter(p -> p.getPrice() > 20000000)
                .sorted(Comparator.comparing(Product::getName))
                .map(Product::getName)
                .collect(Collectors.toList());
        
        expensiveProducts.forEach(name -> System.out.println("  ✓ " + name));


        // ============================================================
        // BÀI 2: Tính tổng giá trị tồn kho của từng category
        // ============================================================
        System.out.println("\n📌 BÀI 2: Tổng giá trị tồn kho theo category");
        System.out.println("─".repeat(50));
        
        Map<String, Double> inventoryByCategory = products.stream()
                .collect(Collectors.groupingBy(
                    Product::getCategory,
                    Collectors.summingDouble(p -> p.getPrice() * p.getQuantity())
                ));
        
        inventoryByCategory.forEach((category, total) -> 
            System.out.println("  📁 " + category + ": " + String.format("%,.0f VNĐ", total)));


        // ============================================================
        // BÀI 3: Top 3 sản phẩm đắt nhất còn hàng
        // ============================================================
        System.out.println("\n📌 BÀI 3: Top 3 sản phẩm đắt nhất còn hàng");
        System.out.println("─".repeat(50));
        
        products.stream()
                .filter(Product::isInStock)
                .sorted(Comparator.comparing(Product::getPrice).reversed())
                .limit(3)
                .forEach(p -> System.out.println("  🏆 " + p.getName() + " - " + 
                        String.format("%,.0f VNĐ", p.getPrice())));


        // ============================================================
        // BÀI 4: Đếm số sản phẩm trong mỗi category
        // ============================================================
        System.out.println("\n📌 BÀI 4: Số sản phẩm trong mỗi category");
        System.out.println("─".repeat(50));
        
        Map<String, Long> countByCategory = products.stream()
                .collect(Collectors.groupingBy(
                    Product::getCategory,
                    Collectors.counting()
                ));
        
        countByCategory.forEach((category, count) -> 
            System.out.println("  📁 " + category + ": " + count + " sản phẩm"));


        // ============================================================
        // BÀI 5: Tìm sản phẩm có số lượng tồn kho nhiều nhất
        // ============================================================
        System.out.println("\n📌 BÀI 5: Sản phẩm tồn kho nhiều nhất");
        System.out.println("─".repeat(50));
        
        products.stream()
                .max(Comparator.comparing(Product::getQuantity))
                .ifPresent(p -> System.out.println("  📦 " + p.getName() + 
                        " - Số lượng: " + p.getQuantity()));


        // ============================================================
        // BÀI 6: Lấy sản phẩm rẻ nhất của mỗi category
        // ============================================================
        System.out.println("\n📌 BÀI 6: Sản phẩm rẻ nhất mỗi category");
        System.out.println("─".repeat(50));
        
        Map<String, Product> cheapestByCategory = products.stream()
                .collect(Collectors.groupingBy(
                    Product::getCategory,
                    Collectors.collectingAndThen(
                        Collectors.minBy(Comparator.comparing(Product::getPrice)),
                        opt -> opt.orElse(null)
                    )
                ));
        
        cheapestByCategory.forEach((category, product) -> {
            if (product != null) {
                System.out.println("  📁 " + category + ": " + product.getName() + 
                        " - " + String.format("%,.0f VNĐ", product.getPrice()));
            }
        });


        // ============================================================
        // BÀI 7: Kiểm tra và báo cáo
        // ============================================================
        System.out.println("\n📌 BÀI 7: Báo cáo tồn kho");
        System.out.println("─".repeat(50));
        
        long totalProducts = products.stream().count();
        long inStockCount = products.stream().filter(Product::isInStock).count();
        long outOfStockCount = products.stream().filter(p -> !p.isInStock()).count();
        
        double avgPrice = products.stream()
                .mapToDouble(Product::getPrice)
                .average()
                .orElse(0);
        
        int totalQuantity = products.stream()
                .mapToInt(Product::getQuantity)
                .sum();
        
        System.out.println("  📊 Tổng số sản phẩm: " + totalProducts);
        System.out.println("  ✅ Còn hàng: " + inStockCount);
        System.out.println("  ❌ Hết hàng: " + outOfStockCount);
        System.out.println("  💰 Giá trung bình: " + String.format("%,.0f VNĐ", avgPrice));
        System.out.println("  📦 Tổng số lượng tồn: " + totalQuantity);


        // ============================================================
        // BÀI 8: Phân loại sản phẩm theo mức giá
        // ============================================================
        System.out.println("\n📌 BÀI 8: Phân loại theo mức giá");
        System.out.println("─".repeat(50));
        
        Map<String, List<Product>> productsByPriceRange = products.stream()
                .collect(Collectors.groupingBy(p -> {
                    if (p.getPrice() < 10000000) return "💚 Giá rẻ (< 10tr)";
                    else if (p.getPrice() < 30000000) return "💛 Trung bình (10-30tr)";
                    else return "💎 Cao cấp (> 30tr)";
                }));
        
        productsByPriceRange.forEach((range, prods) -> {
            System.out.println("\n  " + range + ":");
            prods.forEach(p -> System.out.println("    - " + p.getName()));
        });


        // ============================================================
        // BÀI 9: Partitioning - Chia thành 2 nhóm
        // ============================================================
        System.out.println("\n📌 BÀI 9: Phân chia còn hàng / hết hàng");
        System.out.println("─".repeat(50));
        
        Map<Boolean, List<Product>> partitionedByStock = products.stream()
                .collect(Collectors.partitioningBy(Product::isInStock));
        
        System.out.println("  ✅ Còn hàng: " + partitionedByStock.get(true).size() + " sản phẩm");
        System.out.println("  ❌ Hết hàng: " + partitionedByStock.get(false).size() + " sản phẩm");


        // ============================================================
        // BÀI 10: Complex query - Kết hợp nhiều operations
        // ============================================================
        System.out.println("\n📌 BÀI 10: Query phức tạp");
        System.out.println("─".repeat(50));
        System.out.println("  Tìm 2 điện thoại rẻ nhất còn hàng, format output:\n");
        
        String result = products.stream()
                .filter(p -> p.getCategory().equals("Điện thoại"))
                .filter(Product::isInStock)
                .sorted(Comparator.comparing(Product::getPrice))
                .limit(2)
                .map(p -> String.format("  📱 %s: %,.0f VNĐ (còn %d chiếc)", 
                        p.getName(), p.getPrice(), p.getQuantity()))
                .collect(Collectors.joining("\n"));
        
        System.out.println(result);


        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  🎉 HOÀN THÀNH TẤT CẢ BÀI TẬP STREAM API!                    ║");
        System.out.println("║  Bạn đã sẵn sàng cho Phase 2: Spring Boot!                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}
