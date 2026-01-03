package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class B12 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            Map<Integer, Product> mappingProduct = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
            mappingProduct.forEach((k, v) -> System.out.println(k + " " + v));
        }
    }
}


// Bài 12: Tạo Map từ id → Product
// > Tạo Map với key là id, value là Product object