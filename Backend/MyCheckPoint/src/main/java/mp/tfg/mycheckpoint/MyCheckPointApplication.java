package mp.tfg.mycheckpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyCheckPointApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyCheckPointApplication.class, args);
    }

}
