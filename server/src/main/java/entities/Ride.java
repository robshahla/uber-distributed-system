package entities;

import generated.ride;
import management.ServerManager;

import java.util.ArrayList;

import static java.lang.Math.*;

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

    public boolean isCityNeighbor(City city) {
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
        double m = Double.POSITIVE_INFINITY;
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
        double d1 = distanceBetween(city, getStartPosition());
        double d2 = distanceBetween(city, getEndPosition());
        double angel1 = asin(distance / d1);
        double angel2 = asin(distance / d2);
        return angel1 < PI / 2 && angel2 < PI / 2;
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
