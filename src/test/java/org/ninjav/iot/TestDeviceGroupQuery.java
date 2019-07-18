package org.ninjav.iot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestDeviceGroupQuery {

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
    public void returnTemperatureValuesForWorkingDevices() {
        TestKit requester = new TestKit(system);
        TestKit device1 = new TestKit(system);
        TestKit device2 = new TestKit(system);

        Map<ActorRef, String> actorToDeviceId = new HashMap<>();
        actorToDeviceId.put(device1.getRef(), "device1");
        actorToDeviceId.put(device2.getRef(), "device2");

        ActorRef queryActor = system.actorOf(DeviceGroupQuery.props(
                actorToDeviceId, 1L, requester.getRef(), new FiniteDuration(3, TimeUnit.SECONDS)));

        assertThat(device1.expectMsgClass(Device.ReadTemperature.class).requestId, is(0L));
        assertThat(device2.expectMsgClass(Device.ReadTemperature.class).requestId, is(0L));

        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(1.0)), device1.getRef());
        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(2.0)), device2.getRef());

        DeviceGroup.RespondAllTemperatures response =
                requester.expectMsgClass(DeviceGroup.RespondAllTemperatures.class);
        assertThat(response.requestId, is(1L));

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceGroup.Temperature(1.0));
        expectedTemperatures.put("device2", new DeviceGroup.Temperature(2.0));

        assertThat(response.temperatures, is(equalTo(expectedTemperatures)));
    }

    @Test
    public void returnTemperatureNotAvailableForDevicesWithNoReadions() {
        TestKit requester = new TestKit(system);
        TestKit device1 = new TestKit(system);
        TestKit device2 = new TestKit(system);

        Map<ActorRef, String> actorToDeviceId = new HashMap<>();
        actorToDeviceId.put(device1.getRef(), "device1");
        actorToDeviceId.put(device2.getRef(), "device2");

        ActorRef queryActor = system.actorOf(DeviceGroupQuery.props(
                actorToDeviceId, 1L, requester.getRef(), new FiniteDuration(3, TimeUnit.SECONDS)));

        assertThat(device1.expectMsgClass(Device.ReadTemperature.class).requestId, is(0L));
        assertThat(device2.expectMsgClass(Device.ReadTemperature.class).requestId, is(0L));

        queryActor.tell(new Device.RespondTemperature(0L, Optional.empty()), device1.getRef());
        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(2.0)), device2.getRef());

        DeviceGroup.RespondAllTemperatures response =
                requester.expectMsgClass(DeviceGroup.RespondAllTemperatures.class);
        assertThat(response.requestId, is(1L));

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", DeviceGroup.TemperatureNotAvailable.INSTANCE);
        expectedTemperatures.put("device2", new DeviceGroup.Temperature(2.0));

        assertThat(response.temperatures, is(equalTo(expectedTemperatures)));
    }

    @Test
    public void returnDeviceNotAvailableIfDeviceStopsBeforeAnswering() {
        TestKit requester = new TestKit(system);
        TestKit device1 = new TestKit(system);
        TestKit device2 = new TestKit(system);

        Map<ActorRef, String> actorToDeviceId = new HashMap<>();
        actorToDeviceId.put(device1.getRef(), "device1");
        actorToDeviceId.put(device2.getRef(), "device2");

        ActorRef queryActor = system.actorOf(DeviceGroupQuery.props(
                actorToDeviceId, 1L, requester.getRef(), new FiniteDuration(3, TimeUnit.SECONDS)));

        assertThat(device1.expectMsgClass(Device.ReadTemperature.class).requestId, is(0L));
        assertThat(device2.expectMsgClass(Device.ReadTemperature.class).requestId, is(0L));

        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(1.0)), device1.getRef());
        device2.getRef().tell(PoisonPill.getInstance(), ActorRef.noSender());

        DeviceGroup.RespondAllTemperatures response =
                requester.expectMsgClass(DeviceGroup.RespondAllTemperatures.class);
        assertThat(response.requestId, is(1L));

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceGroup.Temperature(1.0));
        expectedTemperatures.put("device2", DeviceGroup.DeviceNotAvailable.INSTANCE);

        assertThat(response.temperatures, is(equalTo(expectedTemperatures)));
    }

    @Test
    public void returnTemperatureReadingEvenIfDeviceStopsAfterAnswering() {
        TestKit requester = new TestKit(system);
        TestKit device1 = new TestKit(system);
        TestKit device2 = new TestKit(system);

        Map<ActorRef, String> actorToDeviceId = new HashMap<>();
        actorToDeviceId.put(device1.getRef(), "device1");
        actorToDeviceId.put(device2.getRef(), "device2");

        ActorRef queryActor = system.actorOf(DeviceGroupQuery.props(
                actorToDeviceId, 1L, requester.getRef(), new FiniteDuration(3, TimeUnit.SECONDS)));

        assertThat(device1.expectMsgClass(Device.ReadTemperature.class).requestId, is(0L));
        assertThat(device2.expectMsgClass(Device.ReadTemperature.class).requestId, is(0L));

        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(1.0)), device1.getRef());
        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(2.0)), device2.getRef());
        device2.getRef().tell(PoisonPill.getInstance(), ActorRef.noSender());

        DeviceGroup.RespondAllTemperatures response =
                requester.expectMsgClass(DeviceGroup.RespondAllTemperatures.class);
        assertThat(response.requestId, is(1L));

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceGroup.Temperature(1.0));
        expectedTemperatures.put("device2", new DeviceGroup.Temperature(2.0));

        assertThat(response.temperatures, is(equalTo(expectedTemperatures)));

    }

    @Test
    public void returDeviceTimedOutIfDeviceDoesNotAnswerInTime() {
        TestKit requester = new TestKit(system);
        TestKit device1 = new TestKit(system);
        TestKit device2 = new TestKit(system);

        Map<ActorRef, String> actorToDeviceId = new HashMap<>();
        actorToDeviceId.put(device1.getRef(), "device1");
        actorToDeviceId.put(device2.getRef(), "device2");

        ActorRef queryActor = system.actorOf(DeviceGroupQuery.props(
                actorToDeviceId, 1L, requester.getRef(), new FiniteDuration(1, TimeUnit.SECONDS)));

        assertThat(device1.expectMsgClass(Device.ReadTemperature.class).requestId, is(0L));
        assertThat(device2.expectMsgClass(Device.ReadTemperature.class).requestId, is(0L));

        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(1.0)), device1.getRef());

        DeviceGroup.RespondAllTemperatures response =
                requester.expectMsgClass(
                        java.time.Duration.ofSeconds(5), DeviceGroup.RespondAllTemperatures.class);
        assertThat(response.requestId, is(1L));

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceGroup.Temperature(1.0));
        expectedTemperatures.put("device2", DeviceGroup.DeviceTimedOut.INSTANCE);

        assertThat(response.temperatures, is(equalTo(expectedTemperatures)));

    }
}
