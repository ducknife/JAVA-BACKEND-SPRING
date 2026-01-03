package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class B9 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            Map<String, Double> avgPriceByCategory = products.stream() 
                    .collect(Collectors.groupingBy(
                        Product::getCategory, 
                        Collectors.averagingDouble(Product::getPrice)
                    ));
            avgPriceByCategory.forEach((k, v) -> {
                System.out.println(k + " " + v);
            });
        }
    }
}

// Bài 9: Giá trung bình theo category
// > Tính giá trung bình của sản phẩm trong mỗi category
