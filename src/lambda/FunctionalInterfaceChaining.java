package lambda;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FunctionalInterfaceChaining {
    public static void main(String[] a) {

//        Consumer
        Consumer<String> c1 = (String s) -> System.out.println(s);
        Consumer<String> c2 = (String s) -> System.out.println(s.length());

        Consumer<String> c3 = (s) -> {
            c1.accept(s);
            c2.accept(s);
        };
        c3.accept("Hello");

        Consumer<String> c4 = c1.andThen(c2);
        c4.accept("Hello");


//        Predicate
        Predicate<String> isNotNull = (s) -> !(s == null);
        Predicate<String> isNotEmpty = (s) -> !s.isEmpty();
        Predicate<String> notnullempty = isNotNull.and(isNotEmpty);
        System.out.println(notnullempty.test("Hello"));
        System.out.println(notnullempty.test(""));

        Predicate<String> isNull = (s) -> (s == null);
        Predicate<String> isEmpty = (s) -> s.isEmpty();
        notnullempty = isNull.negate().and(isEmpty.negate());
        System.out.println(notnullempty.test("Hello"));
        System.out.println(notnullempty.test(""));

    }
}
