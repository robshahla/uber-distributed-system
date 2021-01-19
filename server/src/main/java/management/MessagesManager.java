package management;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import entities.Ride;
import entities.Server;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessagesManager {
    public static MessagesManager instance = new MessagesManager();
    public static final String ROOT_LOG_NAME = "ds-hw2";
    public static final Level LOG_LEVEL = Level.FINER;
    public static final Logger ROOT_LOGGER = Logger.getLogger(ROOT_LOG_NAME);
    public static final MessagesManager logger = MessagesManager.instance;
    private static final String OPERATION = "operation";
    private static final String DATA = "data";

    private static final String OPERATION_NEW_RIDE = "new-ride";
    private static final String OPERATION_UPDATE_RIDE = "update-ride";

    public static void ProcessMessage(byte[] message) {
        String data_string = new String(message, StandardCharsets.UTF_8);
        JsonObject json_message = JsonParser.parseString(data_string).getAsJsonObject();
        JsonElement operation = json_message.get(OPERATION);
        JsonElement data = json_message.get(DATA);
        logger.log(Level.FINE, "Processing message...");
        if (operation != null && data != null) {
            JsonObject ride_as_json = data.getAsJsonObject();
            Ride received_ride = Ride.deserialize(ride_as_json);
            logger.log(Level.FINE, "Operation " + operation.getAsString());
            logger.log(Level.FINE, "Ride " + received_ride.toString());
            if (operation.getAsString().equals(OPERATION_NEW_RIDE)) {

                ServerManager.getInstance().addRide(received_ride);
            } else if (operation.getAsString().equals(OPERATION_UPDATE_RIDE)) {
                ServerManager.getInstance().updateRide(received_ride);
            }
        }
    }

    public static class MessageFactory {

        public static String newRideBroadCastMessage(Ride new_ride) {
            JsonObject json_message = new JsonObject();
            json_message.add(OPERATION, new JsonPrimitive(OPERATION_NEW_RIDE));
            json_message.add(DATA, JsonParser.parseString(new_ride.serialize()));
            return json_message.toString();
        }

        public static String updateRideBroadCastMessage(Ride updated_ride) {
            JsonObject json_message = new JsonObject();
            json_message.add(OPERATION, new JsonPrimitive(OPERATION_UPDATE_RIDE));
            json_message.add(DATA, JsonParser.parseString(updated_ride.serialize()));
            return json_message.toString();
        }

        public static void appendMessageToServers(Set<Server> servers, String message, Map<Server, List<String>> message_map) {

            servers.forEach(server -> {
                message_map.computeIfAbsent(server, k -> new ArrayList<>());
                message_map.get(server).add(message);
            });
        }
    }

    public void log(Level level, String str) {
        System.out.println(str);
    }

}
