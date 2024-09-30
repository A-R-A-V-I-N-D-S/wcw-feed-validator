package com.manualtasks.wcwfeedvalidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.manualtasks.wcwfeedvalidator.controller.ApplicationController;

@SpringBootApplication
public class WcwFeedValidatorApplication {

	@Autowired
	private ApplicationController applicationController;

	public static void main(String[] args) {
		SpringApplication.run(WcwFeedValidatorApplication.class, args).close();
	}

	@Bean
	public CommandLineRunner commandLineRunner() {
		return runner -> {
			applicationController.executeTheTask();
		};
	}

}
