package grpc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entities.Ride;
import generated.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class sscClient {
    private static final Logger logger = Logger.getLogger(sscClient.class.getName());
    private static final emptyMessage EMPTY_MESSAGE = emptyMessage.newBuilder().build();
    private final sscGrpc.sscBlockingStub blockingStub;
    private final sscGrpc.sscStub asyncStub;
    private final ManagedChannel channel;


    public sscClient(String address) {
        channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
        blockingStub = sscGrpc.newBlockingStub(channel);
        asyncStub = sscGrpc.newStub(channel);
    }

    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
        }
    }


    public Ride addRideLeader(Ride msg) {
        ride request = msg.getRideRequestForGRPC();
        System.out.println("Got Ride: " + msg);
        System.out.println("Transformed to ride: " + request);

        String new_ride = blockingStub.withDeadlineAfter(3, TimeUnit.SECONDS).addRideLeader(request).getMsg(); // @TODO: decide how to use deadline for the retries with different leaders... exponential backoff until a threshold of 1 second for example.
        JsonObject obj = JsonParser.parseString(new_ride).getAsJsonObject();
        return Ride.deserialize(obj);
    }


    public void getRidesAsync(List<Ride> append_list, CountDownLatch latch) {
        asyncStub.getRidesAsync(EMPTY_MESSAGE, new StreamObserver<Response>() {
            @Override
            public void onNext(Response value) {
                synchronized (append_list) {
                    JsonObject jsonObject = JsonParser.parseString(value.getMsg()).getAsJsonObject();
                    append_list.add(Ride.deserialize(jsonObject));
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

    public Ride blockingReserveRide(reservation reservation) {
        final Ride[] reserved_ride = new Ride[1];
        reserved_ride[0] = Ride.nullRide();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<reservation> request_stream = asyncStub.reserveRides(new StreamObserver<ride>() {
            @Override
            public void onNext(ride value) {
                reserved_ride[0] = new Ride(value);
            }

            @Override
            public void onError(Throwable t) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });

        try {
            // send reserve request
            request_stream.onNext(reservation);
            // wait for reservation
            countDownLatch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return reserved_ride[0];
    }

    public StreamObserver<reservation> nonBlockingReserveRide(reservation reservation, List<Ride> relevant_rides, CountDownLatch countDownLatch) {
        StreamObserver<reservation> request_stream = asyncStub.reserveRides(new StreamObserver<ride>() {
            @Override
            public void onNext(ride value) {
                synchronized (relevant_rides) {
                    relevant_rides.add(new Ride(value));
                }
            }

            @Override
            public void onError(Throwable t) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });

        request_stream.onNext(reservation);
        return request_stream;
    }
}