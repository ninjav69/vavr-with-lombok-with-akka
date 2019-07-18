package org.ninjav;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSupervisor {

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
        ActorRef supervisingActor = system.actorOf(SupervisingActor.props(), "supervising-actor");
        supervisingActor.tell("failChild", ActorRef.noSender());
    }


    private static class SupervisingActor extends AbstractActor {

        public static Props props() {
            return Props.create(SupervisingActor.class, SupervisingActor::new);
        }

        private ActorRef child = getContext().actorOf(SupervisedActor.props(), "supervised-actor");

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("failChild", f -> {
                        child.tell("fail", getSelf());
                    }).build();
        }
    }

    private static class SupervisedActor extends AbstractActor {

        public static Props props() {
            return Props.create(SupervisedActor.class, SupervisedActor::new);
        }

        @Override
        public void preStart() throws Exception {
            System.out.println("supervised actor started");
        }

        @Override
        public void postStop() throws Exception {
            System.out.println("supervised actor stopped");
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder().matchEquals("fail", f -> {
                System.out.println("supervised actor fails now");
                throw new Exception("I failed!");
            }).build();
        }
    }
}
