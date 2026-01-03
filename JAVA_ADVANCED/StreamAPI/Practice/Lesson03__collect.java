package JAVA_ADVANCED.StreamAPI.Practice;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import JAVA_ADVANCED.StreamAPI.Product;

public class Lesson03__collect {
    public static void main(String[] args) {
        List<Product> products = Arrays.asList(
                new Product(1, "iPhone 15", "Điện thoại", 25000000, 50, true),
                new Product(2, "Samsung S24", "Điện thoại", 22000000, 30, true),
                new Product(3, "MacBook Pro", "Laptop", 45000000, 20, true),
                new Product(4, "Dell XPS 15", "Laptop", 35000000, 0, false),
                new Product(5, "iPad Pro", "Tablet", 28000000, 15, true),
                new Product(6, "AirPods Pro", "Phụ kiện", 5000000, 100, true),
                new Product(7, "Galaxy Buds", "Phụ kiện", 3000000, 80, true));

        // collect - toList/toSet -> trả về list hoặc set  
        List<Product> topThreePriceProduct = products.stream()
                .filter(p -> p.isInStock())
                .sorted(Comparator.comparingDouble(Product::getPrice).reversed())
                .limit(3)
                .collect(Collectors.toList());
        topThreePriceProduct.forEach(System.out::println);

        // collect - joining: nối thành chuỗi -> trả về string 
        String allNames = products.stream()
                .map(Product::getName)
                .collect(Collectors.joining(", "));
        System.out.println(allNames);
        
        // collect - groupingBy: Nhóm theo -> trả về map<key, list<value>>
        Map<String, List<Product>> productGroupByCategory = products.stream()
                .collect(Collectors.groupingBy(Product::getCategory));
        productGroupByCategory.forEach((k, v) -> {
            System.out.println("Category: " + k);
            v.forEach(System.out::println);
        });

        // collect - toMap: trả về Map<K, V>
        Map<Integer, Product> productsMappingTable = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity())); // nếu không dùng Function.identity() -> p -> p;
        
        productsMappingTable.forEach((k, v) -> System.out.println(k + " " + v));
        
        // collect - toMap: nhiều tham số 
        Map<Integer, String> productName = products.stream()
                .collect(Collectors.toMap(
                    Product::getId, Product::getName,
                    (p1, p2) -> p1, // lọc trùng, chỉ giữ cái giá trị thứ nhất 
                    TreeMap::new // kiểu mong muốn trả về treemap 
                ));
        productName.forEach((k, v) -> System.out.println(k + " " + v));
    }
}

// thao tác kết thúc: kết thúc stream pipeline, kích hoạt xử lí và trả về kết
// quả cuối cùng (không phải stream)