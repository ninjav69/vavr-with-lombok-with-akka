package org.ninjav.other;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestFunction {
    public static final int VERSION_1 = 1;
    public static final int VERSINT_2 = 2;

    private final Map<Integer, Predicate<Integer>> behaviour = new HashMap() {{
        put(1, (Predicate<Integer>) integer -> integer == 0 ? false : true);
        put(2, (Predicate<Integer>) integer -> integer == 0 ? true : false);
    }};

    @Test
    public void testVariations() {
        assertThat(behaviour.get(VERSION_1).test(0), is(equalTo(false)));
        assertThat(behaviour.get(VERSINT_2).test(0), is(equalTo(true)));
    }
}
