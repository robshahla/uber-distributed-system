import management.ServerManager;
import management.logging.UberLogger;

import java.util.logging.Level;


public class Main {

    public static final UberLogger logger = UberLogger.getLogger(Main.class.getName());
    ;

    public static void main(String[] args) {

        ServerManager sm = ServerManager.getInstance();
        String filePath = args[0]; // config file path
        String server_name = args[1];
        logger.log(Level.INFO, "Initializing server: " + server_name);
        System.out.println("Version: 10");
        if (!sm.init(filePath, server_name)) {
            logger.log(Level.SEVERE, "Could not initialize server: " + server_name);
            return;
        }
        try {
            sm.start();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error starting server: " + server_name);
        }
    }

}
