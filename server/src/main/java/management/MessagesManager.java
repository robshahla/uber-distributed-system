package management;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import entities.Ride;

import java.nio.charset.StandardCharsets;

public class MessagesManager {
    private static final String OPERATION = "operation";
    private static final String DATA = "data";

    private static final String OPERATION_NEW_RIDE = "new-ride";

    public static void ProcessMessage(byte[] message) {
        String data_string = new String(message, StandardCharsets.UTF_8);
        JsonObject json_message = JsonParser.parseString(data_string).getAsJsonObject();
        JsonElement operation = json_message.get(OPERATION);
        if (operation != null) {
            if (operation.getAsString().equals(OPERATION_NEW_RIDE)) {
                JsonObject ride_as_json = json_message.get(DATA).getAsJsonObject();
                Ride ride_to_add = Ride.deserialize(ride_as_json);
                ServerManager.getInstance().addRide(ride_to_add);
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
    }
}
