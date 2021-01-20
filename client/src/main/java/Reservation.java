import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Reservation {

    private final String first_name, last_name, departure_time;
    private final String path;

    public Reservation(String first_name, String last_name, String departure_time, String path) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.path = path;
        this.departure_time = departure_time;
    }


    private static class Fields {
        private static final String FIRST_NAME = "first_name";
        private static final String LAST_NAME = "last_name";
        private static final String DEPARTURE_TIME = "departure_time";
        private static final String PATH = "path";

    }


    public String serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(Reservation.Fields.FIRST_NAME, new JsonPrimitive(first_name));
        jsonObject.add(Reservation.Fields.LAST_NAME, new JsonPrimitive(last_name));
        jsonObject.add(Reservation.Fields.DEPARTURE_TIME, new JsonPrimitive(departure_time));
        jsonObject.add(Fields.PATH, new JsonPrimitive(path));
        return jsonObject.toString();
    }

}
