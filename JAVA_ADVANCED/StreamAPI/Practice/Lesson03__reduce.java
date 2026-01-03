package JAVA_ADVANCED.StreamAPI.Practice;

import java.util.List;
import java.util.Optional;

public class Lesson03__reduce {
    public static void main(String[] args) {
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6);
        Integer SUM = nums.stream()
                .reduce(0, (a, b) -> a + b);
        System.out.println(SUM);

        Integer PRODUCT = nums.stream() 
                .reduce(1, (a, b) -> a * b);
        System.out.println(PRODUCT);

        Optional<Integer> MAX = nums.stream() // Optional có thể chứa giá trị hoặc null 
                .reduce(Integer::max);

        Optional<Integer> MIN = nums.stream()
                .reduce(Integer::min);
 
        MAX.ifPresent(n -> System.out.println(n)); // ifPresent: nếu trong optional có giá trị 
        MIN.ifPresent(n -> System.out.println(n));

        
    }
}
