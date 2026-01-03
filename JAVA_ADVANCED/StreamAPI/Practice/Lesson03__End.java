package JAVA_ADVANCED.StreamAPI.Practice;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class Lesson03__End {
    public static void main(String[] args) {
        List<Integer> nums = List.of(1, 2, 2, 3, 3, 5, 6, 7);
        
        // count(): đếm -> trả về long 
        long countNumberGreaterThanOne = nums.stream()
                .filter(n -> n > 1)
                .distinct()
                .count();
        System.out.println(countNumberGreaterThanOne);

        // min & max
        Optional<Integer> Max = nums.stream().max(Comparator.naturalOrder());
        Optional<Integer> Min = nums.stream().min(Comparator.naturalOrder());
        Max.ifPresent(n -> System.out.println(n));
        Min.ifPresent(n -> System.out.println(n));

        // findFirst & findAny : trả về đầu tiên và trả về bất kì 
        Optional<Integer> firstNumberGreaterThanTwo = nums.stream().filter(n -> n > 2).findFirst();
        Optional<Integer> anyNumberGreaterThanTwo = nums.stream().filter(n -> n > 8).findAny();
        firstNumberGreaterThanTwo.ifPresentOrElse(n -> System.out.println(n),() -> System.out.println("Not Found"));
        anyNumberGreaterThanTwo.ifPresentOrElse(n -> System.out.println(n), () -> System.out.println("Not Found"));

        // allMatch & anyMatch & noneMatch: trả về boolean nếu đúng 
        boolean hasAllNumberGreaterThanThree = nums.stream().allMatch(n -> n > 3);
        boolean hasAnyNumberGreaterThanThree = nums.stream().anyMatch(n -> n > 3);
        boolean hasNoNumberGreaterThanEight = nums.stream().noneMatch(n -> n > 8);
        System.out.println(hasAllNumberGreaterThanThree);
        System.out.println(hasAnyNumberGreaterThanThree);
        System.out.println(hasNoNumberGreaterThanEight);

        // mapToInt, ... : chuyển Stream<T> về IntStream, ... nhanh hơn, nhiều công dụng tích hợp hơn
        int sum = nums.stream().mapToInt(n -> n).sum();
        OptionalInt min = nums.stream().mapToInt(n -> n).min();
        OptionalInt max = nums.stream().mapToInt(n -> n).max();
        OptionalDouble avg = nums.stream().mapToDouble(n -> n).average();
        System.out.println(sum);
        min.ifPresentOrElse(n -> System.out.println(n), () -> System.out.println("Not Found"));
        max.ifPresentOrElse(n -> System.out.println(n), () -> System.out.println("Not Found"));
        avg.ifPresentOrElse(n -> System.out.printf("%.2f", n), () -> System.out.println("Not Found"));
    }
}
