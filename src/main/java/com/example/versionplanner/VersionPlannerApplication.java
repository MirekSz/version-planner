
package com.example.versionplanner;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@EnableScheduling
public class VersionPlannerApplication {

	private static Logger logger = LoggerFactory.getLogger(VersionPlannerApplication.class);

	public static void main(final String[] args) {
		System.setOut(new PrintStream(System.out) {

			@Override
			public void print(final String val) {
				logger.info(val);
				super.print(val);
			}
		});
		System.setErr(new PrintStream(System.err) {

			@Override
			public void print(final String val) {
				logger.error(val);
				super.print(val);
			}
		});

		SpringApplication.run(VersionPlannerApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {

			@Override
			public void addCorsMappings(final CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS")
						.allowCredentials(true);
			}
		};
	}
}
