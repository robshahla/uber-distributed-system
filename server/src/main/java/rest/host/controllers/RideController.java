package rest.host.controllers;

import entities.Reservation;
import entities.Ride;
import management.RestManager;
import management.ServerManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RideController {

    @PostMapping("/publishRide")
    String publishRide(@RequestBody Ride ride) {
        return RestManager.publishRide(ride);
    }

    @PostMapping("/reserveRide")
    String reserveRide(@RequestBody Reservation reservation) {
        return ""; // ServerManager.getInstance().reserveRide(reservation);
    }

    @GetMapping("/snapshot")
    String getSnapshot() {
        return ServerManager.getInstance().getSnapshot();
    }
}
