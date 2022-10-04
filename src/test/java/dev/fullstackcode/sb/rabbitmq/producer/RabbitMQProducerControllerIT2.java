package dev.fullstackcode.sb.rabbitmq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.fullstackcode.sb.rabbitmq.producer.controller.RabbitMQProducerController;
import dev.fullstackcode.sb.rabbitmq.producer.model.Event;
import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@Slf4j
public class RabbitMQProducerControllerIT2 {


    static GenericContainer  rabbitMQContainer ;

    // Port waiting strategy

    static {
        WaitStrategy waitStrategy = new HostPortWaitStrategy().withStartupTimeout(Duration.of(100, SECONDS));
        rabbitMQContainer = new GenericContainer("docker.io/rabbitmq:3.10.6-management-alpine")
                .withExposedPorts(5672,15672);
        rabbitMQContainer.setWaitStrategy(waitStrategy);
        rabbitMQContainer.start();
     }

     // Log waiting strategy

//    static {
//        WaitStrategy waitStrategy = Wait.forLogMessage(".*Server startup complete.*", 1).withStartupTimeout(Duration.ofSeconds(60L));
//        rabbitMQContainer = new GenericContainer("docker.io/rabbitmq:3.10.6-management-alpine")
//                .withExposedPorts(5672,15672);
//        rabbitMQContainer.setWaitStrategy(waitStrategy);
//        rabbitMQContainer.start();
//    }


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitMQProducerController rabbitMQProducerController;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testSendAndReceiveMessage() throws Exception {

        Event event = new Event();
        event.setId(1);
        event.setName("Event A");
        rabbitMQProducerController.send(event);

        Message message =  rabbitTemplate.receive("queue.A",10);
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        Event eventReceived  = (Event) converter.fromMessage(message);

        assertEquals(eventReceived.getId(),event.getId());
        assertEquals(eventReceived.getName(),event.getName());

    }

    // https://rawcdn.githack.com/rabbitmq/rabbitmq-server/v3.10.7/deps/rabbitmq_management/priv/www/api/index.html
    @Test
    public void testExchangeCreation()  {

        ResponseEntity<Object> exchanges  = testRestTemplate.withBasicAuth("guest","guest").getForEntity("http://"+ rabbitMQContainer.getHost()+":"+rabbitMQContainer.getMappedPort(5672)+"/api/exchanges", Object.class);
        log.info("exchanges {}",exchanges);
        assertEquals(200,exchanges.getStatusCode().value());
        assertTrue(exchanges.getBody().toString().contains("name=exchange.direct, type=direct"));

    }

    @Test
    public void testQueueCreation() throws JsonProcessingException {

        ResponseEntity<Object> queues  = testRestTemplate.withBasicAuth("guest","guest").getForEntity("http://"+ rabbitMQContainer.getHost()+":"+rabbitMQContainer.getMappedPort(5672)+"/api/queues", Object.class);
        log.info("queues {}",queues.getBody().toString());
        assertEquals(200,queues.getStatusCode().value());
        assertTrue(queues.getBody().toString().contains("name=queue.A"));
        assertTrue(queues.getBody().toString().contains("name=queue.B"));
    }




    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {

        registry.add("spring.rabbitmq.host",() -> rabbitMQContainer.getHost());
        registry.add("spring.rabbitmq.port",() -> rabbitMQContainer.getMappedPort(5672));

    }

}
