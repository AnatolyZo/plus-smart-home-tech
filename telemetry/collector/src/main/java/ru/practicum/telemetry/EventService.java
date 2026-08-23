package ru.practicum.telemetry;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.telemetry.hubs.HubEvent;
import ru.practicum.telemetry.mapper.HubMapper;
import ru.practicum.telemetry.mapper.SensorMapper;
import ru.practicum.telemetry.sensors.SensorEvent;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.*;

@GrpcService
@RequiredArgsConstructor
public class EventService extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final EventsService eventsService;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            SensorEvent sensorEvent = SensorMapper.toSensorEvent(request);
            eventsService.processSensorEvent(sensorEvent);

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
            HubEvent hubEvent = HubMapper.toHubEvent(request);
            eventsService.processHubEvent(hubEvent);

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
