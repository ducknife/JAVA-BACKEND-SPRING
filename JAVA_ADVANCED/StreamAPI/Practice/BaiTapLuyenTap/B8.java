package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class B8 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            Map<String, Long> numberProductByCategory = products.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));
            numberProductByCategory.forEach((k, v) -> System.out.println(k + " " + v));
        }
    }
}

// Bài 8: Đếm số lượng mỗi category
// > Đếm có bao nhiêu sản phẩm trong mỗi category