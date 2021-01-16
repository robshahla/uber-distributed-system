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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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
        zooKeeper = null;
    }

    public boolean connectToMailbox() {
        String server_name = ServerManager.getInstance().getServer().getName();
        String server_mailbox_path = Paths.get(ZKPaths.MESSAGES, server_name).toString();
        logger.log(Level.FINER, "Connecting [" + server_name + "] to MailBox");
        if (createZNode(server_mailbox_path, CreateMode.PERSISTENT) == null) return false;
        addPersistentZNodeWatch(server_mailbox_path, message_received_watch);
        logger.log(Level.FINER, "Server [" + server_name + "] connected to mailbox!");
        return true;
    }

    public boolean connect() {
        try {
            logger.log(Level.FINER, "Connecting to zookeeper on host=" + host_addr);
            zooKeeper = new ZooKeeper(host_addr, SESSION_TIME_OUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    isConnected = event.getState() == Event.KeeperState.SyncConnected;
                    connectedSignal.countDown();
                }
            });
            if (!connectedSignal.await(5, TimeUnit.SECONDS)) {
                zooKeeper = null;
                logger.log(Level.SEVERE, "Error connecting to zookeeper! (TIMEOUT)");
                return false;
            }
            if (!isConnected) {
                zooKeeper = null;
                logger.log(Level.SEVERE, "Error connecting to zookeeper! (Fail)");
                return false;
            }
            logger.log(Level.FINER, "Connected to zookeeper!");
            if (!connectToActive()) return false;

            //
            participateInElections();
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }


    public int generateUniqueId() {
        String path = ZKPaths.COUNTER;
        String created_id;
        logger.log(Level.FINER, "Generating new id...");
        created_id = createZNode(Paths.get(path, "id-").toString(),
                CreateMode.EPHEMERAL_SEQUENTIAL);
        if (created_id == null) return -1;
        deleteZNode(created_id);
        int generated_id = getSequentialNumber(created_id);
        logger.log(Level.FINER, "New Id generated = " + generated_id);
        return generated_id;
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
    private void participateInElections() {
        ServerManager sm = ServerManager.getInstance();
        for (String shard : sm.getSystemShards().keySet()) {
            Path election_path = Paths.get(ZKPaths.ELECTIONS, shard);
            addPersistentZNodeWatch(election_path.toString(), shard_election_watch);
            if (sm.getServer().getShards().contains(shard)) {
                Path shard_path = Paths.get(election_path.toString(), sm.getServer().getName() + "-");
                createZNode(shard_path.toString(), CreateMode.EPHEMERAL_SEQUENTIAL);
            }
        }
        sm.getSystemShards().keySet().stream()
                .map(shard -> Paths.get(ZKPaths.ELECTIONS, shard))
                .forEach(this::updateLeader);

    }

    private boolean connectToActive() {
        ServerManager sm = ServerManager.getInstance();
        Path active_node = Paths.get(ZKPaths.ACTIVE, sm.getServer().getName());
        addPersistentZNodeWatch(ZKPaths.ACTIVE, active_watch);
        deleteZNode(active_node.toString());
        return createZNode(active_node.toString(), CreateMode.EPHEMERAL) != null;
    }

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


    private int getSequentialNumber(String node_name) {
        assert node_name.contains("-");
        return Integer.parseInt(node_name.substring(node_name.lastIndexOf("-") + 1));
    }

    private void addPersistentZNodeWatch(String path_to_node, Watcher watcher) {
        try {
            logger.log(Level.FINEST, "Adding watch to ZNode [" + path_to_node + "]");
            zooKeeper.addWatch(path_to_node, watcher, AddWatchMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            logger.log(Level.WARNING, "Failed to add watcher to node [" +
                    path_to_node +
                    "]\n\tErrorr:" +
                    e.getMessage());
        }
    }

    private void deleteZNode(String path_to_node) {
        try {
            logger.log(Level.FINEST, "Deleting ZNode" +
                    path_to_node + "]");
            zooKeeper.delete(path_to_node, -1);
        } catch (InterruptedException | KeeperException e) {
            logger.log(Level.WARNING, "Failed to delete  ZNode [" +
                    path_to_node +
                    "]\n\tErrorr:" +
                    e.getMessage());
        }
    }

    private String createZNode(String path_to_node, CreateMode mode) {
        assert zooKeeper != null;
        try {
            logger.log(Level.FINEST, "Creating new ZNode (With No data):\n\tpath: ["
                    + path_to_node + "]\n\tType: [" + mode + "]");
            String created_node = zooKeeper.create(path_to_node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
            logger.log(Level.FINEST, "Successfully created node: " + created_node);
            return created_node;
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Failed to create ZNode: [" +
                    path_to_node + "]\n\t" +
                    "Error: " +
                    e.getMessage());
        }
        return null;
    }


    private static class ZKPaths {
        public static final String ROOT = "/Uber";
        public static final String ELECTIONS = Paths.get(ROOT, "elections").toString();
        public static final String ACTIVE = Paths.get(ROOT, "active").toString();
        public static final String MESSAGES = Paths.get(ROOT, "mailbox").toString();
        public static final String COUNTER = Paths.get(ROOT, "id-generator").toString();
    }


}