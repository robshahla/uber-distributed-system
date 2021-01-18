package management;

import entities.Reservation;
import entities.Ride;
import generated.reservation;
import generated.ride;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.Semaphore;

public class ReserveRequestHandler implements StreamObserver<reservation> {
    private final Semaphore internal_lock;
    private static ReserveRequestHandler instance = null;
    private StreamObserver<ride> responseObserver;

    static {
        instance = new ReserveRequestHandler();
    }

    private ReserveRequestHandler() {
        internal_lock = new Semaphore(1);
    }

    public static ReserveRequestHandler getNewHandler(StreamObserver<ride> responseObserver) throws InterruptedException {
        instance.internal_lock.acquire();
        instance.responseObserver = responseObserver;
        return instance;
    }


    @Override
    public void onNext(reservation value) {
        List<String> path = value.getPathList();
        final int path_size = path.size();
        if (path_size < 2) responseObserver.onCompleted();
        else if (path_size == 2) {
            Ride reserved_ride = ServerManager.getInstance()
                    .reserveOneRideIfAvailableAndBroadCast(new Reservation(value));
            responseObserver.onNext(reserved_ride.getRideRequestForGRPC());
            responseObserver.onCompleted();
            internal_lock.release();
            return;
        }
        path.remove(path_size - 1);
        List<Ride> relevant_rides = ServerManager.getInstance()
                .getRidesForCities(path);


    }

    @Override
    public void onError(Throwable t) {
        internal_lock.release();
    }

    @Override
    public void onCompleted() {
        internal_lock.release();
    }


}
