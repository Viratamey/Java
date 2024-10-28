package lambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public class FunctionsAndComparator {

    public static void main(String[] args) {
        List<String> names = Arrays.asList("john", "wick", "dan", "trevor", "ray", "nick");

        Comparator<String> cmp1 = (s1, s2) -> s1.compareTo(s2);
//        names.sort(cmp1);
//        System.out.println(names);
//        Comparator<String> cmp2 = cmp1.reversed();
//        names.sort(cmp2);
//        System.out.println(names);

        List<User> users = new ArrayList<>();
        users.addAll(Arrays.asList(
                new User("nick", 12),
                new User("wick", 34),
                new User("dan", 75),
                new User("trevor", 5),
                new User("ray", 67),
                new User("john", 12)
        ));

        Function<User, Integer> namelenfn = (user) -> user.getName().length();
        Comparator cmp3= Comparator.comparing(namelenfn);
//        users.sort(cmp3);
//        System.out.println(users);


        // These comparator function do autoBoxing of int to Integer
        // so slowing the comparison and sorting
        // to make it fast use ToIntFunction used below
        Function<User, Integer> agefn = (user) -> user.getAge();
        Comparator cmp4 = Comparator.comparing(agefn);
//        users.sort(cmp4);
//        System.out.println(users);

        //Sort by name first if name length is same then by age
        Comparator cmp5 = cmp3.thenComparing(cmp4);
//        users.sort(cmp5);
//        System.out.println(users);

        ToIntFunction<User> agefnint = (user) -> user.getAge();
        Function<User, String> namefn = (user) -> user.getName();
        Comparator cmp6 = Comparator.comparingInt(agefnint);
        Comparator cmp7 = Comparator.comparing(namefn);
        Comparator cmp8 = cmp3.thenComparing(cmp6).thenComparing(cmp7);
        users.sort(cmp8);
        System.out.println(users);

    }
}
