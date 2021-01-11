package grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.util.Scanner;

public class GrpcMain {
    public static void run(String port) {
        try {
            sscServer server = new sscServer(Integer.parseInt(port));
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
