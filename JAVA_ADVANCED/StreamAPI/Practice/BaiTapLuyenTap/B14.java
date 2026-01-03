package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class B14 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            Map<String, List<Product>> byPrice = products.stream()
                    .collect(Collectors.groupingBy(
                        p -> {
                            if (p.getPrice() < 10000000) return "Rẻ";
                            else if (p.getPrice() <= 30000000) return "Trung bình";
                            else return "Cao cấp";
                        }
                    ));
            byPrice.forEach((k, v) -> {
                System.out.println("Loại " + k + ":");
                v.forEach(p -> System.out.println(p.getName()));
            });
        }
    }
}

// Bài 14: Phân loại sản phẩm theo mức giá
// > Phân loại thành 3 nhóm:
// > - "Rẻ": < 10 triệu
// > - "Trung bình": 10-30 triệu
// > - "Cao cấp": > 30 triệu