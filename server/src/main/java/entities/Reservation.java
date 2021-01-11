package entities;

public class Reservation {

    private final String first_name, last_name, date;
    private final String[] path; // TODO: This should be a comma seperated string, in order to pass it in protobuf, once we change it we must change also the reservation in protobuf

    public Reservation(String first_name, String last_name, String date, String[] path) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.path = path;
        this.date = date;
    }

    public String[] getPath() {
        return path;
    }

    public String getFirstName() {
        return first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public String getDate() {
        return date;
    }
}
