package management;

import entities.City;
import entities.Reservation;
import entities.Ride;
import entities.Server;
import grpc.sscClient;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class RestManager {

    private static final MessagesManager logger = MessagesManager.instance;

    /**
     * Called from the RESET server
     *
     * @param ride the ride to add to the system
     * @return the added ride on success or Error message
     */
    public static String publishRide(Ride ride) {
        logger.log(Level.FINE, "----------------------- RESET REQUEST (PUBLISH RIDE) -----------------------");
        ServerManager sm = ServerManager.getInstance();
        Server leader_server = sm.getLeader(ride.getStartPosition().getName());
        if (sm.getServer().getName().equals(leader_server.getName())) {
            Ride new_ride = sm.addRideBroadCast(ride);
            return new_ride.toString(); // think how to return to client... serialize for now
        }
        sscClient grpc_client = leader_server.getGrpcClient();
        // @TODO: add retry for a couple of times in case the leader failed while broadcasting - we want to send this to the new leader
        return grpc_client.addRideLeader(ride).toString();
    }

    public static String reserveRide(Reservation reservation) {
        final List<String> path = reservation.getPath();
        assert path.size() >= 2;

        if (path.size() == 2) {
            return reserveOneRide(reservation);
        } else {
            return reservePath(reservation);
        }
    }

    /**
     * Called from the RESET server
     */
    public static String getSnapshot() {
        logger.log(Level.FINE, "----------------------- RESET REQUEST (GET SNAPSHOT) -----------------------");
        ServerManager sm = ServerManager.getInstance();
        final String this_server_name = sm.getServer().getName();
        Map<String, Server> system_shards = sm.getSystemShards();
        final int shards_n = system_shards.size();
        CountDownLatch latch = new CountDownLatch(shards_n);
        logger.log(Level.INFO, "Snapshot request, getting rides from shards leaders, shards_n = " + shards_n);
        logger.log(Level.INFO, "Calling server: " + this_server_name);
        List<Ride> all_rides = new ArrayList<>();
        Set<Server> servers_to_add = new HashSet<>(system_shards.values());

        servers_to_add.forEach(server -> {
            assert server != null;
            final String current_server_name = server.getName();
            if (current_server_name.equals(this_server_name)) {
                logger.log(Level.FINE, "Getting rides from the calling server: " + server.getName());
                synchronized (all_rides) {
                    logger.log(Level.FINE, "MNYKE + " + sm.getRides().size());
                    all_rides.addAll(sm.getRides());
                    latch.countDown();
                }
                return;
            }
            logger.log(Level.FINER, "Initializing grpc request with server: " + server.getName());
            sscClient grpc_client = server.getGrpcClient();
            grpc_client.getRidesAsync(all_rides, latch);
        });
        boolean success = false;
        try {
            success = latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        if (!success) {
            logger.log(Level.WARNING, "Error getting snapshot from server(s)");
            logger.log(Level.WARNING, "Init latch = " + system_shards.size());
            return "Error getting snapshot, please retry\n";
        }
        StringBuilder stringBuilder = new StringBuilder();
        all_rides.forEach(ride -> {
            stringBuilder.append(ride.toString());
            stringBuilder.append("------------------------------------\n");
        });
        logger.log(Level.FINE, "Finished getting rides, total of " + all_rides.size() + " ride(s)");
        return stringBuilder.toString();
    }


    private static String reserveOneRide(Reservation reservation) {
        synchronized (ServerManager.getInstance()) {
            ServerManager serverManager = ServerManager.getInstance();
            City start_city = serverManager.getCity(reservation.getPath().get(0));
            Set<Server> leaders = new HashSet<>(serverManager.getSystemShards().values());
            //TODO maybe sort?

            // send reserve request to each leader
            Ride reserved_ride = Ride.nullRide();
            for (Server leader : leaders) {
                if (leader.getName().equals(serverManager.getServer().getName())) {
                    reserved_ride = serverManager.reserveOneRideIfAvailableAndBroadCast(reservation);
                } else {
                    sscClient grpc_client = leader.getGrpcClient();
                    reserved_ride = grpc_client.blockingReserveRide(reservation.getReservationForGRPC());
                }
                if (!reserved_ride.isNull()) {
                    break;
                }
            }
            if (reserved_ride.isNull()) {
                return "No available ride found!";
            }
            return reserved_ride.toString();
        }
    }

    private static String reservePath(Reservation reservation) {
        // global lock
        synchronized (ServerManager.getInstance()) {

            return "";
        }
    }
}
