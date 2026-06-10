
package co.empresa.vivaeventos.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VivaeventosEventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(VivaeventosEventsApplication.class, args);
    }
}
