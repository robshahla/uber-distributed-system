package entities;

import generated.reservation;

import java.util.Arrays;
import java.util.List;

public class Reservation {

    private final String first_name, last_name, departure_time;
    private final String path;

    public Reservation(String first_name, String last_name, String departure_time, String path) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.path = path;
        this.departure_time = departure_time;
    }

    public Reservation(reservation request) {
        this(request.getFirstName(),
                request.getLastName(),
                request.getDepartureTime(),
                String.join(",", request.getPathList()));
    }

    public reservation getReservationForGRPC() {
        return reservation.newBuilder()
                .setDepartureTime(departure_time)
                .setLastName(last_name)
                .setFirstName(first_name)
                .addAllPath(getPath()).build();
    }

    public List<String> getPath() {
        return Arrays.asList(path.trim().split("\\s*,\\s*").clone());
    }

    public String getFirstName() {
        return first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public String getDepartureTime() {
        return departure_time;
    }


}
