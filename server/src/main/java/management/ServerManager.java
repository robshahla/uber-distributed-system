package management;

import entities.City;
import entities.Reservation;
import entities.Ride;
import grpc.GrpcMain;
import grpc.sscClient;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
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
        return primary_shards;
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
    private Map<String, Server> system_shards;

    /**
     * Contains all servers in the system (dead and alive!)
     */
    private Map<String, Server> system_servers;


    /**
     * Represents the name of the shards that this server is leader for.
     */
    private ArrayList<String> primary_shards;

    /**
     * Represents all the rides from cities which this leader is responsible for (@field shards)
     */
    private ArrayList<Ride> rides;

    private static ServerManager instance = null;

    public ZKManager zk;


    private ServerManager() {
        cities = new HashSet<>();
        rides = new ArrayList<>();
        system_shards = new HashMap<>();
        primary_shards = new ArrayList<>();
        system_servers = new HashMap<>();
    }


    public static ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager();
            instance.logger = Logger.getLogger(ServerManager.class.getName());
        }
        return instance;
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
            JSONObject city_info = (JSONObject) obj;
            String city_name = city_info.get("city-name").toString();
            double x = Double.parseDouble(city_info.get("X").toString());
            double y = Double.parseDouble(city_info.get("Y").toString());
            String shard = city_info.get("shard").toString();
            City city = new City(city_name, x, y, shard);
            cities.add(city);
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
            system_servers.put(serverName, server);

            if (serverName.equals(server_name)) {
                this.current_server = server;
            }

        });
        return true;
    }

    public boolean start() {
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(ch.qos.logback.classic.Level.ERROR);
        zk.connect();
        zk.updateCityNeighbor(cities.stream().findAny().get(), null, 12);
        GrpcMain.run(current_server.getGrpcPort());
        RestMain.run(current_server.getRestAddress());
        return true;
    }

    /**
     * bradcast a given ride to all the followers. Invoked by a leader.
     */
    public String broadcastRide(Ride ride) {
        City start_city = cities.stream().filter(city -> {
            return city.getName().equals(ride.getStartPosition());
        }).findAny().orElse(null);
        assert start_city != null;
        system_servers.values().stream()
                .filter(server -> server.shards.contains(start_city.getShard()))
                .forEach(server -> {
                    sscClient grpc_client = new sscClient(server.getGrpcAddress());
                    grpc_client.addRideFollower(ride);
                });
        return "";
    }

    /**
     * given a city name, return the current leader server for this city.
     */
    public Server getLeader(String start_city) {
        City needed_city = cities.stream().filter(city -> {
            return city.getName().equals(start_city);
        }).findAny().orElse(null);

        assert needed_city != null;
        return system_shards.get(needed_city.getShard());
    }

    public String saveRide(Ride ride) {
        Server leader_server = getLeader(ride.getStartPosition().getName());
        if (current_server.getName().equals(leader_server.getName())) {
            updateZk(ride, ride.getVacancies());
            addRide(ride);
            broadcastRide(ride);
            return ride.toString();
        }
        sscClient grpc_client = new sscClient(leader_server.getGrpcAddress());
        grpc_client.addRideLeader(ride); // @TODO: add retry for a couple of times in case the leader failed while broadcasting - we want to send this to the new leader
        return ride.toString();
    }

    /**
     * add info abou the ride in zookeeper for the neighbors.
     */
    public void updateZk(Ride ride, int offset) {
        cities.stream()
                .filter(city ->
                        !getLeader(city.getName()).getName().equals(current_server.getName()) &&
                                ride.isCityNeighbor(city)
                ).forEach(city -> zk.updateCityNeighbor(city, ride, offset));
    }

    /**
     * add a given ride to the rides array only if there doesn't exist another ride with the same parameters.
     * Important: We want this to be idempotent, in case there was a leader change and some followers executed it while
     * others didn't (this happens if the leader fails while broadcasting). To prevent this we retry with several leaders, and thus,
     * a new leader will broadcast again, and a follower might receive a command to add the same ride several times.
     */
    public void addRide(Ride ride) {
        boolean ride_exists = rides.stream().anyMatch(element ->
                element.getFirstName().equals(ride.getFirstName()) &&
                        element.getLastName().equals(ride.getLastName()) &&
                        element.getStartPosition().equals(ride.getStartPosition()) &&
                        element.getDepartureTime().equals(ride.getDepartureTime()));
        if (!ride_exists) {
            rides.add(ride);
        }
    }

    public String reserveRide(Reservation reservation) {
        assert reservation.getPath().length >= 2; // we assume that a client won't request a reservation with a path of length less than 2
        if (reservation.getPath().length == 2) {
            return reserveOneRide(reservation);
        }

        // return reservePath(reservation); // TODO: should think how to implement this
        return ""; // TODO: remove
    }

    public String reserveOneRide(Reservation reservation) {
        Server leader_server = getLeader(reservation.getPath()[0]);
        if (current_server.getName().equals(leader_server.getName())) {
            String ride_status = addReservation(reservation);
            if (ride_status.equals("No ride was found")) {
                return ride_status;
            }
            //broadcastReservation()
            return ride_status;
        }
        sscClient grpc_client = new sscClient(leader_server.getGrpcAddress());
//        return grpc_client.reserveRideLeader(reservation); // @TODO: add retry for a couple of times in case the leader failed while broadcasting - we want to send this to the new leader, this should be idempotent
        return ""; // TODO: remove
    }

    public String addReservation(Reservation reservation) {
        // TODO: check if there is a suitable ride, if so reserve it and return ride.toString(), otherwise return "No ride was found"
        return "";
    }

    public String getSnapshot() {
        CountDownLatch latch = new CountDownLatch(system_shards.size());
        ArrayList<String> rides = new ArrayList<>();
        for (Server server : system_shards.values()) {
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


    public static class Server {
        private final String name;
        private String grpc_address, rest_address;
        private ArrayList<String> shards;
        private long heartbeat_time = 0;
        private static long current_time = 0;


        public Server(String name) {
            this(name, "", "");
        }

        public Server(String name, String grpc_address, String rest_address) {
            this.name = name;
            this.grpc_address = grpc_address;
            this.rest_address = rest_address;
            heartbeat();
        }

        public ArrayList<String> getShards() {
            return shards;
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

        public boolean isAlive() {
            return heartbeat_time == current_time;
        }

        public void heartbeat() {
            heartbeat_time = current_time;
        }

        public static void killAll() {
            current_time++;
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

    public Server getServer() {
        return current_server;
    }

    public Collection<Server> getAliveServers() {
        return system_servers.values();
    }

    public Map<String, Server> getSystem_shards() {
        return system_shards;
    }


    public void updateShardLeader(String shard, Server server) {
        logger.log(Level.INFO, "new shard leader" + shard + " server=" + server.getName());
        system_shards.put(shard, server);
    }

    public void updateAliveServers(List<String> alive) {
        Server.killAll();
        alive.forEach(server_name -> system_servers.get(server_name).heartbeat());
    }

    public City getCity(String city_name) {
        return cities.stream().filter(city -> city.getName().equals(city_name)).findAny().orElse(null);
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
