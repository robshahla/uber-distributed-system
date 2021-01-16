package management;

import entities.Server;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import static management.MessagesManager.ProcessMessage;


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
            try {
                zooKeeper.delete(active_node.toString(), -1);
            } catch (KeeperException.NoNodeException ignored) {
            }
            zooKeeper.create(active_node.toString(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            for (String shard : sm.getSystemShards().keySet()) {
                Path election_path = Paths.get(ZKPaths.ELECTIONS, shard);
                zooKeeper.addWatch(election_path.toString(), shard_election_watch, AddWatchMode.PERSISTENT);
                if (sm.getServer().getShards().contains(shard)) {
                    Path shard_path = Paths.get(election_path.toString(), sm.getServer().getName() + "-");
                    zooKeeper.create(shard_path.toString(), null,
                            ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                }
            }
            sm.getSystemShards().keySet().stream()
                    .map(shard -> Paths.get(ZKPaths.ELECTIONS, shard))
                    .forEach(this::updateLeader);

            String server_mailbox_path = Paths.get(ZKPaths.MESSAGES, ServerManager.getInstance().getServer().getName()).toString();
            try {
                zooKeeper.create(server_mailbox_path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } catch (InterruptedException | KeeperException ignored) {

            }
            zooKeeper.addWatch(server_mailbox_path, message_received_watch, AddWatchMode.PERSISTENT);
            return true;
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
        return false;
    }


    public int generateUniqueId() {
        String path = ZKPaths.COUNTER;
        String created_id;
        try {
            created_id = zooKeeper.create(Paths.get(path, "id-").toString(), null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            zooKeeper.delete(created_id, -1);
            return getSequentialNumber(created_id);
        } catch (KeeperException | InterruptedException e) {
            System.out.println("ZooKeeper: Error generating unique id"); //should not get here
        }
        return -1;
    }


    /**
     * Atomically broadcast message to all recipes (servers) or none.
     *
     * @param to_servers servers name to send the message to.
     * @param message    the message to sent
     */
    public void atomicBroadCastMessage(Set<Server> to_servers, String message) {
        List<Op> create_node_ops = new ArrayList<>();
        String message_node_name = ServerManager.getInstance().getServer().getName() + "-";
        byte[] message_bytes = message.getBytes(StandardCharsets.UTF_8);
        to_servers.stream().filter(Server::isAlive).forEach(server -> {
            String path_to_node = Paths.get(ZKPaths.MESSAGES, server.getName(), message_node_name).toString();
            Op operation = Op.create(path_to_node, message_bytes,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
            create_node_ops.add(operation);
        });
        try {
            zooKeeper.multi(create_node_ops);
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------ PRIVATE METHODS -------------------------------------------------

    private final Watcher active_watch = (event) -> {
        try {
            ServerManager.getInstance().updateAliveServers(zooKeeper.getChildren(event.getPath(), false));
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    };

    private final Watcher message_received_watch = (event) -> {
        /**
         * 1) getChildren(event.getPath) (actually the messages)
         * 2) process the messages
         * 3) delete the node_messages
         */
        try {
            List<String> message_children = zooKeeper.getChildren(event.getPath(), false);
            message_children.stream().sorted((str1, str2) -> {
                int i1 = getSequentialNumber(str1);
                int i2 = getSequentialNumber(str2);
                return i1 - i2;
            }).forEachOrdered(child_message_node -> {
                String child_path = Paths.get(event.getPath(), child_message_node).toString();
                Stat child_data_stat = new Stat();
                try {
                    byte[] message_data_bytes = zooKeeper.getData(child_path, false, child_data_stat);
                    ProcessMessage(message_data_bytes);
                    zooKeeper.delete(child_path, -1);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }


    };

    private final Watcher shard_election_watch = (event) -> {
        updateLeader(Paths.get(event.getPath()));
    };

    private void updateLeader(Path shard_path) {
        String shard = shard_path.getFileName().toString();
        try {
            zooKeeper.getChildren(shard_path.toString(), false)
                    .stream()
                    .min((str1, str2) -> {
                        int i1 = getSequentialNumber(str1);
                        int i2 = getSequentialNumber(str2);
                        return i1 - i2;
                    })
                    .flatMap(min_shard -> ServerManager.getInstance()
                            .getAliveServers().stream()
                            .filter(ser -> ser.getName().equals(min_shard.substring(0, min_shard.lastIndexOf("-"))))
                            .findAny()).ifPresent(leader_server -> ServerManager.getInstance().updateShardLeader(shard, leader_server));

        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class ZKPaths {
        public static final String ROOT = "/Uber";
        public static final String ELECTIONS = Paths.get(ROOT, "elections").toString();
        public static final String ACTIVE = Paths.get(ROOT, "active").toString();
        public static final String MESSAGES = Paths.get(ROOT, "mailbox").toString();
        public static final String COUNTER = Paths.get(ROOT, "id-generator").toString();
    }


    private int getSequentialNumber(String node_name) {
        assert node_name.contains("-");
        return Integer.parseInt(node_name.substring(node_name.lastIndexOf("-") + 1));
    }
}