# 📚 Stream API - Hướng Dẫn Học

## 🎯 Mục Tiêu

Sau khi hoàn thành module này, bạn sẽ:
- Hiểu Stream là gì và tại sao cần dùng
- Biết cách tạo Stream từ nhiều nguồn
- Thành thạo Intermediate Operations (filter, map, sorted,...)
- Thành thạo Terminal Operations (collect, reduce, count,...)
- Áp dụng vào bài toán thực tế

---

## 📖 Thứ Tự Học

| STT | File | Nội dung |
|-----|------|----------|
| 1 | `Lesson01_BasicStream.java` | Giới thiệu Stream, cách tạo Stream |
| 2 | `Lesson02_IntermediateOps.java` | filter, map, sorted, distinct, limit, skip |
| 3 | `Lesson03_TerminalOps.java` | collect, reduce, count, find, match |
| 4 | `Lesson04_Practice.java` | Bài tập thực hành tổng hợp |

---

## 🚀 Cách Chạy

### Cách 1: Dùng Terminal

```bash
# Di chuyển vào thư mục OOP
cd "d:\Downloads\JAVA SPRING BOOT\OOP"

# Compile
javac StreamAPI/*.java

# Chạy từng bài
java StreamAPI.Lesson01_BasicStream
java StreamAPI.Lesson02_IntermediateOps
java StreamAPI.Lesson03_TerminalOps
java StreamAPI.Lesson04_Practice
```

### Cách 2: Dùng VS Code
- Mở file Java cần chạy
- Click nút "Run" ở góc trên phải
- Hoặc nhấn `Ctrl + F5`

---

## 📝 Tóm Tắt Kiến Thức

### Stream Pipeline

```
Source (Collection/Array)
    ↓
Intermediate Operations (filter, map, sorted...) ← LAZY, trả về Stream
    ↓
Terminal Operation (collect, forEach, count...) ← Kích hoạt xử lý
    ↓
Result
```

### Intermediate Operations Thường Dùng

| Method | Mô tả | Ví dụ |
|--------|-------|-------|
| `filter(Predicate)` | Lọc phần tử | `.filter(x -> x > 10)` |
| `map(Function)` | Biến đổi phần tử | `.map(x -> x * 2)` |
| `sorted()` | Sắp xếp | `.sorted()` |
| `distinct()` | Loại trùng | `.distinct()` |
| `limit(n)` | Lấy n phần tử | `.limit(5)` |
| `skip(n)` | Bỏ qua n phần tử | `.skip(2)` |

### Terminal Operations Thường Dùng

| Method | Mô tả | Trả về |
|--------|-------|--------|
| `collect()` | Thu thập kết quả | Collection |
| `forEach()` | Duyệt phần tử | void |
| `count()` | Đếm | long |
| `reduce()` | Gộp thành 1 giá trị | Optional/T |
| `findFirst()` | Tìm phần tử đầu | Optional |
| `anyMatch()` | Có thỏa mãn? | boolean |
| `min()/max()` | Tìm min/max | Optional |

### Collectors Thường Dùng

```java
Collectors.toList()                    // → List
Collectors.toSet()                     // → Set
Collectors.toMap(keyMapper, valueMapper) // → Map
Collectors.joining(", ")               // → String
Collectors.groupingBy(classifier)      // → Map<K, List<V>>
Collectors.counting()                  // → Long
Collectors.summingDouble(mapper)       // → Double
```

---

## 💡 Tips

1. **Stream chỉ dùng 1 lần** - Sau khi gọi terminal operation, stream "chết"
2. **LAZY evaluation** - Intermediate ops không chạy cho đến khi gặp terminal op
3. **Method Reference** - `p -> p.getName()` có thể viết `Product::getName`
4. **Parallel Stream** - Dùng `.parallelStream()` để xử lý song song

---

## ✅ Checklist Hoàn Thành

- [ ] Chạy Lesson01 và hiểu cách tạo Stream
- [ ] Chạy Lesson02 và thực hành filter, map, sorted
- [ ] Chạy Lesson03 và thực hành collect, reduce, match
- [ ] Hoàn thành tất cả bài tập trong Lesson04
- [ ] Tự viết thêm 2-3 bài tập tương tự

---

**Tiếp theo:** Sau khi hoàn thành Stream API, bạn đã sẵn sàng cho **Phase 2: Spring Boot**! 🚀
