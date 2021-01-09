import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class sscServer {
    private static final Logger logger = Logger.getLogger(sscServer.class.getName());

    private final int port;
    private final Server server;


    public sscServer(int port) throws IOException {
        this(ServerBuilder.forPort(port), port);
    }

    public sscServer(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        server = serverBuilder.addService(new sscService(port))
                .build();

    }

    public void start() throws IOException {
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    sscServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    /**
     * Stop serving requests and shutdown resources.
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}