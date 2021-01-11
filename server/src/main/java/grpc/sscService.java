package grpc;

import entities.Ride;
import generated.*;
import io.grpc.stub.StreamObserver;
import management.ServerManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class sscService extends sscGrpc.sscImplBase {

    private final int port;

    public sscService(int port) {
        this.port = port;
    }

    @Override
    public void upsert(ride request, StreamObserver<response> responseObserver) {
        System.out.println("Server port [" + port + "]");
        responseObserver.onNext(response.newBuilder().setMsg("YEs").build());
        responseObserver.onCompleted();
    }

    @Override
    public void getRides(generated.emptyMessage request, StreamObserver<generated.response> responseObserver) {
        ServerManager.getInstance().getRides().stream().filter((element) -> {
            ServerManager sm = ServerManager.getInstance();
            return sm.getLeaderShards().contains(sm.getCityShard(element.getStartPosition()));
        }).forEach((ride) -> {
            responseObserver.onNext(response.newBuilder().setMsg(ride.toString()).build());
        });
        responseObserver.onCompleted();
    }

    @Override
    public void addRideLeader(ride request, StreamObserver<response> responseObserver) {
        ServerManager sm = ServerManager.getInstance();
        Ride ride = new Ride(request);
        String result = sm.saveRide(ride);
        responseObserver.onNext(response.newBuilder().setMsg(result).build());
        responseObserver.onCompleted();
    }
}