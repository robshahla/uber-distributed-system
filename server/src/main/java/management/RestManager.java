package management;

import entities.Ride;
import entities.Server;
import grpc.sscClient;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RestManager {

    private final static Logger logger = Logger.getLogger(RestManager.class.getName());

    /**
     * Called from the RESET server
     *
     * @param ride the ride to add to the system
     * @return the added ride on success or Error message
     */
    public static String publishRide(Ride ride) {
        ServerManager sm = ServerManager.getInstance();
        Server leader_server = sm.getLeader(ride.getStartPosition().getName());
        if (sm.getServer().getName().equals(leader_server.getName())) {
            Ride new_ride = sm.addRideBroadCast(ride);
            return new_ride.serialize(); // think how to return to client... serialize for now
        }
        sscClient grpc_client = new sscClient(leader_server.getGrpcAddress());
        grpc_client.addRideLeader(ride); // @TODO: add retry for a couple of times in case the leader failed while broadcasting - we want to send this to the new leader
        return ride.serialize();
    }

    /**
     * Called from the RESET server
     */
    public static String getSnapshot() {
        ServerManager sm = ServerManager.getInstance();
        final String this_server_name = sm.getServer().getName();
        Map<String, Server> system_shards = sm.getSystemShards();
        final int shards_n = system_shards.size();
        CountDownLatch latch = new CountDownLatch(shards_n);
        logger.log(Level.FINE, "Snapshot request, getting rides from shards leaders, shards_n = " + shards_n);
        logger.log(Level.FINE, "Calling server: " + this_server_name);
        List<Ride> all_rides = new ArrayList<>();
        Set<Server> servers_to_add = new HashSet<>(system_shards.values());

        servers_to_add.forEach(server -> {
            assert server != null;
            final String current_server_name = server.getName();
            if (current_server_name.equals(this_server_name)) {
                logger.log(Level.FINE, "Getting rides from the calling server: " + server.getName());
                synchronized (all_rides) {
                    all_rides.addAll(sm.getRides());
                }
                return;
            }
            logger.log(Level.FINER, "Initializing grpc request with server: " + server.getName());
            sscClient grpc_client = new sscClient(server.getGrpcAddress());
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
            stringBuilder.append("--------------------------------------------------------------------");
        });
        logger.log(Level.FINE, "Finished getting rides, total of " + all_rides.size() + " ride(s)");
        return stringBuilder.toString();
    }
}
