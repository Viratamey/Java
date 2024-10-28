package mapFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.of;

public class MAP {
    public static void main(String[] args) {
        List<Person> persons = Arrays.asList(
                new Person(12),
                new Person(23),
                new Person(56),
                new Person(7),
                new Person(11),
                new Person(45),
                new Person(34),
                new Person(20),
                new Person(2));

        // get average of ersons whose age is greater than 20
        Stream<Person> personsStream = persons.stream();
//        Stream<Integer> intStream = personsStream.map(p -> p.getAge()).
//                filter(age -> age > 20);
//        System.out.println(intStream.count());

        personsStream.filter(p -> p.getAge() > 20).
                forEach(p -> System.out.println(p.getAge()));
    }
}
