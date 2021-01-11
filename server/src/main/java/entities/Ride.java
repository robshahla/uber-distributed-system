package entities;

import generated.ride;

import java.util.ArrayList;

public class Ride {

    private int id;
    private final String first_name, last_name, phone, start_position, end_position, departure_time;
    int vacancies;
    double pd;
    ArrayList<String> reservations;

    public Ride(String first_name, String last_name, String phone, String start_position, String end_position, String departure_time, int vacancies, double pd) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone = phone;
        this.start_position = start_position;
        this.end_position = end_position;
        this.departure_time = departure_time;
        this.vacancies = vacancies;
        this.pd = pd;
    }

    public Ride(ride request) {
         this.first_name = request.getFirstName();
         this.last_name = request.getLastName();
         this.phone = request.getPhone();
         this.start_position = request.getStartPosition();
         this.end_position = request.getEndPosition();
         this.departure_time = request.getDepartureTime();
         this.vacancies = request.getVacancies();
         this.pd = request.getPd();
    }

    public void setId(int id) {
        this.id = id;
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

    public String getStartPosition() {
        return start_position;
    }

    public String getEndPosition() {
        return end_position;
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
    public String toString() {
        return "Ride{" +
                "id=" + id +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", phone='" + phone + '\'' +
                ", start_position='" + start_position + '\'' +
                ", end_position='" + end_position + '\'' +
                ", departure_time='" + departure_time + '\'' +
                ", vacancies=" + vacancies +
                ", pd=" + pd +
                ", reservations=" + String.join(",", reservations) +
                '}';
    }
}
