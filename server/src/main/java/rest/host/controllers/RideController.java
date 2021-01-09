package rest.host.controllers;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RideController {

    @GetMapping("/employees")
    void all() {
        System.out.println("hello-rest");
    }

}
