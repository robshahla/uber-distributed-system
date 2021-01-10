import grpc.*;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rest.host.RestMain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.logging.LogManager;


public class Main implements Watcher {

    final CountDownLatch connectedSignal = new CountDownLatch(1);
    public ZooKeeper zk;

    public static void main(String[] args) {
        String[] port = {"8081"};
        RestMain.run(port);

//        Main runnn = new Main();
//        try {
//            ACL acl = new ACL();
//            ArrayList<ACL> acls = new ArrayList<>();
//            acls.add(acl);
//
//            System.out.println("Here");
//            System.out.println("path1=" + runnn.zk.create("/emil1", "3omar".getBytes(StandardCharsets.UTF_8),
//                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL));
//
//            List<String> child = runnn.zk.getChildren("/", runnn);
//            for (String i : child) {
//                System.out.println(i);
//            }
//            Thread.sleep(10000);
//            System.out.println("Exiting!!!");
//
//        } catch (KeeperException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
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