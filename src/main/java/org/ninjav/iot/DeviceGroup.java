package org.ninjav.iot;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DeviceGroup extends AbstractActor  {
    private final Logger log = LoggerFactory.getLogger(DeviceGroup.class);

    final String groupId;
    final Map<String, ActorRef> deviceIdToActor = new HashMap<>();
    final Map<ActorRef, String> actorToDeviceId = new HashMap<>();

    public DeviceGroup(String groupId) {
        this.groupId = groupId;
    }

    public static Props props(String groupId) {
        return Props.create(DeviceGroup.class, () -> new DeviceGroup(groupId));
    }

    @Override
    public void preStart() throws Exception {
        log.info("DeviceGroup {} started", groupId);
    }

    @Override
    public void postStop() throws Exception {
        log.info("DeviceGroup {} stoppped", groupId);
    }

    private void onTrackDevice(DeviceManager.RequestTrackDevice trackMsg) {
        if (this.groupId.equals(trackMsg.groupId)) {
            ActorRef deviceActor = deviceIdToActor.get(trackMsg.deviceId);
            if (deviceActor != null) {
                deviceActor.forward(trackMsg, getContext());
            } else {
                log.info("Creating device actor for {}", trackMsg.deviceId);
                deviceActor = getContext().actorOf(
                        Device.props(groupId, trackMsg.deviceId), "device-" + trackMsg.deviceId);
                getContext().watch(deviceActor);
                deviceIdToActor.put(trackMsg.deviceId, deviceActor);
                actorToDeviceId.put(deviceActor, trackMsg.deviceId);
                deviceActor.forward(trackMsg, getContext());
            }
        } else {
            log.warn("Ignoring TrackDevice request for {}. This actor is reponsible for {}.",
                    groupId, this.groupId);
        }
    }

    private void onTerminated(Terminated t) {
        ActorRef deviceActor = t.getActor();
        String deviceId = actorToDeviceId.get(deviceActor);
        log.info("Device actor for {} has been terminated", deviceId);
        actorToDeviceId.remove(deviceActor);
        deviceIdToActor.remove(deviceId);
    }

    private void onDeviceList(RequestDeviceList r) {
        getSender().tell(new ReplyDeviceList(r.requestId, deviceIdToActor.keySet()), getSelf());
    }

    private void onAllTemperatures(RequestAllTemperatures r) {
        Map<ActorRef, String> actorToDeviceIdCopy = new HashMap<>(this.actorToDeviceId);

        getContext().actorOf(DeviceGroupQuery.props(
                actorToDeviceIdCopy, r.requestId, getSender(), new FiniteDuration(3, TimeUnit.SECONDS)));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DeviceManager.RequestTrackDevice.class, this::onTrackDevice)
                .match(Terminated.class, this::onTerminated)
                .match(RequestDeviceList.class, this::onDeviceList)
                .match(RequestAllTemperatures.class, this::onAllTemperatures)
                .build();
    }

    public static final class RequestDeviceList {
        final long requestId;

        public RequestDeviceList(long requestId) {
            this.requestId = requestId;
        }
    }

    public static final class ReplyDeviceList {
        final long requestId;
        final Set<String> ids;

        public ReplyDeviceList(long requestId, Set<String> ids) {
            this.requestId = requestId;
            this.ids = ids;
        }
    }

    public static final class RequestAllTemperatures {
        final long requestId;

        public RequestAllTemperatures(long requestId) {
            this.requestId = requestId;
        }
    }

    public static final class RespondAllTemperatures {
        final long requestId;
        final Map<String, TemperatureReading> temperatures;

        public RespondAllTemperatures(long requestId, Map<String, TemperatureReading> temperatures) {
            this.requestId = requestId;
            this.temperatures = temperatures;
        }
    }

    public static interface TemperatureReading {}

    public static final class Temperature implements TemperatureReading {
        public final double value;

        public Temperature(double value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Temperature that = (Temperature) o;
            return Double.compare(that.value, value) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "Temperature{" +
                    "value=" + value +
                    '}';
        }
    }

    public enum TemperatureNotAvailable implements TemperatureReading {
        INSTANCE
    }

    public enum DeviceNotAvailable implements TemperatureReading {
        INSTANCE
    }

    public enum DeviceTimedOut implements TemperatureReading {
        INSTANCE
    }


}
