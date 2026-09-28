package com.RentEaseLanka.RentEaseLankaTeam;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RentEaseLankaTeamApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().directory("RentEaseLankaTeam/.env")
				.ignoreIfMissing() // Prevents app crash if run in production where .env is absent
				.load();

		// Set each variable as a system property so Spring environment can resolve them
		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);
		SpringApplication.run(RentEaseLankaTeamApplication.class, args);
	}

}
