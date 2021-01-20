package utilities;

import entities.Ride;
import management.MessagesManager;
import management.ServerManager;
import management.logging.UberLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PathSolver {

    private static UberLogger logger = UberLogger.getLogger(PathSolver.class.getName());

    static class Variable {
        String start_city, end_city;
        ArrayList<Ride> domain;
        Ride value;
        boolean valid_value;

        public Variable(String start_city, String end_city) {
            this.start_city = start_city;
            this.end_city = end_city;
            this.domain = new ArrayList<>();
            this.value = Ride.nullRide();
            this.valid_value = false;
        }

        public ArrayList<Ride> getDomain() {
            return domain;
        }

        public void setDomain(ArrayList<Ride> domain) {
            this.domain = domain;
        }

        public String getStartCity() {
            return start_city;
        }

        public void setStartCity(String start_city) {
            this.start_city = start_city;
        }

        public String getEndCity() {
            return end_city;
        }

        public void setEndCity(String end_city) {
            this.end_city = end_city;
        }

        public Ride getValue() {
            return value;
        }

        public void setValue(Ride value) {
            this.value = value;
        }

        public boolean isValidValue() {
            return valid_value;
        }

        public void setValidValue(boolean valid_value) {
            this.valid_value = valid_value;
        }
    }

    ArrayList<Variable> assignment;

    Map<Integer, Boolean> already_seen_bitmap;

    public PathSolver(List<Ride> rides, List<String> path) {
        assert path.size() >= 2;

        // init already_seen_bitmap
        already_seen_bitmap = new HashMap<>();
        assignment = new ArrayList<>();

        rides.forEach(ride -> {
            already_seen_bitmap.put(ride.getId(), false);
        });
        for (int i = 0; i < path.size() - 1; i++) {
            Variable var = new Variable(path.get(i),path.get(i+1));
            assignment.add(var);
        }

        ServerManager sm = ServerManager.getInstance();
        assignment.forEach(var -> {
            var.getDomain().addAll(rides.stream()
                    .filter(ride -> ride.canServe(sm.getCity(var.getStartCity()), sm.getCity(var.getEndCity())))
                    .collect(Collectors.toList()));
        });
    }

    public List<Ride> solve() {
        logger.log(Level.FINE, "Searching for path:");
        assignment.forEach(var -> {
            logger.log(Level.FINE, var.start_city + "->" + var.end_city);
        });
        boolean solved = solve_aux(0);

        // no suitable set of rides was found was found
        if (!solved) {
            logger.log(Level.FINE, "No solution found!");
            return null;
        }
        logger.log(Level.FINE, "Found solution:");
        // a set of rides was found, return them.
        List<Ride> rides_to_reserve = new ArrayList<>();
        assignment.forEach(var -> {
            logger.log(Level.FINE, "------------------------------------------");
            logger.log(Level.FINE, var.start_city + "->" + var.end_city);
            logger.log(Level.FINE, "On Ride:\n " + var.value.toString());
            rides_to_reserve.add(var.getValue());
        });

        return rides_to_reserve;
    }

    private boolean solve_aux(int solver_index) {
        if (solver_index == assignment.size()) {
            return true;
        }

        for (Ride ride : assignment.get(solver_index).getDomain()) {
            if (already_seen_bitmap.get(ride.getId())) continue; //someone before me already used this ride.

            assignment.get(solver_index).setValue(ride);
            already_seen_bitmap.put(ride.getId(), true);
            boolean solved = solve_aux(solver_index + 1);
            if (solved) return true;

            // no solution was found, try another one
            already_seen_bitmap.put(ride.getId(), false);

        }
        return false; // no solution was found
    }
}
