package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

public class write {
    public static void main(String[] args) throws Exception {
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
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("PRODUCT.in"))) {
            oos.writeObject(products);
        }
    }
}
