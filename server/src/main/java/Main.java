import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            Scanner s = new Scanner(System.in);
            int port = 1234;
            if (port == 1234) { // we are server

                sscServer server = new sscServer(port);
                server.start();
                Thread.sleep(30000);
                System.out.println("Ending :D");
            } else {
                String target = "localhost:1234";
                ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                sscClient client = new sscClient(channel);
                client.upsert();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


    }
}
