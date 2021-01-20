import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Ride {

    private final String first_name, last_name, phone, start_position, end_position, departure_time;
    int vacancies;
    double pd;


    public Ride(String first_name, String last_name, String phone, String start_position,
                String end_position, String departure_time, int vacancies, double pd) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone = phone;
        this.start_position = start_position;
        this.end_position = end_position;
        this.departure_time = departure_time;
        this.vacancies = vacancies;
        this.pd = pd;
    }

    public String getFirstName() {
        return first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public String getPhone() {
        return phone;
    }

    public String getDepartureTime() {
        return departure_time;
    }

    public int getVacancies() {
        return vacancies;
    }

    public double getPd() {
        return pd;
    }

    @Override
    public String toString() {
        StringBuilder ride_to_str = new StringBuilder();
        final String new_line = "\n\t";
        ride_to_str.append("Ride")
                .append(new_line).append("First Name: ").append(first_name)
                .append(new_line).append("Last Name: ").append(last_name)
                .append(new_line).append("Phone: ").append(phone)
                .append(new_line).append("Ride From ").append(start_position).append(" to ").append(end_position)
                .append(new_line).append("Departure time: ").append(departure_time)
                .append(new_line).append("Permitted Deviation: ").append(pd)
                .append("\n");
        return ride_to_str.toString();
    }


    private static class Fields {
        private static final String FIRST_NAME = "first_name";
        private static final String LAST_NAME = "last_name";
        private static final String PHONE = "phone";
        private static final String START_POSITION = "start_position";
        private static final String END_POSITION = "end_position";
        private static final String DEPARTURE_TIME = "departure_time";
        private static final String VACANCIES = "vacancies";
        private static final String PERMITTED_DEVIATION = "pd";
        private static final String RESERVATIONS = "reservations";

    }

    public String serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(Fields.FIRST_NAME, new JsonPrimitive(first_name));
        jsonObject.add(Fields.LAST_NAME, new JsonPrimitive(last_name));
        jsonObject.add(Fields.PHONE, new JsonPrimitive(phone));
        jsonObject.add(Fields.START_POSITION, new JsonPrimitive(start_position));
        jsonObject.add(Fields.END_POSITION, new JsonPrimitive(end_position));
        jsonObject.add(Fields.DEPARTURE_TIME, new JsonPrimitive(departure_time));
        jsonObject.add(Fields.VACANCIES, new JsonPrimitive(vacancies));
        jsonObject.add(Fields.PERMITTED_DEVIATION, new JsonPrimitive(pd));
        return jsonObject.toString();
    }

    public static Ride deserialize(JsonObject json_object) {
        String firstName = json_object.get(Fields.FIRST_NAME).getAsString();
        String lastName = json_object.get(Fields.LAST_NAME).getAsString();
        String phone = json_object.get(Fields.PHONE).getAsString();
        String startPosition = json_object.get(Fields.START_POSITION).getAsString();
        String endPosition = json_object.get(Fields.END_POSITION).getAsString();
        String departureTime = json_object.get(Fields.DEPARTURE_TIME).getAsString();
        int vacancies = json_object.get(Fields.VACANCIES).getAsInt();
        double pd = json_object.get(Fields.PERMITTED_DEVIATION).getAsDouble();
        return new Ride(firstName, lastName, phone, startPosition, endPosition, departureTime, vacancies, pd);
    }

}
