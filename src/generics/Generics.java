package generics;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Generics {
    public static <E> List<? extends E> mergeWildcard(List<? extends E> listOne, List<? extends E> listTwo) {
        return Stream.concat(listOne.stream(), listTwo.stream())
                .collect(Collectors.toList());
    }

    public static <E> List<E> mergeTypeParameter(List<? extends E> listOne, List<? extends E> listTwo) {
        return Stream.concat(listOne.stream(), listTwo.stream())
                .collect(Collectors.toList());
    }

    public static <E> List<E> mergeTypeParameterGenericArgs(List<E> listOne, List<E> listTwo) {
        return Stream.concat(listOne.stream(), listTwo.stream())
                .collect(Collectors.toList());
    }

    public static long sum(List<Number> numbers) {
        return numbers.stream().mapToLong(Number::longValue).sum();
    }

    public static <T extends Number> long sumWildCard(List<T> numbers) {
        return numbers.stream().mapToLong(Number::longValue).sum();
    }

    public static void addNumber(List<? super Integer> list, Integer number) {
        list.add(number);
    }

    // wont compile as wildcard
    public static void swap(List<?> list, int srcIndex, int destIndex) {
        // wont compile as in set compiler doesnt know the type of object
//        list.set(srcIndex, list.set(destIndex, list.get(srcIndex)));
    }

    private static <E> void swapHelper(List<E> list, int src, int des) {
        list.set(src, list.set(des, list.get(src)));
    }

//    public static <T extends Animal & Comparable<T>> void order(List<T> list) {
//        list.sort(Comparable::compareTo);
//    }



    public static void main(String[] args) {
        List<Number> numbers1 = new ArrayList<>();
        numbers1.add(5);
        numbers1.add(10L);

        List<Number> numbers2 = new ArrayList<>();
        numbers2.add(15f);
        numbers2.add(20.0);

        List<Integer> numbers3 = new ArrayList<>();
        numbers2.add(15f);
        numbers2.add(20.0);

        numbers1.addAll(numbers2);

        List<Number> numbersMergedCasted = (List<Number>) mergeWildcard(numbers1, numbers2);

        numbersMergedCasted = (List<Number>) mergeWildcard(numbers1, numbers3);

        // This will fail as Stream concat gives genetic type  instead of wildcard type
//        List<Number> numbersMerged = mergeWildcard(numbers1, numbers2);

        List<Number> numbersMergedTypeGenArgs = mergeTypeParameterGenericArgs(numbers1, numbers2);
//        numbersMergedTypeGenArgs = mergeTypeParameterGenericArgs(numbers1, numbers3);  // wont compile

        List<Number> numbersMerged = mergeTypeParameter(numbers1, numbers2);
        numbersMerged = mergeTypeParameter(numbers1, numbers3);


        //--------------------- Upper Bound ---------------------

        List<Number> numbers = new ArrayList<>();
        numbers.add(5);
        numbers.add(10L);
        numbers.add(15f);
        numbers.add(20.0);
        sum(numbers);

        List<Integer> integers = new ArrayList<>();
        integers.add(5);
        integers.add(10);
//        sum(integers); //wont compile as no oupper bound present in method

        // Solution with sumWildCard
        sumWildCard(integers);


        //--------------------- Upper Bound ---------------------

        addNumber(integers, 6);
        addNumber(numbers, 7);
        List<Float> floaters = new ArrayList<>();
        floaters.add(5.0f);
        floaters.add(10f);
//        addNumber(floaters, 7);  wont co,pile as Float is noy supertype of Integer
        List<Object> objects = new ArrayList<>();
        objects.add(5);
        objects.add(10);
        addNumber(objects, 9); // will work as Object is supertype of Integer

        //--------------------- Multiple Bounds ---------------------

        Cat cat1 = new Cat("white", "kitty");
        Cat cat2 = new Cat("white", "mini");
        Cat cat3 = new Cat("white", "charlotte");
        Dog dog1 = new Dog("black", "dogo");
        Dog dog2 = new Dog("black", "khandu");
        Dog dog3 = new Dog("black", "dagadu");
        List<Cat> cats = new ArrayList(Arrays.asList(cat1, cat2, cat3));
        List<Dog> dogs = new ArrayList(Arrays.asList(dog1, dog2, dog3));
        order(cats);
//        order(dogs); wont compile as dogs arent of comparable type as order demands <T extends Animal & Comparable<T>>
        System.out.println(cats);
    }

    public static <T extends Animal & Comparable<T>> void order(List<T> list) {
        list.sort(Comparable::compareTo);
    }

}


abstract class Animal {

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    protected final String type;
    protected final String name;

    protected Animal(String type, String name) {
        this.type = type;
        this.name = name;
    }

    abstract String makeSound();
}
class Dog extends Animal {

    public Dog(String type, String name) {
        super(type, name);
    }

    @Override
    public String makeSound() {
        return "Wuf";
    }

}



class Cat extends Animal implements Comparable<Cat> {
    public Cat(String type, String name) {
        super(type, name);
    }

    @Override
    public String makeSound() {
        return "Meow";
    }

    @Override
    public int compareTo(@NotNull Cat cat) {
        return this.getName().length() - cat.getName().length();
    }

}

