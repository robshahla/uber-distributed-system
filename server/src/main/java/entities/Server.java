package entities;

import grpc.sscClient;

import java.util.ArrayList;
import java.util.Objects;

public class Server {
    private final String name;
    private final String grpc_address, rest_address;
    private ArrayList<String> shards;
    private long heartbeat_time = 0;
    private static long current_time = 0;
    private sscClient grpc_client;


    public Server(String name) {
        this(name, "", "");
    }

    public Server(String name, String grpc_address, String rest_address) {
        this.name = name;
        this.grpc_address = grpc_address;
        this.rest_address = rest_address;
        heartbeat();
        grpc_client = null;
    }

    public ArrayList<String> getShards() {
        return shards;
    }

    public void setShards(ArrayList<String> shards) {
        this.shards = shards;
    }

    @Override
    public String toString() {
        return "Server{" +
                "name='" + name + '\'' +
                ", grpc_address='" + grpc_address + '\'' +
                ", rest_address='" + rest_address + '\'' +
                ", shards=" + shards +
                '}';
    }



    public String getName() {
        return name;
    }

    public String getGrpcAddress() {
        return grpc_address;
    }

    public String getGrpcPort() {
        return grpc_address.split(":")[1];
    }

    public String getRestPort() {
        return rest_address.split(":")[1];
    }

    public String getRestAddress() {
        return rest_address.split(":")[1];
    }

    public boolean isAlive() {
        return heartbeat_time == current_time;
    }

    public void heartbeat() {
        heartbeat_time = current_time;
    }

    public static void killAll() {
        current_time++;
    }

    public sscClient getGrpcClient() {
        if (grpc_client != null) {
            return grpc_client;
        }
        grpc_client = new sscClient(grpc_address);
        return grpc_client;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server server = (Server) o;
        return name.equals(server.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public void shutdown() {
        System.out.println("Shutting down server ...");
        heartbeat_time = -1;
        if (grpc_client != null) {
            grpc_client.shutdown();
        }
    }
}
