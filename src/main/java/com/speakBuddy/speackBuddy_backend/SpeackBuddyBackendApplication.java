package com.speakBuddy.speackBuddy_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpeackBuddyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpeackBuddyBackendApplication.class, args);
	}

}
