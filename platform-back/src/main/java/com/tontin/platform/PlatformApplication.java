package com.tontin.platform;

import com.tontin.platform.config.DotEnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlatformApplication {

	public static void main(String[] args) {
		DotEnvLoader.loadIfPresent();
		SpringApplication.run(PlatformApplication.class, args);
	}
}
