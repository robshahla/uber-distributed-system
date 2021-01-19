package management;

import entities.City;
import entities.Reservation;
import entities.Ride;
import entities.Server;
import grpc.GrpcMain;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import rest.host.RestMain;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * Singleton Class which manges server data structures and communications
 */
public class ServerManager {


    private static ServerManager instance = null;
    /**
     * Saves All cities in the system.
     */
    private final Set<City> cities;
    /**
     * Maps shard name to its server leader name
     */
    private final Map<String, Server> system_shards;
    /**
     * Contains all servers in the system (dead and alive!)
     */
    private final Map<String, Server> system_servers;
    /**
     * Represents the name of the shards that this server is leader for.
     */
    private final ArrayList<String> primary_shards;
    /**
     * Represents all the rides from cities which this leader is responsible for (@field shards)
     */
    private final List<Ride> rides;
    public ZKManager zk;
    //    private static final Logger logger = Logger.getLogger(ServerManager.class.getName());
    private static MessagesManager logger = MessagesManager.instance;
    /**
     * This server name.
     */
    private Server current_server;

    public static ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager();
        }
        return instance;
    }

    public ArrayList<String> getLeaderShards() {
        return primary_shards;
    }

    @SuppressWarnings("unchecked")
    public boolean init(String file_path, String server_name) {
        current_server = new Server(server_name);
        Map<String, Object> json_data = getJsonMap(file_path);
        if (json_data == null) {
            return false;
        }
        String zk_addr = (String) json_data.get("zk-address");
        logger.log(Level.CONFIG, "zk-address = " + zk_addr);
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
            logger.log(Level.CONFIG, "Adding City: " + city.toString());
            cities.add(city);
        });

        // initializing servers array
        Set<String> system_shards_aux = new HashSet<>();
        JSONArray servers_array = (JSONArray) json_data.get("servers");
        servers_array.forEach(obj -> {
            JSONObject server_object = (JSONObject) obj;
            String serverName = server_object.get("server-name").toString();
            String grpc_address = server_object.get("grpc-address").toString();
            String rest_address = server_object.get("rest-address").toString();
            JSONArray server_shards = (JSONArray) server_object.get("shards");
            Server server = new Server(serverName, grpc_address, rest_address);
            ArrayList<String> shards = new ArrayList<String>(server_shards);
            system_shards_aux.addAll(shards);
            server.setShards(shards);
            system_servers.put(serverName, server);
            if (serverName.equals(server_name)) {
                this.current_server = server;
            }

        });
        system_shards_aux.forEach(shard -> system_shards.put(shard, null));
        return true;
    }

    public boolean start() {
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
        if (!zk.connect()) return false;
        logger.log(Level.SEVERE, "Hello start!");
        zk.connectToMailbox();
        GrpcMain.run("8000");
        RestMain.run("8080");
        return true;
    }


    /**
     * given a city name, return the current leader server for this city.
     */
    public Server getLeader(String start_city) {
        City needed_city = cities.stream().filter(city -> city.getName().equals(start_city))
                .findAny().orElse(null);

        assert needed_city != null;
        return system_shards.get(needed_city.getShard());
    }

    public Set<Server> getCityFollowers(City needed_city) {
        Server leader_server = system_shards.get(needed_city.getShard());
        String city_shard = needed_city.getShard();
        return system_servers.values().stream()
                .filter(server -> !server.getName().equals(leader_server.getName()) && server.getShards().contains(city_shard))
                .collect(Collectors.toSet());

    }

    public Ride addRideBroadCast(Ride ride) {
        assert ride != null && ride.getVacancies() > 0;
        assert getServer().getName().equals(getLeader(ride.getStartPosition().getName()).getName());
        Ride result_ride = addRide(ride);
        System.out.println("result_ride:" + result_ride);
        if (result_ride.getId() < 0) {
            result_ride.setId(zk.generateUniqueId());
            Set<Server> followers = getCityFollowers(result_ride.getStartPosition());
            Map<Server, List<String>> message_map = new HashMap<>();
            String message = MessagesManager.MessageFactory.newRideBroadCastMessage(result_ride);
            MessagesManager.MessageFactory.appendMessageToServers(followers, message, message_map);
            atomicBroadcast(message_map);
        }
        return result_ride; // response to the user.
    }


    /**
     * add a given ride to the rides array only if there doesn't exist another ride with the same parameters.
     * Important: We want this to be idempotent, in case there was a leader change and some followers executed it while
     * others didn't (this happens if the leader fails while broadcasting). To prevent this we retry with several leaders, and thus,
     * a new leader will broadcast again, and a follower might receive a command to add the same ride several times.
     */
    public Ride addRide(Ride ride) {
        synchronized (rides) {
            Ride existing_ride = this.rides.stream().filter(ride::equals).findAny().orElse(Ride.nullRide());
            System.out.println("Existing ride: " + existing_ride);
            if (existing_ride.isNull()) {
                System.out.println("Exisiting ride is null :)");
                rides.add(ride);
                return this.rides.stream().filter(ride::equals).findAny().orElse(null);
            }
            return existing_ride;
        }
    }

    public Ride reserveOneRideIfAvailableAndBroadCast(Reservation reservation) {
        synchronized (this.rides) {
            assert reservation.getPath().size() == 2;

            final City start_city = getCity(reservation.getPath().get(0));
            final City end_city = getCity(reservation.getPath().get(1));

            // Filter rides before reserving,
            // Get rides that 1) This leader is responsible for their shard
            //                2) The ride can take the person from/to request path

            Ride ride_to_reserve = rides.stream().filter(ride -> isLeaderForRide(ride) &&
                    ride.canServe(start_city, end_city) &&
                    ride.getDepartureTime().equals(reservation.getDepartureTime())
            ).min((ride1, ride2) -> {
                double d1 = ride1.distanceToLine(start_city);
                double d2 = ride2.distanceToLine(start_city);
                if (d1 == d2) return 0;
                else return d1 - d2 < 0 ? -1 : 1;
            }).orElse(Ride.nullRide());


            if (ride_to_reserve.isNull()) {
                logger.log(Level.FINE, "Could not find valid ride for the requested reservation!");
                return Ride.nullRide();
            }

            boolean reserve_result = ride_to_reserve.reserve(reservation);
            assert reserve_result;

            //getting the followers
            Set<Server> followers = getCityFollowers(ride_to_reserve.getStartPosition());
            // Build message to broadcast & broadcast
            Map<Server, List<String>> message_map = new HashMap<>();
            String message = MessagesManager.MessageFactory.updateRideBroadCastMessage(ride_to_reserve);
            MessagesManager.MessageFactory.appendMessageToServers(followers, message, message_map);
            atomicBroadcast(message_map);
            return ride_to_reserve;
        }
    }

    public void updateRide(Ride ride_to_update) {
        synchronized (rides) {
            boolean removed = rides.removeIf(ride -> ride.getId() == ride_to_update.getId());
            if (removed && !ride_to_update.isNull()) {
                rides.add(ride_to_update);
            }
        }

    }


    public void updateShardLeader(String shard, Server new_server) {
        synchronized (system_shards) {
            Server current_server = system_shards.get(shard);
            if (current_server == null || !current_server.equals(new_server)) {
                logger.log(Level.FINEST, "New server leader was elected for shard [" + shard + "]");
                logger.log(Level.FINEST, "Shard [" + shard + "] leader is now server [" + new_server.getName() + "]");
                system_shards.put(shard, new_server);
            }
        }
    }

    public void updateAliveServers(List<String> alive) {
        synchronized (system_servers) {
            Server.killAll();
            alive.forEach(server_name -> system_servers.get(server_name).heartbeat());
            system_servers.values().stream().filter(server -> !server.isAlive()).forEach(Server::shutdown);
        }
    }


    //------------------------------------ Getters  -------------------------------------------------

    public City getCity(String city_name) {
        City found_city = cities.stream().filter(city -> city.getName().equals(city_name)).findAny().orElse(null);
        if (found_city == null) {
            logger.log(Level.SEVERE, "Could not find city [" + city_name + "]");

        }
        return found_city;
    }


    public List<Ride> getRidesForPath(List<String> path) {
        synchronized (rides) {
            List<Pair<String, String>> pairs = new ArrayList<>();
            for (int i = 0; i < path.size() - 1; i++) {
                pairs.add(Pair.of(path.get(i), path.get(i + 1)));
            }
            List<Ride> available_rides = new ArrayList<>();
            pairs
                    .forEach(single_path -> available_rides
                            .addAll(getRidesForCity(single_path.getFirst(), single_path.getSecond())));
            return available_rides;
        }
    }

    public synchronized List<Ride> getRides() {
        return rides.stream().filter(this::isLeaderForRide).collect(Collectors.toList());
    }


    public Server getServer() {
        return current_server;
    }

    public Collection<Server> getAliveServers() {
        return system_servers.values().stream().filter(Server::isAlive).collect(Collectors.toList());
    }

    public Map<String, Server> getSystemShards() {
        return system_shards;
    }


//------------------------------------ ZooKeeper  -------------------------------------------------

    public synchronized void atomicBroadcast(Map<Server, List<String>> message_map) {
        zk.atomicBroadCastMessage(message_map);
    }

    public String gLock() {
        return zk.lock();
    }

    public void releaseGLock(String g_lock) {
        zk.releaseLock(g_lock);
    }

    //------------------------------------ PRIVATE METHODS -------------------------------------------------
    private List<Ride> getRidesForCity(String start_city_name, String end_city_name) {
        City start_city = getCity(start_city_name);
        City end_city = getCity(end_city_name);
        return rides.stream()
                .filter(ride -> ride.canServe(start_city, end_city))
                .collect(Collectors.toList());
    }

    private boolean isLeaderForRide(Ride ride) {
        assert ride != null;
        return system_shards.get(ride.getStartPosition().getShard()).equals(getServer());
    }


    private Map<String, Object> getJsonMap(String file_path) {
        JSONParser jsonParser = new JSONParser();
        Map<String, Object> json_data = new HashMap<>();
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

    private ServerManager() {
        cities = new HashSet<>();
        rides = new ArrayList<>();
        system_shards = new HashMap<>();
        primary_shards = new ArrayList<>();
        system_servers = new HashMap<>();
    }


}
