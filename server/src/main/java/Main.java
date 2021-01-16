import management.ServerManager;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {

    final CountDownLatch connectedSignal = new CountDownLatch(1);
    public ZooKeeper zk;
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws InterruptedException {

        ServerManager sm = ServerManager.getInstance();
        String filePath = args[0]; // config file path
        String server_name = args[1];

        logger.log(Level.INFO, "Initializing server: " + server_name);
        if (!sm.init(filePath, server_name)) {
            logger.log(Level.SEVERE, "Could not initialize server: " + server_name);
            return;
        }
        sm.start();
    }

}
