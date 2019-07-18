package org.ninjav;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Application {

    static final Logger logger = LoggerFactory.getLogger(Application.class.getName());

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("helloakka");

        final ActorRef printerActor =
                system.actorOf(Printer.props(), "printerActor");
        final ActorRef howdyGreeter =
                system.actorOf(Greeter.props("Howdy", printerActor), "howdyGreeter");
        final ActorRef helloGreeter =
                system.actorOf(Greeter.props("Hello", printerActor), "helloGreeter");
        final ActorRef goodDayGreeter =
                system.actorOf(Greeter.props("Good day", printerActor), "goodDayGreeter");

        howdyGreeter.tell(new Greeter.WhoToGreet("Akka"), ActorRef.noSender());
        howdyGreeter.tell(new Greeter.Greet(), ActorRef.noSender());

        helloGreeter.tell(new Greeter.WhoToGreet("Lightbend"), ActorRef.noSender());
        helloGreeter.tell(new Greeter.Greet(), ActorRef.noSender());

        goodDayGreeter.tell(new Greeter.WhoToGreet("Play"), ActorRef.noSender());
        goodDayGreeter.tell(new Greeter.Greet(), ActorRef.noSender());

        System.out.println(">>> Press ENTER to exit <<<");
        try {
            System.in.read();
        } catch (IOException e) {
        } finally {
            system.terminate();
        }
    }
}
