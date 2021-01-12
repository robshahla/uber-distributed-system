
import management.ServerManager;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main implements Watcher {

    final CountDownLatch connectedSignal = new CountDownLatch(1);
    public ZooKeeper zk;
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws InterruptedException {
        ServerManager sm = ServerManager.getInstance();
        String filePath = "/Users/Emil/IdeaProjects/ds-hw2/server/src/main/java/config.json";
        String server_name = "server-1";
        logger.log(Level.INFO, "Initializing server: " + server_name);
        if (!sm.init(filePath, server_name)) {
            logger.log(Level.SEVERE, "Could not initialize server: " + server_name);
            return;
        }
        sm.start();
    }


    public Main() {
        try {
            zk = new ZooKeeper("localhost:2181", 3000, this); // to run in docker container get ip of the container running zookeeper, for example 172.17.0.2, and instead of localhost:2128 write 172.17.0.2:2181.
            connectedSignal.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
            System.out.println("ZooKeeper CONNECTED!");
            connectedSignal.countDown();
        } else {
            System.out.print("ZooKeeper COULD NOT CONNECT!");
        }
    }
}


//  /elections/shard1 /elections/shard2 /elections/shard3 .. /elections/shardn

// /active/server-1 /active/server-2 /active/server-3 .. /active/server-k   EPH