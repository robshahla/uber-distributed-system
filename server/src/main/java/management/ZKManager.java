package management;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return isConnected;
    }
}
