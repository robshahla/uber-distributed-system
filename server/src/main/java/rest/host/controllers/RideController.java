package rest.host.controllers;

import entities.Reservation;
import entities.Ride;
import management.ServerManager;
import org.springframework.web.bind.annotation.*;


@RestController
public class RideController {

    @PostMapping("/rides")
    String publishRide(@RequestBody Ride ride) {
        return ServerManager.getInstance().saveRide(ride);
    }

    @PostMapping("/reservations")
    String reserveRide(@RequestBody Reservation reservation) {
        return ServerManager.getInstance().reserveRide(reservation);
    }

    @GetMapping("/snapshot")
    String getSnapshot() {
        return ServerManager.getInstance().getSnapshot();
    }
}
