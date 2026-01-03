package JAVA_ADVANCED.StreamAPI;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * =====================================================
 * BÀI 3: TERMINAL OPERATIONS (Thao tác kết thúc)
 * =====================================================
 * 
 * Terminal Operations:
 * - KẾT THÚC stream pipeline
 * - KÍCH HOẠT việc xử lý (vì stream là lazy)
 * - Trả về kết quả CUỐI CÙNG (không phải Stream)
 * 
 * Các operations phổ biến:
 * - forEach()   : Duyệt từng phần tử
 * - collect()   : Thu thập thành Collection
 * - count()     : Đếm số phần tử
 * - reduce()    : Gộp thành 1 giá trị
 * - findFirst() : Tìm phần tử đầu tiên
 * - findAny()   : Tìm bất kỳ phần tử nào
 * - anyMatch()  : Kiểm tra có phần tử nào thỏa mãn
 * - allMatch()  : Kiểm tra tất cả thỏa mãn
 * - noneMatch() : Kiểm tra không có phần tử nào thỏa mãn
 * - min(), max(): Tìm min/max
 */
public class Lesson03_TerminalOps {

    public static void main(String[] args) {
        
        List<Product> products = Arrays.asList(
            new Product(1, "iPhone 15", "Điện thoại", 25000000, 50, true),
            new Product(2, "Samsung S24", "Điện thoại", 22000000, 30, true),
            new Product(3, "MacBook Pro", "Laptop", 45000000, 20, true),
            new Product(4, "Dell XPS 15", "Laptop", 35000000, 0, false),
            new Product(5, "iPad Pro", "Tablet", 28000000, 15, true),
            new Product(6, "AirPods Pro", "Phụ kiện", 5000000, 100, true),
            new Product(7, "Galaxy Buds", "Phụ kiện", 3000000, 80, true)
        );


        System.out.println("========== 1. COLLECT - Thu thập thành List ==========");
        List<String> productNames = products.stream()
                .map(Product::getName)
                .collect(Collectors.toList());
        
        System.out.println("Danh sách tên sản phẩm: " + productNames);


        System.out.println("\n========== 2. COLLECT - Joining (Nối chuỗi) ==========");
        String allNames = products.stream()
                .map(Product::getName)
                .collect(Collectors.joining(", "));
        
        System.out.println("Tất cả sản phẩm: " + allNames);


        System.out.println("\n========== 3. COLLECT - GroupingBy (Nhóm theo) ==========");
        Map<String, List<Product>> productsByCategory = products.stream()
                .collect(Collectors.groupingBy(Product::getCategory));
        
        System.out.println("Sản phẩm theo danh mục:");
        productsByCategory.forEach((category, prods) -> {
            System.out.println("\n📁 " + category + ":");
            prods.forEach(p -> System.out.println("   - " + p.getName()));
        });


        System.out.println("\n========== 4. COUNT - Đếm số phần tử ==========");
        long countInStock = products.stream()
                .filter(Product::isInStock)
                .count();
        
        System.out.println("Số sản phẩm còn hàng: " + countInStock);


        System.out.println("\n========== 5. REDUCE - Tính tổng ==========");
        // Tính tổng giá trị tồn kho
        double totalInventoryValue = products.stream()
                .map(p -> p.getPrice() * p.getQuantity())
                .reduce(0.0, (sum, value) -> sum + value);
        
        System.out.println("Tổng giá trị tồn kho: " + String.format("%,.0f VNĐ", totalInventoryValue));
        
        // Cách viết ngắn hơn với method reference
        double totalValue2 = products.stream()
                .map(p -> p.getPrice() * p.getQuantity())
                .reduce(0.0, Double::sum);
        
        System.out.println("Cách 2: " + String.format("%,.0f VNĐ", totalValue2));


        System.out.println("\n========== 6. MIN & MAX ==========");
        Optional<Product> cheapest = products.stream()
                .min((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
        
        Optional<Product> mostExpensive = products.stream()
                .max((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
        
        cheapest.ifPresent(p -> System.out.println("Rẻ nhất: " + p.getName() + " - " + String.format("%,.0f VNĐ", p.getPrice())));
        mostExpensive.ifPresent(p -> System.out.println("Đắt nhất: " + p.getName() + " - " + String.format("%,.0f VNĐ", p.getPrice())));


        System.out.println("\n========== 7. FIND FIRST & FIND ANY ==========");
        Optional<Product> firstPhone = products.stream()
                .filter(p -> p.getCategory().equals("Điện thoại"))
                .findFirst();
        
        firstPhone.ifPresent(p -> System.out.println("Điện thoại đầu tiên: " + p.getName()));
        
        // findAny() thường dùng với parallelStream() để tăng performance
        Optional<Product> anyLaptop = products.parallelStream()
                .filter(p -> p.getCategory().equals("Laptop"))
                .findAny();
        
        anyLaptop.ifPresent(p -> System.out.println("Một laptop bất kỳ: " + p.getName()));


        System.out.println("\n========== 8. MATCH - Kiểm tra điều kiện ==========");
        
        // anyMatch: Có ít nhất 1 phần tử thỏa mãn?
        boolean hasExpensiveProduct = products.stream()
                .anyMatch(p -> p.getPrice() > 40000000);
        System.out.println("Có sản phẩm trên 40 triệu? " + hasExpensiveProduct);
        
        // allMatch: Tất cả đều thỏa mãn?
        boolean allHaveName = products.stream()
                .allMatch(p -> p.getName() != null && !p.getName().isEmpty());
        System.out.println("Tất cả đều có tên? " + allHaveName);
        
        // noneMatch: Không có phần tử nào thỏa mãn?
        boolean noneNegativePrice = products.stream()
                .noneMatch(p -> p.getPrice() < 0);
        System.out.println("Không có giá âm? " + noneNegativePrice);


        System.out.println("\n========== 9. TỔNG HỢP - SummaryStatistics ==========");
        var priceStats = products.stream()
                .mapToDouble(Product::getPrice)
                .summaryStatistics();
        
        System.out.println("📊 Thống kê giá:");
        System.out.println("   - Số lượng: " + priceStats.getCount());
        System.out.println("   - Tổng: " + String.format("%,.0f VNĐ", priceStats.getSum()));
        System.out.println("   - Trung bình: " + String.format("%,.0f VNĐ", priceStats.getAverage()));
        System.out.println("   - Min: " + String.format("%,.0f VNĐ", priceStats.getMin()));
        System.out.println("   - Max: " + String.format("%,.0f VNĐ", priceStats.getMax()));


        System.out.println("\n========== 10. COLLECT - toMap ==========");
        Map<Integer, String> idToName = products.stream()
                .collect(Collectors.toMap(
                    Product::getId,      // Key
                    Product::getName     // Value
                ));
        
        System.out.println("Map ID -> Tên: " + idToName);

        
        System.out.println("\n✅ Bài 3 hoàn thành! Chạy Lesson04 để xem bài tập thực hành.");
    }
}
