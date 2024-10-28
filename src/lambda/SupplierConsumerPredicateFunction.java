package lambda;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SupplierConsumerPredicateFunction {

    public static void main(String[] ar) {
        Supplier<String> supplier = () -> "new supplier";
        String str = supplier.get();
        System.out.println(str);

        Supplier<String> supplier2 = () -> null;
        String str2 = supplier2.get();
        System.out.println(str2);

        Consumer<String> consumer = (s) -> System.out.println(s);
        consumer.accept("Hello Amey");
        consumer.accept(null);

        Predicate<String> isEmptyString = s -> s.isEmpty();
        boolean isStrEmpty = isEmptyString.test("Hello");
        boolean isStrEmpty2 = isEmptyString.test("");
        System.out.println(isStrEmpty);
        System.out.println(isStrEmpty2);

//      null pointer exception if null is passed
//      boolean isStrEmpty3 = isEmptyString.test(null);
//      System.out.println(isStrEmpty3);

        Function<String, Integer> fn = (s) -> s.length();
        System.out.println(fn.apply("hello"));

    }

}
