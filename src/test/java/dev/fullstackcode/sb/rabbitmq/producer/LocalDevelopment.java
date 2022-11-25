package dev.fullstackcode.sb.rabbitmq.producer;

import org.springframework.boot.SpringApplication;

public class LocalDevelopment {
	
	public static void main(String[] args) {
		
		 SpringApplication application = SpringbootRabbitmqProducerApplication.createSpringApplication();
			
		 application.addInitializers(new AbstractIntegrationTest.Initializer());

	     application.run(args);
	}
	

}
