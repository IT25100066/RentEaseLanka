package com.RentEaseLanka.RentEaseLankaTeam.User_Management.Config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String[] potentialPaths = {
                "RentEaseLankaTeam/src/main",
                "src/main",
                "RentEaseLankaTeam/src/main/resources",
                "src/main/resources",
                "RentEaseLankaTeam",
                "."
        };

        Dotenv dotenv = null;
        for (String path : potentialPaths) {
            File envFile = new File(path, ".env");
            if (envFile.exists()) {
                dotenv = Dotenv.configure().directory(path).ignoreIfMissing().load();
                break;
            }
        }

        if (dotenv == null) {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        }

        if (dotenv != null) {
            Map<String, Object> map = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                map.put(entry.getKey(), entry.getValue());
                if (System.getProperty(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", map));
        }
    }
}
