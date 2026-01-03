package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class B7 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            Map<String, List<Product>> productGroupByCategory = products.stream()
                    .collect(Collectors.groupingBy(Product::getCategory));
            productGroupByCategory.forEach((k, v) -> {
                System.out.println("Category: " + k);
                v.forEach(p -> System.out.println(p));
            });
        }
    }
}

// Bài 7: Nhóm sản phẩm theo category
// > Nhóm tất cả sản phẩm theo category