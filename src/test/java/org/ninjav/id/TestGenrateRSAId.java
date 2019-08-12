package org.ninjav.id;

import io.vavr.collection.Stream;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

// See infos of ID number format:
// https://mybroadband.co.za/news/security/303812-what-your-south-african-id-number-means-and-what-it-reveals-about-you.html

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestGenrateRSAId {
    final static int MALE = 5111;
    final static int FEMALE = 111;

    @Test
    public void TestMakeIdNumbersForKoketso() {
        System.out.println("Male 05/05/2000: " + makeIdNumber(0, 5, 5, MALE));
        System.out.println("Male 25/12/2018: " + makeIdNumber(18, 12, 25, MALE));

        System.out.println("Female 19/07/2006: " + makeIdNumber(6, 7, 19, FEMALE));
        System.out.println("Female 01/09/2015: " + makeIdNumber(15, 9, 1, FEMALE));
    }

    private String makeIdNumber(int yearAfter2K, int month, int day, int gender) {
        String partialId = makeId(yearAfter2K, month, day, gender, 0, 8);
        int l = luhn(partialId);
        return String.format("%s%d", partialId, l);
    }

    private String makeId(int year, int month, int day, int gender, int citizenship, int race) {
        return String.format("%02d%02d%02d%04d%1d%1d",
                year, month, day, gender, citizenship, race);
    }

    private int luhn(String partialId) {
        // Map string to list of Int
        List<Integer> lst = Arrays.stream(partialId.split(""))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // Make list of every even index values
        List<Integer> evenList = new ArrayList<>();
        for (int i = lst.size() - 1; i >= 0; i -= 2) {
            evenList.add(lst.get(i));
        }
        // Double the list of values at even index * 2 and perform a fadic sum
        List<Integer> evenFadicList = new ArrayList<>();
        for (int i = 0; i < evenList.size(); i++) {
            int sum = evenList.get(i) * 2;
            String sumStr = String.format("%02d", sum);
            int fadicSum = Integer.parseInt(sumStr.split("")[0])
                    + Integer.parseInt(sumStr.split("")[1]);
            evenFadicList.add(fadicSum);
        }
        // Make a list of every odd index values
        List<Integer> oddList = new ArrayList<>();
        for (int i = lst.size() - 2; i >= 0; i -= 2) {
            oddList.add(lst.get(i));
        }

        // Add the lists together
        List<Integer> totalList = new ArrayList<>();
        totalList.addAll(evenFadicList);
        totalList.addAll(oddList);

        // Calculate the total sum
        int totalSum = totalList.stream()
                .mapToInt(Integer::intValue)
                .sum();
        return (totalSum % 10 == 0) ? 0 : 10 - (totalSum % 10);
    }

    @Test
    public void canMakeIdNumber() {
        String idNumber = makeIdNumber(00, 05, 05, MALE);
        assertThat(idNumber, is("0005055111081"));
    }

    @Test
    public void canFormatIdFromParts() {
        int year = 88;
        int month = 01;
        int day = 23;
        int gender = 5111;
        int citizenship = 0;
        int race = 8;

        String partialId = makeId(year, month, day, gender, citizenship, race);

        assertThat(partialId, is("880123511108"));
    }

    @Test
    public void canCalculateLuhnForPartialId() {
        String id = "880123511108";
        int result = luhn(id);
        assertThat(result, is(8));
    }

    @Test
    public void func() {

        io.vavr.collection.List<Integer> list = io.vavr.collection.List.of(1, 2, 3, 4, 5, 6);

        list.map(RSAId.doubleIt)
                .map(RSAId.fadic)
                .forEach(System.out::println);
    }

    public static class RSAId {
        public static final Function<Integer, Integer> doubleIt = x -> x * 2;
        static final Function<Integer, io.vavr.collection.List<Integer>> digitToInts = x ->
                Stream.of(String.format("%d", x).split(""))
                        .map(Integer::parseInt)
                        .toList();
        public static final Function<Integer, Integer> fadic = x -> x > 9
                ? RSAId.fadic.apply(digitToInts.apply(x).sum().intValue())
                : x;
    }
}
