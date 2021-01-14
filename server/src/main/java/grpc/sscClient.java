package grpc;

import entities.Reservation;
import entities.Ride;
import generated.*;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class sscClient {
    private static final Logger logger = Logger.getLogger(sscClient.class.getName());

    private final sscGrpc.sscBlockingStub blockingStub;
    private final sscGrpc.sscStub asyncStub;

    public sscClient(String address) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
        blockingStub = sscGrpc.newBlockingStub(channel);
        asyncStub = sscGrpc.newStub(channel);
    }

    /**
     * Blocking server-streaming example. Calls listFeatures with a rectangle of interest. Prints each
     * response feature as it arrives.
     */
    public void upsert() {
        ride request =
                ride.newBuilder()
                        .setFirstName("Emil").setLastName("Kh").setPhone("05000").setStartPosition("A")
                        .setEndPosition("B").setDepartureTime("11/2/2020").setVacancies(3).setPd(4).build();

        blockingStub.upsert(request);
    }

    public String addRideLeader(Ride msg) {
        ride request = getRideRequest(msg);
        return blockingStub.withDeadlineAfter(3, TimeUnit.SECONDS).addRideLeader(request).getMsg(); // @TODO: decide how to use deadline for the retries with different leaders... exponential backoff until a threshold of 1 second for example.
    }

    public void addRideFollower(Ride msg) {
        ride request = getRideRequest(msg);

        asyncStub.addRideFollower(request, new StreamObserver<response>() {
            @Override
            public void onNext(response value) {

            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t.toString());
            }

            @Override
            public void onCompleted() {

            }
        });

    }

    private ride getRideRequest(Ride msg) {
        return ride.newBuilder()
                .setFirstName(msg.getFirstName())
                .setLastName(msg.getLastName())
                .setPhone(msg.getPhone())
                .setStartPosition(msg.getStartPosition().getName())
                .setEndPosition(msg.getEndPosition().getName())
                .setDepartureTime(msg.getDepartureTime())
                .setVacancies(msg.getVacancies())
                .setPd(msg.getPd())
                .build();
    }

    public void getRides(ArrayList<String> append_list, CountDownLatch latch) {
        asyncStub.getRides(emptyMessage.newBuilder().build(), new StreamObserver<response>() {
            @Override
            public void onNext(response value) {
                synchronized (append_list) {
                    append_list.add(value.getMsg());
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });
    }

    public Ride reserveRideLeader(Reservation msg) {
        reservation request = getReservationRequest(msg);
        return new Ride(blockingStub.withDeadlineAfter(3, TimeUnit.SECONDS).reserveRideLeader(request));
    }

    private reservation getReservationRequest(Reservation msg) {
        return reservation.newBuilder()
                .setFirstName(msg.getFirstName())
                .setLastName(msg.getLastName())
                .setDepartureTime(msg.getDepartureTime())
                .setPath(String.join(",", msg.getPath()))
                .build();

    }
}