package com.paytm.bookshow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication()
@EnableJpaAuditing
public class BookshowApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookshowApplication.class, args);
	}

}
