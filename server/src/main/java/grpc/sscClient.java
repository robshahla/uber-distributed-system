package grpc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entities.Ride;
import generated.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import management.MessagesManager;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;


public class sscClient {
    private static final MessagesManager logger = MessagesManager.instance;
    private static final emptyMessage EMPTY_MESSAGE = emptyMessage.newBuilder().build();
    private final sscGrpc.sscBlockingStub blockingStub;
    private final sscGrpc.sscStub asyncStub;
    private final ManagedChannel channel;
    StreamObserver<reservation> request_stream;

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

//        String new_ride = blockingStub.withDeadlineAfter(3, TimeUnit.SECONDS).addRideLeader(request).getMsg(); // @TODO: decide how to use deadline for the retries with different leaders... exponential backoff until a threshold of 1 second for example.
        String new_ride = blockingStub.addRideLeader(request).getMsg();
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

    public Ride blockingReserveRide(reservation reserve) {
        final Ride[] reserved_ride = new Ride[1];
        reserved_ride[0] = Ride.nullRide();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        request_stream = asyncStub.reserveRides(new StreamObserver<ride>() {
            @Override
            public void onNext(ride value) {
                //TODO: this is temp fix, we should implement proper Request/Response Header in protbuf
                if (value.getFirstName().equals("onCompleted")) {
                    countDownLatch.countDown();
                    request_stream.onCompleted();
                    return;
                }
                logger.log(Level.FINER, "Got onNext from server!");
                reserved_ride[0] = new Ride(value);
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.SEVERE, "Got onError from server! message: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.log(Level.FINER, "Got onCompleted from server!");

            }
        });

        try {
            // send reserve request
            logger.log(Level.FINE, "Sending stream request to server to reserve one ride if available..");
            request_stream.onNext(reserve);
//            request_stream.onCompleted(); // TEMP FIX for one ride reservation.

            countDownLatch.await();
            return reserved_ride[0];

        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Runtime error..." + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        request_stream.onCompleted();
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