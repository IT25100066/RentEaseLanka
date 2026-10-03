package com.RentEaseLanka.RentEaseLankaTeam;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class RentEaseLankaTeamApplication {

	public static void main(String[] args) {
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
			dotenv.entries().forEach(entry -> {
				if (System.getProperty(entry.getKey()) == null) {
					System.setProperty(entry.getKey(), entry.getValue());
				}
			});
		}

		SpringApplication.run(RentEaseLankaTeamApplication.class, args);
	}

}
