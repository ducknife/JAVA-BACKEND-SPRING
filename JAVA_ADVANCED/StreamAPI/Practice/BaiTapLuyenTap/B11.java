package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

public class B11 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            boolean anyProductHasPriceGreaterThan50M = products.stream().anyMatch(p -> p.getPrice() > 50e6);
            boolean allProductHasName = products.stream().allMatch(p -> p.getName() != null && !p.getName().isEmpty());
            boolean noProductHasNegativePrice = products.stream().noneMatch(p -> p.getPrice() < 0);
            System.out.println(anyProductHasPriceGreaterThan50M + " " + allProductHasName + " " + noProductHasNegativePrice);
        }
    }
}

// Bài 11: Kiểm tra điều kiện
// > a) Có sản phẩm nào giá > 50 triệu không?
// > b) Tất cả sản phẩm đều có tên không?
// > c) Không có sản phẩm nào giá âm?