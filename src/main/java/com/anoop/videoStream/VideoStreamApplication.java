package com.anoop.videoStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VideoStreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoStreamApplication.class, args);
	}

}
