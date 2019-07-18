package org.ninjav.iot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestDevice {
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
    public void testReplyWithLatsetTemperatureReading() {
        TestKit probe = new TestKit(system);
        ActorRef deviceActor = system.actorOf(Device.props("group", "device"));

        deviceActor.tell(new Device.RecordTemperature(1L, 24.0), probe.getRef());
        assertThat(probe.expectMsgClass(Device.TemperatureRecorded.class).requestId, is(1L));

        deviceActor.tell(new Device.ReadTemperature(2L), probe.getRef());
        Device.RespondTemperature response1 = probe.expectMsgClass(Device.RespondTemperature.class);
        assertThat(response1.requestId, is(2L));
        assertThat(response1.value, is(equalTo(Optional.of(24.0))));

        deviceActor.tell(new Device.RecordTemperature(3L, 55.0), probe.getRef());
        assertThat(probe.expectMsgClass(Device.TemperatureRecorded.class).requestId, is(3L));

        deviceActor.tell(new Device.ReadTemperature(4L), probe.getRef());
        Device.RespondTemperature response2 = probe.expectMsgClass(Device.RespondTemperature.class);
        assertThat(response2.requestId, is(4L));
        assertThat(response2.value, is(equalTo(Optional.of(55.0))));
    }

    @Test
    public void canReplyToRegistrationRequests() {
        TestKit probe = new TestKit(system);
        ActorRef deviceActor = system.actorOf(Device.props("grouo", "device"));

        deviceActor.tell(new DeviceManager.RequestTrackDevice("group", "device"), probe.getRef());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        assertThat(deviceActor, is(equalTo(probe.getLastSender())));
    }

    @Test
    public void cannotReplyToWrongRegistrationRequests() {
        TestKit probe = new TestKit(system);
        ActorRef deviceActor = system.actorOf(Device.props("group", "device"));

        deviceActor.tell(new DeviceManager.RequestTrackDevice("wrongGroup", "device"), probe.getRef());
        probe.expectNoMessage();

        deviceActor.tell(new DeviceManager.RequestTrackDevice("group", "wrongDevice"), probe.getRef());
        probe.expectNoMessage();
    }
}
