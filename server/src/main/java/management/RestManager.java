package management;

import entities.Ride;
import entities.Server;
import grpc.sscClient;

public class RestManager {

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

}
