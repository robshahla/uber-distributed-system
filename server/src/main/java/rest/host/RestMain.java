package rest.host;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.Collections;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class RestMain {
    public static void run(String[] args) {
        SpringApplication app = new SpringApplication(RestMain.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", args[0]));
        app.run(args);
    }
}
