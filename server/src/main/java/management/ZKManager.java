package management;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import entities.City;
import entities.Ride;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;


public class ZKManager {
    private final CountDownLatch connectedSignal = new CountDownLatch(1);
    private static final Logger logger = Logger.getLogger(ZKManager.class.getName());
    private static final int SESSION_TIME_OUT = 3000;
    private ZooKeeper zooKeeper;
    private final String host_addr;
    private boolean isConnected = false;

    public ZKManager(String address) {
        host_addr = address;
    }

    public boolean connect() {
        try {
            zooKeeper = new ZooKeeper(host_addr, SESSION_TIME_OUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    isConnected = event.getState() == Event.KeeperState.SyncConnected;
                    connectedSignal.countDown();
                }
            });
            connectedSignal.await();
            if (!isConnected) {
                return false;
            }
            ServerManager sm = ServerManager.getInstance();
            Path active_node = Paths.get(ZKPaths.ACTIVE, sm.getServer().getName());
            zooKeeper.addWatch(ZKPaths.ACTIVE, active_watch, AddWatchMode.PERSISTENT);
            zooKeeper.create(active_node.toString(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);


            for (String lead_shard : sm.getServer().getShards()) {
                Path election_path = Paths.get(ZKPaths.ELECTIONS, lead_shard);
                zooKeeper.addWatch(election_path.toString(), election_watch, AddWatchMode.PERSISTENT);
                Path shard_path = Paths.get(election_path.toString(), sm.getServer().getName() + "-");
                zooKeeper.create(shard_path.toString(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            }
            return true;
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
        return false;
    }

    private final Watcher active_watch = (event) -> {
        try {
            ServerManager.getInstance().updateAliveServers(zooKeeper.getChildren(event.getPath(), false));
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    };
    private final Watcher election_watch = (event) -> {
        Path path = Paths.get(event.getPath());
        String shard = path.getFileName().toString();
        try {
            zooKeeper.getChildren(path.toString(), false)
                    .stream()
                    .min((str1, str2) -> {
                        int i1 = Integer.parseInt(str1.substring(str1.lastIndexOf("-") + 1));
                        int i2 = Integer.parseInt(str2.substring(str2.lastIndexOf("-") + 1));
                        return i1 - i2;
                    })
                    .flatMap(min_shard -> ServerManager.getInstance()
                            .getAliveServers().stream()
                            .filter(ser -> ser.getName().equals(min_shard.substring(0, min_shard.lastIndexOf("-"))))
                            .findAny()).ifPresent(leader_server -> ServerManager.getInstance().updateShardLeader(shard, leader_server));
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    };

    public boolean updateCityNeighbor(City city, Ride ride, int offset) {
        String city_path = Paths.get(ZKPaths.CITIES, city.getName(), ride.getStartPosition().getName()).toString();
        Stat child_state = new Stat();
        byte[] child_data;
        JsonObject json_data = new JsonObject();
        var ref = new Object() {
            int new_val = -1;
        };
        try {
            child_data = zooKeeper.getData(city_path, false, null);
            json_data = JsonParser.parseString(new String(child_data, StandardCharsets.UTF_8)).getAsJsonObject();
            Optional<JsonElement> optionalJsonElement = Optional.of(json_data.get(ride.getEndPosition().getName()));
            ref.new_val = offset;
            optionalJsonElement.ifPresent(jsonElement -> ref.new_val += jsonElement.getAsInt());
            assert ref.new_val >= 0;
            json_data.add(ride.getEndPosition().getName(), new JsonPrimitive(ref.new_val));
        } catch (KeeperException e) {
            if (e.code() == KeeperException.Code.NONODE) {
                assert offset > 0;
                ref.new_val = offset;
                json_data.add(ride.getEndPosition().getName(), new JsonPrimitive(offset));
                try {
                    zooKeeper.create(city_path, json_data.toString().getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                } catch (KeeperException | InterruptedException keeperException) {
                    keeperException.printStackTrace();
                    return false;
                }
            } else {
                e.printStackTrace();
                return false;
            }
        } catch (InterruptedException ignored) {

        }
        return false;
    }

    private static class ZKPaths {
        public static String ROOT = "/";
        public static String ELECTIONS = Paths.get(ROOT, "elections").toString();
        public static String ACTIVE = Paths.get(ROOT, "active").toString();
        public static String CITIES = Paths.get(ROOT, "cities").toString();
    }
}
