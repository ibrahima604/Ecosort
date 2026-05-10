package fr.ecosort.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("fr.ecosort.backend.models")
@EnableJpaRepositories("fr.ecosort.backend.repositories")
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BackendApplication.class);
        app.addInitializers(new EnvLoader()); // ← charge .env avant tout
        app.run(args);
    }
}