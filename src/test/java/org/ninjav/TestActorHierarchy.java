package org.ninjav;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestActorHierarchy {
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
        ActorRef firstRef = system.actorOf(PrintMyActorRefActor.props(), "first-actor");
        System.out.println("First: " + firstRef);
        firstRef.tell("printit", ActorRef.noSender());
    }

    private static class PrintMyActorRefActor extends AbstractActor {
        static Props props() {
            return Props.create(PrintMyActorRefActor.class, PrintMyActorRefActor::new);
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("printit", p -> {
                        ActorRef secondRef = getContext().actorOf(Props.empty(), "second.actor");
                        System.out.println("Second: " + secondRef);
                    })
                    .build();
        }
    }
}
