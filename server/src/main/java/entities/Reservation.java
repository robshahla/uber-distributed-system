package entities;

public class Reservation {

    private String first_name, last_name;
    private String[] path;

    public Reservation(String first_name, String last_name, String[] path) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.path = path;
    }

    public String[] getPath() {
        return path;
    }
}
