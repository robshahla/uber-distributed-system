package rest.host.controllers;

import entities.Ride;
import management.ServerManager;
import org.springframework.web.bind.annotation.*;


@RestController
public class RideController {

    @PostMapping("/rides")
    Ride publishRide(@RequestBody Ride ride) {
        return ServerManager.getInstance().saveRide(ride);
    }

}
