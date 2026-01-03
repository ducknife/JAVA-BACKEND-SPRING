package JAVA_ADVANCED.StreamAPI.Practice.BaiTapLuyenTap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class B19 {
    public static void main(String[] args) throws Exception {
        Order orderOne = new Order();
        orderOne.add(new Product(1, "iPhone 15", "Điện thoại", 25000000, 50, true));
        orderOne.add(new Product(2, "iPhone 16", "Điện thoại", 30000000, 50, true));
        Order orderTwo = new Order();
        orderTwo.add(new Product(1, "iPhone 15", "Điện thoại", 25000000, 50, true));
        orderTwo.add(new Product(3, "iPhone 17", "Điện thoại", 50000000, 50, true));
        Order orderThree = new Order();
        orderThree.add(new Product(1, "iPhone 15", "Điện thoại", 25000000, 50, true));
        orderThree.add(new Product(2, "iPhone 16", "Điện thoại", 30000000, 50, true));
        orderThree.add(new Product(4, "iPhone 12", "Điện thoại", 15000000, 50, true));
        List<Product> o1 = orderOne.getProducts();
        List<Product> o2 = orderTwo.getProducts();
        List<Product> o3 = orderThree.getProducts();
        List<List<Product>> allProducts = Arrays.asList(o1, o2, o3);
        
        // use flatMap 
        // distinct dùng hashCode và Equals để xác định 2 product khác nhau 
        List<Product> uniqueInThreeOrders = allProducts.stream() 
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        uniqueInThreeOrders.forEach(System.out::println);
    }
}

class Order {
    private List<Product> products = new ArrayList<>();

    public Order() {
    }

    public void add(Product p) {
        this.products.add(p);
    }

    public Product take(int i) {
        return this.products.get(i);
    }

    public List<Product> getProducts() {
        return products;
    }
}

// Bài 19: FlatMap - Làm phẳng danh sách
// > Cho danh sách các đơn hàng, mỗi đơn có nhiều sản phẩm
// > Lấy tất cả sản phẩm unique từ tất cả đơn hàng