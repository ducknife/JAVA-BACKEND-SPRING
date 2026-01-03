package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

public class B6 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            
            // Cách 1: reduce 
            double sum1 = products.stream()
                    .map(p -> p.getPrice() * p.getQuantity())
                    .reduce(0.0, (a, b) -> a + b);
            System.out.println(sum1);

            // Cách 2: mapToDouble 
            double sum2 = products.stream()
                    .mapToDouble(p -> p.getPrice() * p.getQuantity())
                    .sum();
            System.out.println(sum2);
        }
    }
}

// Bài 6: Tổng giá trị tồn kho
// > Tính tổng giá trị tồn kho (giá × số lượng) của TẤT CẢ sản phẩm
