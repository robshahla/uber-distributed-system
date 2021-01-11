package entities;

public class Reservation {

    private final String first_name, last_name;
    private final String[] path;

    public Reservation(String first_name, String last_name, String[] path) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.path = path;
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
}
