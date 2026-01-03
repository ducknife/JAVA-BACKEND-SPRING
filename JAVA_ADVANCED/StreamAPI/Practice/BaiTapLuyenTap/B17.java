package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.stream.Collectors;

public class B17 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            String report = products.stream()
                    .map(p -> p.getName() + " (" + p.getCategory() + ")")
                    .collect(Collectors.joining(", "));
            System.out.println(report);
        }
    }
}


// Bài 17: Tạo báo cáo dạng String
// > Tạo chuỗi báo cáo: "SP1 (Category1), SP2 (Category2), ..."