package dev.fullstackcode.sb.rabbitmq.producer;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

import com.sun.tools.javac.Main;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;

//import com.github.terma.javaniotcpproxy.StaticTcpProxyConfig;
//import com.github.terma.javaniotcpproxy.TcpProxy;
//import com.github.terma.javaniotcpproxy.TcpProxyConfig;

import lombok.extern.slf4j.Slf4j;

import net.kanstren.tcptunnel.Params;
import net.kanstren.tcptunnel.observers.InMemoryLogger;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
public abstract class AbstractIntegrationTest {

	public static  class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		
		static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.10.6-management-alpine")
                .withStartupTimeout(Duration.of(100, SECONDS))
                .withEnv("JAVA_OPTS", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:8005");

		
		 public static  Map<String, String> getProperties() {
			 Startables.deepStart(Stream.of(rabbitMQContainer)).join();
						 
			 return Map.of(
					  "spring.rabbitmq.host",rabbitMQContainer.getHost(),
				      "spring.rabbitmq.port", rabbitMQContainer.getAmqpPort().toString()

//                     "spring.datasource.url", postgres.getJdbcUrl(),
//		               "spring.datasource.username", postgres.getUsername(),
//		               "spring.datasource.password",postgres.getPassword(),
					 );

		 }
		

		public void initialize(ConfigurableApplicationContext context) {
			 ConfigurableEnvironment env = context.getEnvironment();
			 
			env.getPropertySources().addFirst(new MapPropertySource("testcontainers", (Map) getProperties()));

//			TcpProxyConfig  config = new StaticTcpProxyConfig(
//			        5005,
//			        rabbitMQContainer.getHost(),
//			        rabbitMQContainer.getMappedPort(15672)
//			);
//			config.setWorkerCount(1);
//			TcpProxy tcpProxy = new TcpProxy(config);
//			tcpProxy.start();
			

			   Params params = new Params(5006, rabbitMQContainer.getHost(), rabbitMQContainer.getMappedPort(15672));
			    //we want to use the captured data in testing, so enable logging the tunnel data in memory with buffer size 8092 bytes
			    params.enableInMemoryLogging(8092);
			    //this gives us access to the data passed from client connected to port 5598 -> localhost:5599 (client to server)
			    InMemoryLogger upLogger = params.getUpMemoryLogger();
			    //this gives us access to the data passed from localhost:5599 -> client connected to port 5598 (server to client)
			    InMemoryLogger downLogger = params.getDownMemoryLogger();
			    //this is how we actually start the tunnel
			    net.kanstren.tcptunnel.Main main = new net.kanstren.tcptunnel.Main(params);
			    main.start();
	         
		} 

	}

	

}
