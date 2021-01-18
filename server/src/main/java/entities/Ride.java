package entities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import generated.ride;
import management.ServerManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Math.*;

public class Ride implements Serializable {

    private int id;
    private String first_name, last_name, phone, start_position, end_position, departure_time;
    int vacancies;
    double pd;
    ArrayList<String> reservations;

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
        this.id = -1;
        this.reservations = new ArrayList<>();
    }

    public Ride(ride request) {
        this(request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getStartPosition(),
                request.getEndPosition(),
                request.getDepartureTime(),
                request.getVacancies(),
                request.getPd());
    }

    private Ride() {
        id = Integer.MIN_VALUE;
    }

    public boolean isNull() {
        return id < -1; //TODO maybe < 0?
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
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

    public City getStartPosition() {
        return ServerManager.getInstance().getCity(start_position);
    }

    public City getEndPosition() {
        return ServerManager.getInstance().getCity(end_position);
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

    public void reserve(String name) {
        if (reservations.size() < vacancies) {
            reservations.add(name);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ride ride = (Ride) o;
        return vacancies == ride.vacancies && Double.compare(ride.pd, pd) == 0 && first_name.equals(ride.first_name) && last_name.equals(ride.last_name) && phone.equals(ride.phone) && start_position.equals(ride.start_position) && end_position.equals(ride.end_position) && departure_time.equals(ride.departure_time) && reservations.equals(ride.reservations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first_name, last_name, phone, start_position, end_position, departure_time, vacancies, pd, reservations);
    }

    /**
     * check if the person suggesting the ride will accept going
     * to the given city to pick someone up if there is available space
     */
    public boolean canPickUpFrom(City city) {
        if (reservations.size() >= vacancies) return false;
        double distance = distanceToLine(city);
        if (distance > pd) return false;
        return isCityInBorder(city, distance);
    }


    public double distanceToLine(City other) {
        City start_city = getStartPosition();
        City end_city = getEndPosition();
        assert start_city != null && end_city != null;
        double dx = end_city.getX() - start_city.getX();
        double dy = end_city.getY() - start_city.getY();
        double m;
        double distance = abs(start_city.getX() - other.getX());
        if (dx != 0) {
            m = dy / dx;
            double c = -(start_city.getY() - m * start_city.getX());
            double a = -m;
            double b = 1;
            distance = abs(a * other.getX() + b * other.getY() + c) / sqrt(pow(a, 2) + pow(b, 2));
        }
        return distance;
    }

    private double distanceBetween(City lhs, City rhs) {
        double dx = abs(rhs.getX() - lhs.getX());
        double dy = abs(rhs.getY() - lhs.getY());
        return sqrt(pow(dx, 2) + pow(dy, 2));
    }

    private boolean isCityInBorder(City city, double distance) {
        if (city.getName().equals(getStartPosition().getName())) return true;
        if (city.getName().equals(getEndPosition().getName())) return false;
        double d1 = distanceBetween(city, getStartPosition());
        double d2 = distanceBetween(city, getEndPosition());
        double angel1 = asin(distance / d1);
        double angel2 = asin(distance / d2);
        return angel1 < PI / 2 && angel2 < PI / 2;
    }

    @Override
    public String toString() {
        StringBuilder ride_to_str = new StringBuilder();
        final String new_line = "\n\t";
        ride_to_str.append("Ride (id:").append(id).append("):")
                .append(new_line).append("First Name: ").append(first_name)
                .append(new_line).append("Last Name: ").append(last_name)
                .append(new_line).append("Phone: ").append(phone)
                .append(new_line).append("Ride From ").append(start_position).append(" to ").append(end_position)
                .append(new_line).append("Departure time: ").append(departure_time)
                .append(new_line).append("Available places: ").append(vacancies - reservations.size())
                .append(new_line).append("Permitted Deviation: ").append(pd)
                .append(new_line).append("Reserved by (").append(reservations.size()).append(" total):");
        for (String reservant : reservations) {
            ride_to_str.append(new_line).append("\t").append(reservant);
        }
        ride_to_str.append("\n");

        return ride_to_str.toString();
    }


    private static class Fields {
        private static final String ID = "id";
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
        jsonObject.add(Fields.ID, new JsonPrimitive(id));
        jsonObject.add(Fields.FIRST_NAME, new JsonPrimitive(first_name));
        jsonObject.add(Fields.LAST_NAME, new JsonPrimitive(last_name));
        jsonObject.add(Fields.PHONE, new JsonPrimitive(phone));
        jsonObject.add(Fields.START_POSITION, new JsonPrimitive(start_position));
        jsonObject.add(Fields.END_POSITION, new JsonPrimitive(end_position));
        jsonObject.add(Fields.DEPARTURE_TIME, new JsonPrimitive(departure_time));
        jsonObject.add(Fields.VACANCIES, new JsonPrimitive(vacancies));
        jsonObject.add(Fields.PERMITTED_DEVIATION, new JsonPrimitive(pd));
        JsonArray reservations_array = new JsonArray();
        reservations.forEach(reservations_array::add);
        jsonObject.add(Fields.RESERVATIONS, reservations_array);
        return jsonObject.toString();
    }

    public static Ride deserialize(JsonObject json_object) {
        int id = json_object.get(Fields.ID).getAsInt();
        String firstName = json_object.get(Fields.FIRST_NAME).getAsString();
        String lastName = json_object.get(Fields.LAST_NAME).getAsString();
        String phone = json_object.get(Fields.PHONE).getAsString();
        String startPosition = json_object.get(Fields.START_POSITION).getAsString();
        String endPosition = json_object.get(Fields.END_POSITION).getAsString();
        String departureTime = json_object.get(Fields.DEPARTURE_TIME).getAsString();
        int vacancies = json_object.get(Fields.VACANCIES).getAsInt();
        double pd = json_object.get(Fields.PERMITTED_DEVIATION).getAsDouble();
        Ride ride = new Ride(firstName, lastName, phone, startPosition, endPosition, departureTime, vacancies, pd);
        JsonArray reservations = json_object.get(Fields.RESERVATIONS).getAsJsonArray();
        reservations.forEach(reserve -> ride.reserve(reserve.getAsString()));
        ride.setId(id);
        return ride;
    }

    public ride getRideRequestForGRPC() {
        return ride.newBuilder()
                .setFirstName(getFirstName())
                .setLastName(getLastName())
                .setPhone(getPhone())
                .setStartPosition(getStartPosition().getName())
                .setEndPosition(getEndPosition().getName())
                .setDepartureTime(getDepartureTime())
                .setVacancies(getVacancies())
                .setPd(getPd())
                .setId(getId())
                .build();
    }

    public static Ride nullRide() {
        return new Ride();
    }
}
