package management;

import entities.Reservation;
import entities.Ride;
import generated.reservation;
import generated.ride;
import io.grpc.stub.StreamObserver;
import management.logging.UberLogger;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

public class ReserveRequestHandler implements StreamObserver<reservation> {
    private static UberLogger logger = UberLogger.getLogger(ReserveRequestHandler.class.getName());


    private static final Semaphore internal_lock;
    private static ReserveRequestHandler instance = null;
    private StreamObserver<ride> responseObserver;

    static {
        instance = new ReserveRequestHandler();
        internal_lock = new Semaphore(1);
    }

    private ReserveRequestHandler() {}

    public static ReserveRequestHandler getNewHandler(StreamObserver<ride> responseObserver) throws InterruptedException {
        acquire();
        instance.responseObserver = responseObserver;
        return instance;
    }

    public static void acquire() throws InterruptedException {
        logger.log(Level.FINE, "Acquiring semaphore lock...");
        internal_lock.acquire();
        logger.log(Level.FINE, "Semaphore lock acquired");
    }

    public static void release() {
        logger.log(Level.FINE, "Releasing semaphore lock");
        internal_lock.release();
    }

    @Override
    public void onNext(reservation value) {
        List<String> path = value.getPathList();
        final int path_size = path.size();
        logger.log(Level.FINE, "Path length=" + path_size);
        if (path_size < 2) {
            responseObserver.onNext(Ride.nullRide().getRideRequestForGRPC());
        } else if (path_size == 2) {
            Ride reserved_ride = ServerManager.getInstance()
                    .reserveOneRideIfAvailableAndBroadCast(new Reservation(value));
            logger.log(Level.FINE, "Finished searching for suitable rides");
            if (!reserved_ride.isNull()) {
                logger.log(Level.FINE, "Found ride");
                responseObserver.onNext(reserved_ride.getRideRequestForGRPC());
            }
            logger.log(Level.FINE, "Sending end of stream to client");
            responseObserver.onNext(Ride.nullRide().getRideRequestForGRPC());
            //responseObserver.onCompleted();// TODO this should be sent as message (another onNext)
        } else {
            List<Ride> relevant_rides = ServerManager.getInstance()
                    .getRidesForPath(path);

            for (Ride relevant_ride : relevant_rides) {
                responseObserver.onNext(relevant_ride.getRideRequestForGRPC());
            }
            //should change this as well
            responseObserver.onNext(Ride.nullRide().getRideRequestForGRPC());
        }

    }

    @Override
    public void onError(Throwable t) {
        logger.log(Level.FINE, "Got onError from Client, releasing semaphore..." + t.getMessage());
        release();

    }

    @Override
    public void onCompleted() {
        responseObserver.onCompleted();
        logger.log(Level.FINE, "Got onCompleted from Client, releasing semaphore...");
        release();
    }


}
