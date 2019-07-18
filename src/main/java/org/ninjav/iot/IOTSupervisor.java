package org.ninjav.iot;

import akka.actor.AbstractActor;
import akka.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOTSupervisor extends AbstractActor {
    private final Logger log = LoggerFactory.getLogger(IOTSupervisor.class);

    public static Props props() {
        return Props.create(IOTSupervisor.class, IOTSupervisor::new);
    }

    @Override
    public void preStart() throws Exception {
        log.info("IOT Application started");
    }

    @Override
    public void postStop() throws Exception {
        log.info("IOT Application stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
