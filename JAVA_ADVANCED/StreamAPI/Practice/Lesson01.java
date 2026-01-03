package JAVA_ADVANCED.StreamAPI.Practice;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Lesson01 {
    public static void main(String[] args) {
        // Tạo stream từ collection 
        List<String> arr = Arrays.asList("hello", "spring-boot");
        Stream<String> streamFromCollection = arr.stream();
        streamFromCollection.forEach(System.out::println);

        System.out.println(streamFromCollection);

        // Tạo stream từ array 
        String[] arr1 = {"hello", "spring-boot"};
        Stream<String> streamFromArray = Arrays.stream(arr1);
        streamFromArray.forEach(System.out::println);

        // Tạo stream bằng Stream.of()
        Stream<Integer> numbers = Stream.of(1, 2, 3, 4, 5);
        numbers.forEach(System.out::println);

        // Tao bằng generate dùng Math.random()
        Stream.generate(() -> Math.random()).limit(5).forEach(System.out::println);

        // Tạo bằng iterate 
        Stream.iterate(0, i -> i + 2)
        .limit(5)
        .forEach(System.out::println);
    }
}


//Source (collection / array) -> Thao tác trung gian -> Thao tác kết thúc (kích hoạt) -> kết quả 
//Stream chỉ dùng được 1 lần 