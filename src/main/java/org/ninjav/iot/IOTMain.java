package org.ninjav.iot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.IOException;

public class IOTMain {

    public static void main(String[] args) throws IOException {
        ActorSystem system = ActorSystem.create("iot-system");

        try {
            ActorRef supervisor = system.actorOf(IOTSupervisor.props(), "iot-supervisor");

            System.out.println("Press ENTER to exit the system");
            System.in.read();
        } finally {
            system.terminate();
        }
    }
}
