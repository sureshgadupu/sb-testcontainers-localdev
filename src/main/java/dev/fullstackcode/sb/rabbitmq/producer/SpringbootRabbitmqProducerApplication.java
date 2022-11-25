package dev.fullstackcode.sb.rabbitmq.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringbootRabbitmqProducerApplication {

//	public static void main(String[] args) {
//		SpringApplication.run(SpringbootRabbitmqProducerApplication.class, args);
//	}

	public static void main(String[] args) {
		createSpringApplication().run(args);
	}

	public static SpringApplication createSpringApplication() {
		return new SpringApplication(SpringbootRabbitmqProducerApplication.class);
	}

}
