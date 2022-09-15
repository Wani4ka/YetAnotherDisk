package me.wani4ka.yadisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class YadiskApplication {

	public static void main(String[] args) {
		SpringApplication.run(YadiskApplication.class, args);
	}

}
