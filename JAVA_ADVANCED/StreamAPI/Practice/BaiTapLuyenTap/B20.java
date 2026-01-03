package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class B20 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"));
                Scanner sc = new Scanner(System.in)) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            System.out.println("DASHBOARD");
            System.out.println("> 1. Tổng số sản phẩm");
            System.out.println("> 2. Số sản phẩm còn hàng / hết hàng");
            System.out.println("> 3. Tổng giá trị tồn kho");
            System.out.println("> 4. Category có nhiều SP nhất");
            System.out.println("> 5. Sản phẩm bán chạy nhất (số lượng cao nhất)");
            System.out.println("-".repeat(50));
            System.out.println("-".repeat(50));

            System.out.println("Nhập yêu cầu: ");
            
            // 1
            long totalProduct = products.stream().count();
            // 2
            Map<Boolean, Long> partitionByStock = products.stream()
                    .collect(Collectors.groupingBy(
                            Product::isInStock,
                            Collectors.counting()));
            // 3
            double totalValueInStock = products.stream()
                    .collect(Collectors.summingDouble(p -> p.getPrice() * p.getQuantity()));
            // 4
            String categoryHasMaxQuantity = products.stream()
                    .collect(Collectors.groupingBy(
                        Product::getCategory, 
                        Collectors.counting()
                    ))
                    .entrySet().stream() 
                    .max(Map.Entry.comparingByValue()) // so sánh 2 entry dựa trên value 
                    .map(et -> et.getKey())
                    .orElse("N/A");

            // 5 
            Optional<Product> highestQuantityProduct = products.stream()
                    .max(Comparator.comparing(Product::getQuantity));

            while (true) {
                int request = sc.nextInt();
                if (request == 1) {
                    System.out.println(totalProduct);
                }
                else if (request == 2) {
                    System.out.println("Con hang: " + partitionByStock.get(true));
                    System.out.println("Het hang: " + partitionByStock.get(false));
                }
                else if (request == 3) {
                    System.out.println("Tong gia tri san pham con tren ke: " + totalValueInStock);
                }
                else if (request == 4) {
                    System.out.println("Category co nhieu san pham nhat: " + categoryHasMaxQuantity);
                }
                else {
                    highestQuantityProduct.ifPresentOrElse(p -> System.out.println(p.getName()), () -> System.out.println("Khong co san pham"));
                }
            }
        }
    }
}
