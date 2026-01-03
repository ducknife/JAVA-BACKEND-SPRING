package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class B18 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            Map<String, List<Product>> partitionProduct = products.stream()
                    .collect(Collectors.groupingBy(p -> {
                        if (p.isInStock())
                            return "Còn hàng";
                        else
                            return "Hết hàng";
                    }));
            partitionProduct.forEach((k, v) -> {
                System.out.println(k + ":");
                v.forEach(p -> System.out.println(p.getName()));
            });

            // Cach 2:
            Map<Boolean, List<Product>> partitioned = products.stream()
                    .collect(Collectors.partitioningBy(Product::isInStock));
            System.out.println("Còn hàng");
            partitioned.get(true).forEach(p -> System.out.println(p.getName()));
            System.out.println("Hết hàng");
            partitioned.get(false).forEach(p -> System.out.println(p.getName()));
        }
    }
}

// Bài 18: Partition - Chia 2 nhóm
// > Chia sản phẩm thành 2 nhóm: còn hàng và hết hàng