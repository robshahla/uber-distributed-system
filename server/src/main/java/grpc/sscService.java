package grpc;

import entities.Ride;
import generated.*;
import io.grpc.stub.StreamObserver;
import management.ReserveRequestHandler;
import management.ServerManager;

public class sscService extends sscGrpc.sscImplBase {


    @Override
    public void getRidesAsync(emptyMessage request, StreamObserver<Response> responseObserver) {
        ServerManager.getInstance().getRides().stream().map(ride ->
                Response.newBuilder().setMsg(ride.serialize()).build())
                .forEach(responseObserver::onNext);
        responseObserver.onCompleted();

    }

    @Override
    public void addRideLeader(ride request, StreamObserver<Response> responseObserver) {
        System.out.println("Got addRideLeader request ride= " + request);
        ServerManager sm = ServerManager.getInstance();
        Ride ride = new Ride(request);
        System.out.println("Transformed into Ride " + ride);
        String result = sm.addRideBroadCast(ride).serialize();
        responseObserver.onNext(Response.newBuilder().setMsg(result).build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<reservation> reserveRides(StreamObserver<ride> responseObserver) {
        ReserveRequestHandler newHandler = null;
        try {
            newHandler = ReserveRequestHandler.getNewHandler(responseObserver);
        } catch (InterruptedException e) {
            //TODO: catch this focking exception by order of the peaky blinders
        }
        return newHandler;
    }
}