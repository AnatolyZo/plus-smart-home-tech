package ru.practicum.telemetry;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.*;

@GrpcService
public class EventService extends CollectorControllerGrpc.CollectorControllerImplBase {

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            SensorEventProto.PayloadCase payloadCase = request.getPayloadCase();

            switch (payloadCase) {
                case LIGHT_SENSOR:
                    LightSensorProto lightSensorProto = request.getLightSensor();
                    break;
                case CLIMATE_SENSOR:
                    ClimateSensorProto climateSensorProto = request.getClimateSensor();
                    break;
                case MOTION_SENSOR:
                    MotionSensorProto motionSensorProto = request.getMotionSensor();
                    break;
                case SWITCH_SENSOR:
                    SwitchSensorProto switchSensorProto = request.getSwitchSensor();
                case TEMPERATURE_SENSOR:
                    TemperatureSensorProto temperatureSensorProto = request.getTemperatureSensor();
                    break;
                default:
                    throw new IllegalArgumentException("Передан неизвестный тип датчика " + request.getClass());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            HubEventProto.PayloadCase payloadCase = request.getPayloadCase();

            switch (payloadCase) {
                case DEVICE_ADDED:
                    DeviceAddedEventProto deviceAddedEventProto = request.getDeviceAdded();
                    break;
                case DEVICE_REMOVED:
                    DeviceRemovedEventProto deviceRemovedEventProto = request.getDeviceRemoved();
                    break;
                case SCENARIO_ADDED:
                    ScenarioAddedEventProto scenarioAddedEventProto = request.getScenarioAdded();
                    break;
                case SCENARIO_REMOVED:
                    ScenarioRemovedEventProto scenarioRemovedEventProto = request.getScenarioRemoved();
                    break;
                default:
                    throw new IllegalArgumentException("Передан неизвестный тип хаба " + request.getClass());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
