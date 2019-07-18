package org.ninjav.exceptions;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestExceptionStrategies {

    @Test(expected = RuntimeException.class)
    public void nothing() {
        assertThat(encodeAddress("my string"), is(equalTo("my+string")));
    }

    public static String encodeAddress(String... values) {
        return Arrays.stream(values)
                .map(s -> {
                    try {
                        return URLEncoder.encode(s, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.joining(" ,"));
    }

    @Test
    public void testExtractedMethod() {
        assertThat(encodedAddressUsingExtractedMehtod("my string"), is(equalTo("my+string")));
    }

    public static String encodeString(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String encodedAddressUsingExtractedMehtod(String... address) {
        return Arrays.stream(address)
        .map(TestExceptionStrategies::encodeString)
        .collect(Collectors.joining(","));
    }


    @Test
    public void testWrapperMethod() {
        assertThat(encodedAddressUsingWrapper("my string"), is(equalTo("my+string")));
    }


    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    private static <T, R> Function<T, R> wrap(CheckedFunction<T, R> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static String encodedAddressUsingWrapper(String... address) {
        return Arrays.stream(address)
                .map(wrap(s -> URLEncoder.encode(s, "UTF-8")))
                .collect(Collectors.joining());
    }
}
