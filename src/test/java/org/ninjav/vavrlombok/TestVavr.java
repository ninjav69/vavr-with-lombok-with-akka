package org.ninjav.vavrlombok;

import io.vavr.*;
import io.vavr.collection.Array;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.Value;
import lombok.val;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.vavr.API.*;
import static io.vavr.Predicates.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestVavr {
    @Test
    public void createATuple() {
        Tuple2<String, Integer> java8 = Tuple.of("Java", 8);
        assertThat(java8._1, is("Java"));
        assertThat(java8._2, is(8));
    }

    @Test
    public void mapTupleComponentWise() {
        Tuple2<String, Integer> java8 = Tuple.of("Java", 8);
        Tuple2<String, Integer> that = java8.map(
                s -> s.substring(2) + "vr",
                i -> i / 8
        );
        assertThat(that._1, is("vavr"));
        assertThat(that._2, is(1));
    }

    @Test
    public void mapToTupleUsingOneMapper() {
        Tuple2<String, Integer> java8 = Tuple.of("java", 8);
        Tuple2<String, Integer> that = java8.map(
                (s, i) -> Tuple.of(s.substring(2) + "vr", i / 8)
        );
        assertThat(that._1, is("vavr"));
        assertThat(that._2, is(1));
    }

    @Test
    public void transformATuple() {
        Tuple2<String, Integer> java8 = Tuple.of("java", 8);
        String that = java8.apply(
                (s, i) -> s.substring(2) + "vr " + i / 8
        );
        assertThat(that, is("vavr 1"));
    }

    @Test
    public void sumOfTwoIntegersFunction() {
        Function2<Integer, Integer, Integer> sum = (a, b) -> a + b;
        assertThat(sum.apply(1, 2), is(3));
    }

    @Test
    public void functionFromMethodReference() {
        Function3<String, String, String, String> function3 =
                Function3.of(this::methodWhichAccepts3Parameters);
        assertThat(function3
                .apply("one")
                .apply("two")
                .apply("three"), is("one, two, three"));
    }

    private String methodWhichAccepts3Parameters(String t1, String t2, String t3) {
        return String.join(", ", t1, t2, t3);
    }

    @Test
    public void functionComposition_withAndThen() {
        Function1<Integer, Integer> plusOne = a -> a + 1;
        Function1<Integer, Integer> multiplyByTwo = a -> a * 2;
        Function1<Integer, Integer> add1AndMultiplyBy2 = plusOne.andThen(multiplyByTwo);
        assertThat(add1AndMultiplyBy2.apply(2), is(6));
    }

    @Test
    public void functionComposition_withCompose() {
        Function1<Integer, Integer> plusOne = a -> a + 1;
        Function1<Integer, Integer> multiplyByTwo = a -> a * 2;
        Function1<Integer, Integer> add1AndMultiplyBy2 = multiplyByTwo.compose(plusOne);
        assertThat(add1AndMultiplyBy2.apply(2), is(6));
    }

    @Test
    public void lisftingPartialFunction() {
        Function2<Integer, Integer, Integer> divide = (a, b) -> a / b;
        Function2<Integer, Integer, Option<Integer>> safeDivide = Function2.lift(divide);
        Option<Integer> i1 = safeDivide.apply(1, 0);
        assertThat(i1.isEmpty(), is(true));
        Option<Integer> i2 = safeDivide.apply(4, 2);
        assertThat(i2.isEmpty(), is(false));
    }

    @Test
    public void liftingPartialFunctionViaMethodReference() {
        Function2<Integer, Integer, Option<Integer>> sum = Function2.lift(this::sum);
        assertThat(sum.apply(-1, -1).isEmpty(), is(true));
    }

    private int sum(int first, int second) {
        if (first < 0 || second < 0) {
            throw new IllegalArgumentException("Only positive integers are allowd");
        }
        return first + second;
    }

    @Test
    public void memoizedFunction() {
        Function0<Double> hashCode = Function0.of(Math::random).memoized();
        double random1 = hashCode.apply();
        double random2 = hashCode.apply();
        assertThat(random1, is(random2));
    }

    @Test
    public void trySomething() throws Exception {
        val defaultPeople = List.of(new Person("Alan", "Pickard", 41));
        val result = Try.of(this::loadPeople)
                .recover(x -> Match(x).of(
                        Case($(instanceOf(Exception.class)), t -> handleException(t))
                ))
                .getOrElse(defaultPeople);
    }

    private List<Person> handleException(Throwable ex) {
        System.out.println("Exception happened: " + ex.getMessage());
        return List.empty();
    }

    public List<Person> loadPeople() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Value
    public static class Person {
        String name;
        String surname;
        int age;
    }

    @Test
    public void lazyFunction() {
        Lazy<Double> lazy = Lazy.of(Math::random);
        assertThat(lazy.isEvaluated(), is(false));
        Double value1 = lazy.get();
        assertThat(lazy.isEvaluated(), is(true));
        Double value2 = lazy.get();
        assertThat(value1, is(value2));
    }

    public static class PersonValidator {

        public Validation<Seq<String>, Person> validatePerson(String name, String surname, int age) {
            return Validation.combine(
                    validateName(name),
                    validateSurname(surname),
                    validateAge(age)).ap(Person::new);
        }

        private Validation<String, String> validateName(String name) {
            return Option.of(name).isEmpty()
                    ? Validation.invalid("Name is required")
                    : Validation.valid(name);
        }

        private Validation<String, String> validateSurname(String surname) {
            return Option.of(surname).isEmpty()
                    ? Validation.invalid("Surname is required")
                    : Validation.valid(surname);
        }

        private Validation<String, Integer> validateAge(int age) {
            return age < 18
                    ? Validation.invalid("You must be at least 18 years of age")
                    : Validation.valid(age);
        }
    }

    @Test
    public void validation() {
        PersonValidator validator = new PersonValidator();
        Validation<Seq<String>, Person> valid = validator.validatePerson("Alan", "Pickard", 41);
        assertThat(valid.isValid(), is(true));
        Validation<Seq<String>, Person> invalid = validator.validatePerson("Koos", null, 16);
        assertThat(invalid.isInvalid(), is(true));

        invalid.getError().forEach(e -> System.out.println(e));
    }

    @Test
    public void infiniteStream() {
        for (double random : Stream.continually(Math::random).take(100)) {
            System.out.println(random);
        }

        Stream.from(1)
                .filter(i -> i % 2 == 0)
                .take(100)
                .toList()
                .forEach(System.out::println);
    }

}
