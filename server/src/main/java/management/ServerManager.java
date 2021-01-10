package management;

import com.google.protobuf.DescriptorProtos;
import entities.City;
import entities.Reservation;
import entities.Ride;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
//import org.apache.zookeeper.server.ServerConfig;

public class ServerManager {


    public ArrayList<String> getLeaderShards() {
        return leader_shards;
    }

    /**
     * This server name.
     */
    private Server server;
    /**
     * Saves All cities in the system.
     */
    private Set<City> cities;
    /**
     * Maps shard name to its server leader name
     */
    private Map<String, Server> shard_leaders;

    private ArrayList<Server> alive_servers;
    /**
     * Represetns the name of the shards that this server is responsible for.
     */
    private Set<String> shards;

    /**
     * Represents the name of the shards that is server is leader for.
     */
    private ArrayList<String> leader_shards;

    /**
     * Represents all the rides from cities which this leader is responsible for (@field shards)
     */
    private ArrayList<Ride> rides;

    private static ServerManager instance = null;

    public ZKManager zk;

    private ServerManager() {
    }

    public static ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager();
        }
        return instance;
    }

    public ZKManager getZKManager() {
        return zk;
    }

    public void init() {
    }

    public String saveRide(Ride ride) {
        return ride.toString();
    }

    public String reserveRide(Reservation reservation) {
        for (String city : reservation.getPath()) {
            System.out.println(city);
        }
        return "";
    }

    public String getSnapshot() {

        return "";
    }

    public ArrayList<Ride> getRides() {
        return rides;
    }

    private class Server {
        public String name;
        public String grpc_address, rest_address;

        public Server(String name, String grpc_address, String rest_address) {
            this.name = name;
            this.grpc_address = grpc_address;
            this.rest_address = rest_address;
        }
    }

    public String getCityShard(String city_name) {
        for (City city : cities) {
            if (city.getName().equals(city_name)) {
                return city.getShard();
            }
        }
        return "";
    }
}
