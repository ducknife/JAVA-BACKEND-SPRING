package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class B15 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            List<Product> filteredProducts = products.stream() 
                    .filter(Product::isInStock)
                    .filter(p -> p.getPrice() >= 15000000 && p.getPrice() <= 30000000)
                    .filter(p -> p.getCategory().equals("Điện thoại") || p.getCategory().equals("Tablet"))
                    .sorted(Comparator.comparing(Product::getPrice).reversed())
                    .limit(2)
                    .collect(Collectors.toList());
            filteredProducts.forEach((p) -> System.out.println(p));
        }
    }
}

// Bài 15: Tìm sản phẩm theo điều kiện phức hợp
// > Tìm sản phẩm: còn hàng, giá 15-30 triệu, category là "Điện thoại" hoặc "Tablet"
// > Sắp xếp theo giá giảm dần, lấy top 2