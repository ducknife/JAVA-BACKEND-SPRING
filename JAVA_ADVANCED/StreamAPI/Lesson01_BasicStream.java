package JAVA_ADVANCED.StreamAPI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * =====================================================
 * BÀI 1: GIỚI THIỆU STREAM - CÁCH TẠO STREAM
 * =====================================================
 * 
 * Stream là một "dòng chảy" dữ liệu mà bạn có thể xử lý tuần tự.
 * 
 * ĐẶC ĐIỂM QUAN TRỌNG:
 * 1. Stream KHÔNG lưu trữ dữ liệu (nó chỉ là "ống dẫn")
 * 2. Stream KHÔNG thay đổi source gốc
 * 3. Stream chỉ dùng được MỘT LẦN
 * 4. Stream operations là LAZY (lười) - chỉ chạy khi cần
 */
public class Lesson01_BasicStream {

    public static void main(String[] args) {
        
        System.out.println("========== CÁCH 1: Tạo Stream từ Collection ==========");
        List<String> names = Arrays.asList("An", "Bình", "Cường", "Dũng");
        
        // Tạo stream từ List
        Stream<String> streamFromList = names.stream();
        
        // In ra từng phần tử (forEach là Terminal Operation)
        streamFromList.forEach(name -> System.out.println("Tên: " + name));
        
        
        System.out.println("\n========== CÁCH 2: Tạo Stream từ Array ==========");
        String[] fruits = {"Táo", "Cam", "Xoài", "Nho"};
        
        // Tạo stream từ Array
        Stream<String> streamFromArray = Arrays.stream(fruits);
        streamFromArray.forEach(fruit -> System.out.println("Quả: " + fruit));
        
        
        System.out.println("\n========== CÁCH 3: Tạo Stream bằng Stream.of() ==========");
        Stream<Integer> numberStream = Stream.of(1, 2, 3, 4, 5);
        numberStream.forEach(n -> System.out.println("Số: " + n));
        
        
        System.out.println("\n========== CÁCH 4: Stream.generate() - Tạo vô hạn ==========");
        // Tạo 5 số ngẫu nhiên
        Stream.generate(() -> Math.random())
              .limit(5)  // Giới hạn 5 phần tử
              .forEach(random -> System.out.println("Random: " + random));
        
        
        System.out.println("\n========== CÁCH 5: Stream.iterate() - Tạo theo quy luật ==========");
        // Tạo dãy số: 0, 2, 4, 6, 8 (bắt đầu từ 0, mỗi lần +2)
        Stream.iterate(0, n -> n + 2)
              .limit(5)
              .forEach(n -> System.out.println("Số chẵn: " + n));
        
        
        System.out.println("\n========== LƯU Ý: Stream chỉ dùng được 1 lần! ==========");
        Stream<String> stream = Stream.of("A", "B", "C");
        stream.forEach(System.out::println);  // OK
        
        // Nếu uncomment dòng dưới sẽ bị lỗi IllegalStateException
        // stream.forEach(System.out::println);  // ERROR: stream has already been operated upon
        
        System.out.println("\n✅ Bài 1 hoàn thành! Chạy Lesson02 để học tiếp.");
    }
}
