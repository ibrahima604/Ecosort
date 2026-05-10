package fr.ecosort.backend;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        Map<String, Object> props = new HashMap<>();
        // Cherche .env à la racine du projet
        java.io.File envFile = new java.io.File(".env");

        if (!envFile.exists()) {
            System.out.println("[EnvLoader] Pas de fichier .env trouvé à : "
                + envFile.getAbsolutePath());
            return;
        }

        System.out.println("[EnvLoader] Chargement de : " + envFile.getAbsolutePath());

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Ignore commentaires et lignes vides
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx > 0) {
                    String key   = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();
                    // Enlève les guillemets si présents
                    if (value.startsWith("\"") && value.endsWith("\""))
                        value = value.substring(1, value.length() - 1);
                    props.put(key, value);
                    System.out.println("[EnvLoader]  " + key + " chargé");
                }
            }
        } catch (Exception e) {
            System.err.println("[EnvLoader] Erreur lecture .env : " + e.getMessage());
        }

        context.getEnvironment().getPropertySources()
            .addFirst(new MapPropertySource("dotenv", props));
    }
}