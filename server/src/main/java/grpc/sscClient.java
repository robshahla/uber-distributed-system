package grpc;

import entities.Ride;
import generated.*;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
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

        try {
            blockingStub.upsert(request);

        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
    }

    public String addRideLeader(Ride msg, boolean async) {
        ride request =
                ride.newBuilder()
                        .setFirstName(msg.getFirstName())
                        .setLastName(msg.getLastName())
                        .setPhone(msg.getPhone())
                        .setStartPosition(msg.getStartPosition())
                        .setEndPosition(msg.getEndPosition())
                        .setDepartureTime(msg.getDepartureTime())
                        .setVacancies(msg.getVacancies())
                        .setPd(msg.getPd())
                        .build();
        if (async) {
            asyncStub.addRideLeader(request, new StreamObserver<response>() {
                @Override
                public void onNext(response value) {

                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            });
            return "";
        } else {
            return blockingStub.addRideLeader(request).getMsg();
        }
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
}