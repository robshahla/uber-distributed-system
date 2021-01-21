package management;

import entities.City;
import entities.Reservation;
import entities.Ride;
import entities.Server;
import grpc.sscClient;
import io.grpc.stub.StreamObserver;
import management.logging.UberLogger;
import utilities.PathSolver;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RestManager {

    private static UberLogger logger = UberLogger.getLogger(RestManager.class.getName());

    /**
     * Called from the RESET server
     *
     * @param ride the ride to add to the system
     * @return the added ride on success or Error message
     */
    public static String publishRide(Ride ride) {

        ServerManager sm = ServerManager.getInstance();
        Server leader_server = sm.getLeader(ride.getStartPosition().getName());
        String result = "";
        if (sm.getServer().getName().equals(leader_server.getName())) {
            Ride new_ride = sm.addRideBroadCast(ride);
            result = new_ride.toString();
        } else {
            sscClient grpc_client = leader_server.getGrpcClient();
            // @TODO: add retry for a couple of times in case the leader failed while broadcasting - we want to send this to the new leader
            result = grpc_client.addRideLeader(ride).toString();
        }
        return result;
    }

    public static String reserveRide(Reservation reservation) {
        final List<String> path = reservation.getPath();
        assert path.size() >= 2;
        String result = "";
        if (path.size() == 2) {
            result = reserveOneRide(reservation);
        } else {
            result = reservePath(reservation);
        }
        return result;
    }

    /**
     * Called from the RESET server
     */
    public static String getSnapshot() {
        ServerManager sm = ServerManager.getInstance();
        final String this_server_name = sm.getServer().getName();
        Map<String, Server> system_shards = sm.getSystemShards();
        final int shards_n = system_shards.size();
        logger.log(Level.INFO, "Snapshot request, getting rides from shards leaders, shards_n = " + shards_n);
        logger.log(Level.INFO, "Calling server: " + this_server_name);
        List<Ride> all_rides = new ArrayList<>();
        Set<Server> servers_to_add = new HashSet<>(system_shards.values());
        CountDownLatch latch = new CountDownLatch(servers_to_add.size());

        servers_to_add.forEach(server -> {
            assert server != null;
            final String current_server_name = server.getName();
            if (current_server_name.equals(this_server_name)) {
                logger.log(Level.FINE, "Getting rides from the calling server: " + server.getName());
                synchronized (all_rides) {
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
        stringBuilder.append("Finished getting rides, total of ").append(all_rides.size()).append(" ride(s)\n");
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
                logger.log(Level.FINE, "Asking leader: [" + leader.getName() + "] for rides...");
                if (leader.getName().equals(serverManager.getServer().getName())) {
                    logger.log(Level.FINE, "Trying to reserve on local rides");
                    reserved_ride = serverManager.reserveOneRideIfAvailableAndBroadCast(reservation);
                } else {
                    sscClient grpc_client = leader.getGrpcClient();
                    logger.log(Level.FINE, "Initializing GRPC request to reserve one ride...");
                    reserved_ride = grpc_client.blockingReserveRide(reservation.getReservationForGRPC());
                }
                if (!reserved_ride.isNull()) {
                    logger.log(Level.FINER, "Found ride with leader [" + leader.getName() + "] !");
                    break;
                }
            }

            if (reserved_ride.isNull()) {
                return "No available ride found!\n";
            }
            return reserved_ride.toString();
        }
    }

    private static String reservePath(Reservation reservation) {
        synchronized (ServerManager.getInstance()) {
            // global lock
            String g_lock = ServerManager.getInstance().gLock();

            ServerManager serverManager = ServerManager.getInstance();
            City start_city = serverManager.getCity(reservation.getPath().get(0));
            Set<Server> leaders = new HashSet<>(serverManager.getSystemShards().values());
            List<Ride> all_relevant_rides = new ArrayList<>();
            Map<Server, StreamObserver<generated.reservation>> server_observer = new HashMap<Server, StreamObserver<generated.reservation>>();

            List<String> path = reservation.getReservationForGRPC().getPathList();
            logger.log(Level.FINER, "Getting all relevant rides from leaders");
            CountDownLatch countDownLatch = new CountDownLatch(leaders.size());
            for (Server leader : leaders) {
                if (leader.getName().equals(serverManager.getServer().getName())) {
                    synchronized (all_relevant_rides) {
                        all_relevant_rides.addAll(serverManager.getRidesForPath(path));
                        countDownLatch.countDown();
                        //TODO: he should also use a semaphore to lock himself, he is only locking everyone and then using his rides without locking himself, someone else can come and lock him
                    }
                } else {
                    sscClient grpc_client = leader.getGrpcClient();
                    server_observer.put(leader, grpc_client.nonBlockingReserveRide(reservation.getReservationForGRPC(), all_relevant_rides, countDownLatch));
                }
            }
            logger.log(Level.FINER, "Got all the rides from leaders");
            //wait for everyone
            try {
                logger.log(Level.FINER, "Waiting for servers to respond");
                if (!countDownLatch.await(10, TimeUnit.SECONDS)) {
                    logger.log(Level.FINER, "Time is up, releasing everyone"); //TODO: remove
                    server_observer.values().forEach(StreamObserver::onCompleted);
                    serverManager.releaseGLock(g_lock);
                    logger.log(Level.WARNING, "Timeout while waiting for servers response");
                    return "Server connection error\n";
                }


                //release glock
                serverManager.releaseGLock(g_lock);

                //release irrelevant servers (that we contacted using grpc
                List<Server> relevant_servers = all_relevant_rides.stream()
                        .map(ride -> ServerManager.getInstance().getLeader(ride.getStartPosition().getName()))
                        .distinct().collect(Collectors.toList());

                // Removing this server (if it does exist, as we did not init grpc call on it)
                relevant_servers.remove(serverManager.getServer());

                server_observer.entrySet().stream()
                        .filter((entry) -> !relevant_servers.contains(entry.getKey()))
                        .forEach(entry -> entry.getValue().onCompleted());


                //backtrack
                PathSolver path_solver = new PathSolver(all_relevant_rides, path);
                List<Ride> rides_to_reserve = path_solver.solve();

                if (rides_to_reserve == null) {
                    relevant_servers.forEach(relevant -> server_observer.get(relevant).onCompleted()); //TODO: should we catch exception?
                    return "No available ride found!\n";
                }

                //broadcast
                final Map<Server, List<String>> message_map = new HashMap<>();
                rides_to_reserve.forEach(ride -> {
                    ride.reserve(reservation);
                    Set<Server> followers = serverManager.getCityFollowers(ride.getStartPosition());
                    followers.add(serverManager.getLeader(ride.getStartPosition().getName()));
                    String message = MessagesManager.MessageFactory.updateRideBroadCastMessage(ride);
                    MessagesManager.MessageFactory.appendMessageToServers(followers, message, message_map);
                });

                // Atomically reserve all rides.
                serverManager.atomicBroadcast(message_map);


                //release everyone
                relevant_servers.forEach(relevant -> server_observer.get(relevant).onCompleted()); //TODO: should we catch exception?

                return rides_to_reserve.stream().map(Ride::toString).collect(Collectors.joining("----\n")); //TODO: think about it
            } catch (InterruptedException e) {
                serverManager.releaseGLock(g_lock);
                server_observer.values().forEach(StreamObserver::onCompleted);
                e.printStackTrace();
            }
            return "Error reserving path\n";
        }
    }
}
