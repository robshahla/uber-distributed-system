import management.MessagesManager;
import management.ServerManager;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    static {
        logger.setParent(MessagesManager.ROOT_LOGGER);
        logger.setLevel(MessagesManager.LOG_LEVEL);
        LogManager.getLogManager().getLogger(MessagesManager.ROOT_LOG_NAME).setLevel(MessagesManager.LOG_LEVEL);
        Arrays.stream(Logger.getLogger("").getHandlers()).forEach(
                logger -> {

                    logger.setLevel(MessagesManager.LOG_LEVEL);
                }
        );
    }

    public static void main(String[] args) {

        ServerManager sm = ServerManager.getInstance();
        String filePath = args[0]; // config file path
        String server_name = args[1];
        logger.log(Level.INFO, "Initializing server: " + server_name);
        if (!sm.init(filePath, server_name)) {
            logger.log(Level.SEVERE, "Could not initialize server: " + server_name);
            return;
        }
        sm.start();
        logger.log(Level.INFO, "happened after start");
    }

}
