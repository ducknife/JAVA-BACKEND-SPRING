# 📝 BÀI TẬP THỰC HÀNH STREAM API

## 📦 Dữ liệu mẫu

```java
List<Product> products = Arrays.asList(
    new Product(1, "iPhone 15", "Điện thoại", 25000000, 50, true),
    new Product(2, "Samsung S24", "Điện thoại", 22000000, 30, true),
    new Product(3, "MacBook Pro", "Laptop", 45000000, 20, true),
    new Product(4, "Dell XPS 15", "Laptop", 35000000, 0, false),
    new Product(5, "iPad Pro", "Tablet", 28000000, 15, true),
    new Product(6, "AirPods Pro", "Phụ kiện", 5000000, 100, true),
    new Product(7, "Galaxy Buds", "Phụ kiện", 3000000, 80, true),
    new Product(8, "Xiaomi 14", "Điện thoại", 18000000, 45, true),
    new Product(9, "Asus ROG", "Laptop", 55000000, 10, true),
    new Product(10, "Apple Watch", "Phụ kiện", 12000000, 0, false)
);
```

---

## 🟢 LEVEL 1: CƠ BẢN

### Bài 1: Lọc sản phẩm còn hàng
> Lấy danh sách tất cả sản phẩm còn hàng (inStock = true)

<details>
<summary>💡 Lời giải</summary>

```java
List<Product> inStockProducts = products.stream()
        .filter(Product::isInStock)
        .collect(Collectors.toList());

inStockProducts.forEach(System.out::println);
```
</details>

---

### Bài 2: Lấy tên sản phẩm
> Lấy danh sách TÊN của tất cả sản phẩm

<details>
<summary>💡 Lời giải</summary>

```java
List<String> names = products.stream()
        .map(Product::getName)
        .collect(Collectors.toList());

System.out.println(names);
// [iPhone 15, Samsung S24, MacBook Pro, ...]
```
</details>

---

### Bài 3: Đếm sản phẩm theo category
> Đếm có bao nhiêu sản phẩm thuộc category "Điện thoại"

<details>
<summary>💡 Lời giải</summary>

```java
long count = products.stream()
        .filter(p -> p.getCategory().equals("Điện thoại"))
        .count();

System.out.println("Số điện thoại: " + count); // 3
```
</details>

---

### Bài 4: Sắp xếp theo giá
> Sắp xếp sản phẩm theo giá từ thấp đến cao

<details>
<summary>💡 Lời giải</summary>

```java
List<Product> sortedByPrice = products.stream()
        .sorted(Comparator.comparing(Product::getPrice))
        .collect(Collectors.toList());

sortedByPrice.forEach(p -> 
    System.out.println(p.getName() + " - " + p.getPrice()));
```
</details>

---

### Bài 5: Top 3 sản phẩm đắt nhất
> Lấy 3 sản phẩm có giá cao nhất

<details>
<summary>💡 Lời giải</summary>

```java
List<Product> top3Expensive = products.stream()
        .sorted(Comparator.comparing(Product::getPrice).reversed())
        .limit(3)
        .collect(Collectors.toList());

top3Expensive.forEach(p -> 
    System.out.println(p.getName() + " - " + p.getPrice()));
// Asus ROG - 55,000,000
// MacBook Pro - 45,000,000
// Dell XPS 15 - 35,000,000
```
</details>

---

## 🟡 LEVEL 2: TRUNG BÌNH

### Bài 6: Tổng giá trị tồn kho
> Tính tổng giá trị tồn kho (giá × số lượng) của TẤT CẢ sản phẩm

<details>
<summary>💡 Lời giải</summary>

```java
double totalValue = products.stream()
        .mapToDouble(p -> p.getPrice() * p.getStock())
        .sum();

System.out.println("Tổng giá trị: " + String.format("%,.0f VNĐ", totalValue));
```
</details>

---

### Bài 7: Nhóm sản phẩm theo category
> Nhóm tất cả sản phẩm theo category

<details>
<summary>💡 Lời giải</summary>

```java
Map<String, List<Product>> byCategory = products.stream()
        .collect(Collectors.groupingBy(Product::getCategory));

byCategory.forEach((category, prods) -> {
    System.out.println("\n📁 " + category + ":");
    prods.forEach(p -> System.out.println("   - " + p.getName()));
});
```
</details>

---

### Bài 8: Đếm số lượng mỗi category
> Đếm có bao nhiêu sản phẩm trong mỗi category

<details>
<summary>💡 Lời giải</summary>

```java
Map<String, Long> countByCategory = products.stream()
        .collect(Collectors.groupingBy(
            Product::getCategory,
            Collectors.counting()
        ));

countByCategory.forEach((cat, count) -> 
    System.out.println(cat + ": " + count + " sản phẩm"));
// Điện thoại: 3 sản phẩm
// Laptop: 3 sản phẩm
// ...
```
</details>

---

### Bài 9: Giá trung bình theo category
> Tính giá trung bình của sản phẩm trong mỗi category

<details>
<summary>💡 Lời giải</summary>

```java
Map<String, Double> avgPriceByCategory = products.stream()
        .collect(Collectors.groupingBy(
            Product::getCategory,
            Collectors.averagingDouble(Product::getPrice)
        ));

avgPriceByCategory.forEach((cat, avg) -> 
    System.out.println(cat + ": " + String.format("%,.0f VNĐ", avg)));
```
</details>

---

### Bài 10: Sản phẩm đắt nhất mỗi category
> Tìm sản phẩm có giá cao nhất trong mỗi category

<details>
<summary>💡 Lời giải</summary>

```java
Map<String, Optional<Product>> maxByCategory = products.stream()
        .collect(Collectors.groupingBy(
            Product::getCategory,
            Collectors.maxBy(Comparator.comparing(Product::getPrice))
        ));

maxByCategory.forEach((cat, optProduct) -> 
    optProduct.ifPresent(p -> 
        System.out.println(cat + ": " + p.getName() + " - " + p.getPrice())));

// Cách 2: Dùng toMap với merge function
Map<String, Product> maxByCategory2 = products.stream()
        .collect(Collectors.toMap(
            Product::getCategory,
            Function.identity(),
            (p1, p2) -> p1.getPrice() > p2.getPrice() ? p1 : p2
        ));
```
</details>

---

### Bài 11: Kiểm tra điều kiện
> a) Có sản phẩm nào giá > 50 triệu không?
> b) Tất cả sản phẩm đều có tên không?
> c) Không có sản phẩm nào giá âm?

<details>
<summary>💡 Lời giải</summary>

```java
// a) anyMatch
boolean hasOver50M = products.stream()
        .anyMatch(p -> p.getPrice() > 50000000);
System.out.println("Có SP > 50 triệu: " + hasOver50M); // true

// b) allMatch
boolean allHaveName = products.stream()
        .allMatch(p -> p.getName() != null && !p.getName().isEmpty());
System.out.println("Tất cả có tên: " + allHaveName); // true

// c) noneMatch
boolean noNegativePrice = products.stream()
        .noneMatch(p -> p.getPrice() < 0);
System.out.println("Không có giá âm: " + noNegativePrice); // true
```
</details>

---

### Bài 12: Tạo Map từ id → Product
> Tạo Map với key là id, value là Product object

<details>
<summary>💡 Lời giải</summary>

```java
Map<Integer, Product> productById = products.stream()
        .collect(Collectors.toMap(
            Product::getId,
            Function.identity()
        ));

// Truy xuất nhanh
Product iphone = productById.get(1);
System.out.println(iphone.getName()); // iPhone 15
```
</details>

---

## 🔴 LEVEL 3: NÂNG CAO

### Bài 13: Thống kê tổng hợp theo category
> Với mỗi category, in ra: số lượng SP, tổng giá, giá TB, giá min, giá max

<details>
<summary>💡 Lời giải</summary>

```java
Map<String, DoubleSummaryStatistics> statsByCategory = products.stream()
        .collect(Collectors.groupingBy(
            Product::getCategory,
            Collectors.summarizingDouble(Product::getPrice)
        ));

statsByCategory.forEach((category, stats) -> {
    System.out.println("\n📊 " + category);
    System.out.println("   Số SP: " + stats.getCount());
    System.out.println("   Tổng: " + String.format("%,.0f", stats.getSum()));
    System.out.println("   TB: " + String.format("%,.0f", stats.getAverage()));
    System.out.println("   Min: " + String.format("%,.0f", stats.getMin()));
    System.out.println("   Max: " + String.format("%,.0f", stats.getMax()));
});
```
</details>

---

### Bài 14: Phân loại sản phẩm theo mức giá
> Phân loại thành 3 nhóm:
> - "Rẻ": < 10 triệu
> - "Trung bình": 10-30 triệu
> - "Cao cấp": > 30 triệu

<details>
<summary>💡 Lời giải</summary>

```java
Map<String, List<Product>> byPriceRange = products.stream()
        .collect(Collectors.groupingBy(p -> {
            if (p.getPrice() < 10000000) return "Rẻ";
            else if (p.getPrice() <= 30000000) return "Trung bình";
            else return "Cao cấp";
        }));

byPriceRange.forEach((range, prods) -> {
    System.out.println("\n💰 " + range + ":");
    prods.forEach(p -> System.out.println("   - " + p.getName() + ": " + p.getPrice()));
});
```
</details>

---

### Bài 15: Tìm sản phẩm theo điều kiện phức hợp
> Tìm sản phẩm: còn hàng, giá 15-30 triệu, category là "Điện thoại" hoặc "Tablet"
> Sắp xếp theo giá giảm dần, lấy top 2

<details>
<summary>💡 Lời giải</summary>

```java
List<Product> result = products.stream()
        .filter(Product::isInStock)
        .filter(p -> p.getPrice() >= 15000000 && p.getPrice() <= 30000000)
        .filter(p -> p.getCategory().equals("Điện thoại") 
                  || p.getCategory().equals("Tablet"))
        .sorted(Comparator.comparing(Product::getPrice).reversed())
        .limit(2)
        .collect(Collectors.toList());

result.forEach(p -> 
    System.out.println(p.getName() + " - " + p.getCategory() + " - " + p.getPrice()));
// iPad Pro - Tablet - 28,000,000
// iPhone 15 - Điện thoại - 25,000,000
```
</details>

---

### Bài 16: Tính doanh thu dự kiến theo category
> Giả sử bán hết tất cả sản phẩm còn hàng, tính tổng doanh thu mỗi category

<details>
<summary>💡 Lời giải</summary>

```java
Map<String, Double> revenueByCategory = products.stream()
        .filter(Product::isInStock)
        .collect(Collectors.groupingBy(
            Product::getCategory,
            Collectors.summingDouble(p -> p.getPrice() * p.getStock())
        ));

revenueByCategory.forEach((cat, revenue) -> 
    System.out.println(cat + ": " + String.format("%,.0f VNĐ", revenue)));
```
</details>

---

### Bài 17: Tạo báo cáo dạng String
> Tạo chuỗi báo cáo: "SP1 (Category1), SP2 (Category2), ..."

<details>
<summary>💡 Lời giải</summary>

```java
String report = products.stream()
        .map(p -> p.getName() + " (" + p.getCategory() + ")")
        .collect(Collectors.joining(", "));

System.out.println(report);
// iPhone 15 (Điện thoại), Samsung S24 (Điện thoại), MacBook Pro (Laptop), ...
```
</details>

---

### Bài 18: Partition - Chia 2 nhóm
> Chia sản phẩm thành 2 nhóm: còn hàng và hết hàng

<details>
<summary>💡 Lời giải</summary>

```java
Map<Boolean, List<Product>> partitioned = products.stream()
        .collect(Collectors.partitioningBy(Product::isInStock));

System.out.println("✅ Còn hàng:");
partitioned.get(true).forEach(p -> System.out.println("   - " + p.getName()));

System.out.println("❌ Hết hàng:");
partitioned.get(false).forEach(p -> System.out.println("   - " + p.getName()));
```
</details>

---

### Bài 19: FlatMap - Làm phẳng danh sách
> Cho danh sách các đơn hàng, mỗi đơn có nhiều sản phẩm
> Lấy tất cả sản phẩm unique từ tất cả đơn hàng

<details>
<summary>💡 Lời giải</summary>

```java
// Giả sử có class Order
class Order {
    List<Product> products;
    // getter...
}

List<Order> orders = ...;

List<Product> allProducts = orders.stream()
        .flatMap(order -> order.getProducts().stream())
        .distinct()
        .collect(Collectors.toList());

// Ví dụ đơn giản với List<List<Integer>>
List<List<Integer>> listOfLists = Arrays.asList(
    Arrays.asList(1, 2, 3),
    Arrays.asList(4, 5),
    Arrays.asList(6, 7, 8, 9)
);

List<Integer> flattened = listOfLists.stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
// [1, 2, 3, 4, 5, 6, 7, 8, 9]
```
</details>

---

### Bài 20: Bài toán tổng hợp - Dashboard
> Tạo dashboard hiển thị:
> 1. Tổng số sản phẩm
> 2. Số sản phẩm còn hàng / hết hàng
> 3. Tổng giá trị tồn kho
> 4. Category có nhiều SP nhất
> 5. Sản phẩm bán chạy nhất (stock cao nhất)

<details>
<summary>💡 Lời giải</summary>

```java
System.out.println("========== 📊 DASHBOARD ==========\n");

// 1. Tổng số sản phẩm
long totalProducts = products.size();
System.out.println("1. Tổng SP: " + totalProducts);

// 2. Còn hàng / Hết hàng
Map<Boolean, Long> stockStatus = products.stream()
        .collect(Collectors.partitioningBy(
            Product::isInStock,
            Collectors.counting()
        ));
System.out.println("2. Còn hàng: " + stockStatus.get(true) 
                 + " | Hết hàng: " + stockStatus.get(false));

// 3. Tổng giá trị tồn kho
double totalValue = products.stream()
        .mapToDouble(p -> p.getPrice() * p.getStock())
        .sum();
System.out.println("3. Tổng giá trị kho: " + String.format("%,.0f VNĐ", totalValue));

// 4. Category có nhiều SP nhất
String topCategory = products.stream()
        .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()))
        .entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse("N/A");
System.out.println("4. Category lớn nhất: " + topCategory);

// 5. Sản phẩm có stock cao nhất
products.stream()
        .max(Comparator.comparing(Product::getStock))
        .ifPresent(p -> System.out.println("5. Stock cao nhất: " 
                      + p.getName() + " (" + p.getStock() + " cái)"));
```
</details>

---

## ✅ CHECKLIST HOÀN THÀNH

- [ ] Level 1: Bài 1-5 (Cơ bản)
- [ ] Level 2: Bài 6-12 (Trung bình)
- [ ] Level 3: Bài 13-20 (Nâng cao)

---

## 💡 TIPS

1. **Debug:** Dùng `.peek()` để xem giá trị giữa các operations
   ```java
   products.stream()
       .filter(p -> p.getPrice() > 20000000)
       .peek(p -> System.out.println("After filter: " + p.getName()))
       .map(Product::getName)
       .collect(Collectors.toList());
   ```

2. **Readable code:** Tách Predicate/Function ra biến riêng
   ```java
   Predicate<Product> isExpensive = p -> p.getPrice() > 30000000;
   Predicate<Product> isInStock = Product::isInStock;
   
   products.stream()
       .filter(isExpensive.and(isInStock))
       .collect(Collectors.toList());
   ```

3. **Method Reference:** Ưu tiên dùng khi có thể
   ```java
   .map(p -> p.getName())  // Lambda
   .map(Product::getName)  // Method Reference ✅
   ```

---

**Sau khi hoàn thành, bạn đã sẵn sàng cho Phase 2: Spring Boot!** 🚀
