package rest.host.controllers;

import entities.Reservation;
import entities.Ride;
import management.RestManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RideController {

    private static String DASH = "-------------";

    @PostMapping("/publishRide")
    String publishRide(@RequestBody Ride ride) {
        System.out.println("\n\n" + DASH + " RESET REQUEST START (PUBLISH RIDE)" + DASH);
        String result = RestManager.publishRide(ride);
        System.out.println(DASH + " RESET REQUEST END (PUBLISH RIDE)" + DASH);
        return result;
    }

    @PostMapping("/reserveRide")
    String reserveRide(@RequestBody Reservation reservation) {
        System.out.println("\n\n" + DASH + " RESET REQUEST START (RESERVE RIDE)" + DASH);
        String result = RestManager.reserveRide(reservation);
        System.out.println(DASH + " RESET REQUEST END (RESERVE RIDE)" + DASH);
        return result;
    }

    @GetMapping("/snapshot")
    String getSnapshot() {
        System.out.println("\n\n" + DASH + " RESET REQUEST START (GET SNAPSHOT)" + DASH);
        String result = RestManager.getSnapshot();
        System.out.println(DASH + " RESET REQUEST END (GET SNAPSHOT)" + DASH);
        return result;
    }
}
