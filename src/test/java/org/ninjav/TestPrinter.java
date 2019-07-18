package org.ninjav;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPrinter {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void nothing() {
        final TestKit testProbe = new TestKit(system);
        final ActorRef printer = system.actorOf(Printer.props());
        printer.tell(new Printer.Greeting("Koos sent me"), ActorRef.noSender());
    }
}
