package grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.util.Scanner;

public class GrpcMain {
    public static void run(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            if (port == 1234) { // we are server

                sscServer server = new sscServer(port);
                server.start();
//                Thread.sleep(30000);
//                System.out.println("Ending :D");
            } else {
                String target = "localhost:1234";
                sscClient client = new sscClient(target);
                client.upsert();
                sscServer server = new sscServer(port);
                server.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
