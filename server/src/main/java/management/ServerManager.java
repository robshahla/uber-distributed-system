package management;

import entities.City;
import entities.Reservation;
import entities.Ride;
import grpc.GrpcMain;
import grpc.sscClient;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import rest.host.RestMain;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerManager {


    private Logger logger;

    public ArrayList<String> getLeaderShards() {
        return leader_shards;
    }

    /**
     * This server name.
     */
    private Server current_server;
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
     * Represents the name of the shards that this server is leader for.
     */
    private ArrayList<String> leader_shards;

    /**
     * Represents all the rides from cities which this leader is responsible for (@field shards)
     */
    private ArrayList<Ride> rides;

    private static ServerManager instance = null;

    public ZKManager zk;

    private ServerManager() {
        cities = new HashSet<>();
        rides = new ArrayList<>();
        leader_shards = new ArrayList<>();
        alive_servers = new ArrayList<>();
    }

    public static ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager();
            instance.logger = Logger.getLogger(ServerManager.class.getName());
        }
        return instance;
    }

    public ZKManager getZKManager() {
        return zk;
    }

    @SuppressWarnings("unchecked")
    public boolean init(String file_path, String server_name) {
        current_server = new Server(server_name);
        Map<String, Object> json_data = getJsonMap(file_path);
        if (json_data == null) {
            return false;
        }
        String zk_addr = (String) json_data.get("zk-address");
        zk = new ZKManager(zk_addr);

        // initializing cities array
        JSONArray city_array = (JSONArray) json_data.get("cities");
        city_array.forEach(obj -> {
            JSONArray city_info = (JSONArray) obj;
            String city_name = ((JSONArray) obj).get(0).toString();
            double x = Double.parseDouble(((JSONArray) obj).get(1).toString());
            double y = Double.parseDouble(((JSONArray) obj).get(2).toString());
            String shard = ((JSONArray) obj).get(3).toString();
            City city = new City(city_name, x, y, shard);
            cities.add(city);
            System.out.println(city);
        });

        // initializing servers array
        JSONArray servers_array = (JSONArray) json_data.get("servers");
        servers_array.forEach(obj -> {
            JSONObject server_object = (JSONObject) obj;
            String serverName = server_object.get("server-name").toString();
            String grpc_address = server_object.get("grpc-address").toString();
            String rest_address = server_object.get("rest-address").toString();
            JSONArray server_shards = (JSONArray) server_object.get("shards");
            Server server = new Server(serverName, grpc_address, rest_address);
            ArrayList<String> shards = new ArrayList<>();
            shards.addAll(server_shards);
            server.setShards(shards);
            alive_servers.add(server);

            if (serverName.equals(server_name)) {
                this.current_server = server;
            }

            System.out.println(server);
        });
        System.out.println(current_server.getGrpcPort());
        return true;
    }

    public boolean start() {
        zk.connect();
        GrpcMain.run(current_server.getGrpcPort());
        RestMain.run(current_server.getRestAddress());
        return true;
    }

    public String broadcastRide(Ride ride) {
        City start_city = cities.stream().filter(city -> {
            return city.getName().equals(ride.getStartPosition());
        }).findAny().orElse(null);
        assert start_city != null;
        alive_servers.stream()
                .filter(server -> server.shards.contains(start_city.getShard()))
                .forEach(server -> {
                    sscClient grpc_client = new sscClient(server.getGrpcAddress());
                    grpc_client.addRideLeader(ride, true);
                });
        return "";
    }

    public String saveRide(Ride ride) {
        City start_city = cities.stream().filter(city -> {
            return city.getName().equals(ride.getStartPosition());
        }).findAny().orElse(null);
        assert start_city != null;
        Server leader_server = shard_leaders.get(start_city.getName());

        if (current_server.getName().equals(leader_server.getName())) {
            rides.add(ride);
            broadcastRide(ride);
            return ride.toString();
        }
        sscClient grpc_client = new sscClient(leader_server.getGrpcAddress());
        grpc_client.addRideLeader(ride, false);
        return ride.toString();
    }

    public String reserveRide(Reservation reservation) {
        for (String city : reservation.getPath()) {
            System.out.println(city);
        }
        return "";
    }

    public String getSnapshot() {
        CountDownLatch latch = new CountDownLatch(shard_leaders.size());
        ArrayList<String> rides = new ArrayList<>();
        for (Server server : shard_leaders.values()) {
            sscClient grpc_client = new sscClient(server.getGrpcAddress());
            grpc_client.getRides(rides, latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error getting snapshot");
            e.printStackTrace();
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        rides.forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    public ArrayList<Ride> getRides() {
        return rides;
    }

    public void addRide(Ride ride) {

    }

    private class Server {
        private final String name;
        private String grpc_address, rest_address;
        private ArrayList<String> shards;

        public Server(String name) {
            this.name = name;
        }

        public Server(String name, String grpc_address, String rest_address) {
            this.name = name;
            this.grpc_address = grpc_address;
            this.rest_address = rest_address;
        }

        public void setShards(ArrayList<String> shards) {
            this.shards = shards;
        }

        @Override
        public String toString() {
            return "Server{" +
                    "name='" + name + '\'' +
                    ", grpc_address='" + grpc_address + '\'' +
                    ", rest_address='" + rest_address + '\'' +
                    '}';
        }

        public String getName() {
            return name;
        }

        public String getGrpcAddress() {
            return grpc_address;
        }

        public String getGrpcPort() {
            return grpc_address.split(":")[1];
        }

        public String getRestPort() {
            return rest_address.split(":")[1];
        }

        public String getRestAddress() {
            return rest_address.split(":")[1];
        }

        public void setGrpcAddress(String grpc_address) {
            this.grpc_address = grpc_address;
        }

        public void setRestAddress(String rest_address) {
            this.rest_address = rest_address;
        }
    }

    public String getCityShard(String city_name) {
        City city = cities.stream()
                .filter(elem -> elem.getName().equals(city_name))
                .findAny()
                .orElse(null);
        assert city != null;
        return city.getShard();
    }

    private Map<String, Object> getJsonMap(String file_path) {
        JSONParser jsonParser = new JSONParser();
        Map<String, Object> json_data = new HashMap<String, Object>();
        try (FileReader reader = new FileReader(file_path)) {
            JSONObject main_object = (JSONObject) jsonParser.parse(reader);
            @SuppressWarnings("unchecked")
            Set<Map.Entry<String, Object>> set = main_object.entrySet();
            for (Map.Entry<String, Object> entry : set) {
                json_data.put(entry.getKey(), entry.getValue());
            }
        } catch (IOException | ParseException e) {
            logger.log(Level.SEVERE, "Could not config server: " + current_server.getName());
            e.printStackTrace();
            return null;
        }
        return json_data;
    }
}
