package org.ninjav.parse;

import org.junit.Test;

import java.util.Arrays;

public class TestParser {
    final String testString = "\"039994\",\"MOMENTUM DIGITAL\",\"Y\",\"20140101\",\"190400218606\",\"N\"\n";

    @Test
    public void canStreamString() {

        Arrays.stream(testString.split(",")).forEach(
                System.out::println
        );
    }

    @Test
    public void canStreamStringChars() {


    }
}
