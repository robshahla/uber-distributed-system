import management.ServerManager;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        Level level = Level.CONFIG;
        Logger.getLogger("").setLevel(level);
        Arrays.stream(Logger.getLogger("").getHandlers()).forEach(
                logger -> logger.setLevel(level)
        );

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
