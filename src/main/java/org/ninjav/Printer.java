package org.ninjav;

import akka.actor.AbstractActor;
import akka.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Printer extends AbstractActor  {
    private static final Logger logger = LoggerFactory.getLogger(Process.class.getName());

    public static Props props() {
        return Props.create(Printer.class, () -> new Printer());
    }

    public static class Greeting {
        public final String message;

        public Greeting(String message) {
            this.message = message;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Greeting.class, greeting -> {
                    logger.info(greeting.message);
                })
                .build();
    }
}
