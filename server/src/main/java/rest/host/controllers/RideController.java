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
        System.out.println("\n\n" + DASH + " REST REQUEST START (PUBLISH RIDE)" + DASH);
        String result = RestManager.publishRide(ride);
        System.out.println(DASH + " REST REQUEST END (PUBLISH RIDE)" + DASH);
        return result;
    }

    @PostMapping("/reserveRide")
    String reserveRide(@RequestBody Reservation reservation) {
        System.out.println("\n\n" + DASH + " REST REQUEST START (RESERVE RIDE)" + DASH);
        String result = null;
        try {
            result = RestManager.reserveRide(reservation);
        } catch (InterruptedException e) {
            result = "Error while handling your request";
            e.printStackTrace();
        }
        System.out.println(DASH + " REST REQUEST END (RESERVE RIDE)" + DASH);
        return result;
    }

    @GetMapping("/snapshot")
    String getSnapshot() {
        System.out.println("\n\n" + DASH + " REST REQUEST START (GET SNAPSHOT)" + DASH);
        String result = RestManager.getSnapshot();
        System.out.println(DASH + " REST REQUEST END (GET SNAPSHOT)" + DASH);
        return result;
    }
}
