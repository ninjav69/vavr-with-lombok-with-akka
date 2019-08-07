package org.ninjav.vavrlombok;

import lombok.Value;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestPerson {
    @Test
    public void nothing() {
        TestPerson.Person p = new TestPerson.Person("Alan", "Pickard");
        assertThat(p.getName(), is("Alan"));
        assertThat(p.getSurname(), is("Pickard"));

        System.out.println(p);
    }

    @Value
    public class Person {
        String name;
        String surname;
    }
}
