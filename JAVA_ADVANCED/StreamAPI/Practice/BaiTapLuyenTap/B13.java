package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class B13 {
    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PRODUCT.in"))) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) ois.readObject();
            Map<String, DoubleSummaryStatistics> statistics = products.stream() 
                    .collect(Collectors.groupingBy(
                        Product::getCategory, 
                        Collectors.summarizingDouble(Product::getPrice)
                    ));
            statistics.forEach((k, v) -> {
                System.out.println("Category " + k + ":");
                System.out.println("So luong: " + v.getCount());
                System.out.println("Gia trung binh " + v.getAverage());
                System.out.println("Gia toi thieu " + v.getMin());
                System.out.println("Gia cao nhat " + v.getMax());
                System.out.println("Tong gia tri " + v.getSum());
            });
        }
    }
}


// Bài 13: Thống kê tổng hợp theo category
// > Với mỗi category, in ra: số lượng SP, tổng giá, giá TB, giá min, giá max