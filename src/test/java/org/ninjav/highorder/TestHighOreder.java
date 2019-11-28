package org.ninjav.highorder;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class TestHighOreder {
    @Test
    public void nothing() {
        final List<BigDecimal> prices = Arrays.asList(
                new BigDecimal("10"),
                new BigDecimal("30"),
                new BigDecimal("17"),
                new BigDecimal("20"),
                new BigDecimal("15"),
                new BigDecimal("18"),
                new BigDecimal("45"),
                new BigDecimal("12")
        );

        final BigDecimal totalOfDiscountedPrices =
                prices.stream()
                        .filter(price -> price.compareTo(BigDecimal.valueOf(20)) > 0)
                        .map(price -> price.multiply(BigDecimal.valueOf(0.9)))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.printf("Discounted Prices Total: " + totalOfDiscountedPrices);
    }

    @Test
    public void returnAFunctionFromFunction() {
        final List<String> names = Arrays.asList("Brian", "Nate", "Neal", "Raju", "Sara", "Scott");

        final Function<String, Predicate<String>> startsWithLetter =
                letter -> name -> name.startsWith(letter);

        names.stream()
                .filter(startsWithLetter.apply("N")).count();
    }

    @Test
    public void badName() {
        final List<String> names = Arrays.asList("Brian", "Nate", "Neal", "Raju", "Sara", "Scott");
        pickNameBadly(names, "N");
    }

    public static void pickNameBadly(
            final List<String> names, final String startingLetter) {
        String foundName = null;
        for (String name : names) {
            if (name.startsWith(startingLetter)) {
                foundName = name;
                break;
            }
        }

        System.out.print(String.format("A name starting with %s: ", startingLetter));

        if (foundName != null) {
            System.out.println(foundName);
        } else {
            System.out.println("No name found");
        }
    }

    @Test
    public void goodName() {
        final List<String> names = Arrays.asList("Brian", "Nate", "Neal", "Raju", "Sara", "Scott");
        pickNameGoodly(names, "N");
    }

    public static void pickNameGoodly(
            final List<String> names, final String startingLetter) {

        final Optional<String> foundName =
                names.stream()
                        .filter(name -> name.startsWith(startingLetter))
                        .findFirst();

        System.out.println(String.format("A name starting with %s: %s",
                startingLetter, foundName.orElse("No name found")));
    }
}