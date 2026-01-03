package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class B10 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            Map<String, Optional<Product>> maxByCategory = products.stream()
                .collect(Collectors.groupingBy(
                    Product::getCategory,
                    Collectors.maxBy(Comparator.comparing(Product::getPrice))
                ));
            maxByCategory.forEach((k, v) -> {
                v.ifPresent(n -> System.out.println("Gia cao nhat cua " + k + " : " +  n.getName()));
            });
        }
    }
}

// Bài 10: Sản phẩm đắt nhất mỗi category
// > Tìm sản phẩm có giá cao nhất trong mỗi category