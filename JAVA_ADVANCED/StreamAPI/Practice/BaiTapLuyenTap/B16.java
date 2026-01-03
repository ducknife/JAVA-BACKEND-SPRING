package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class B16 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            Map<String, Double> totalRevenueByCategory = products.stream()
                    .filter(Product::isInStock)
                    .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.summingDouble(p -> p.getPrice() * p.getQuantity())
                    ));
            totalRevenueByCategory.forEach((k, v) -> {
                System.out.println("Total revenue of " + k);
                System.out.println(String.format("%.2f", v));
            });
        }
    }
}

// Bài 16: Tính doanh thu dự kiến theo category
// > Giả sử bán hết tất cả sản phẩm còn hàng, tính tổng doanh thu mỗi category