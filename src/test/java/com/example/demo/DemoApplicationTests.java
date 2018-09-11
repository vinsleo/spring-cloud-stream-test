package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class DemoApplicationTests {
	@ClassRule
	public static EmbeddedKafkaRule embeddedKafkaRule = new EmbeddedKafkaRule(1, true, "counts-id");

	private static EmbeddedKafkaBroker embeddedKafka = embeddedKafkaRule.getEmbeddedKafka();

	private static Consumer<Integer, String> consumer;

	@BeforeClass
	public static void setUp() throws Exception {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("group-id", "false", embeddedKafka);
		//consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Deserializer.class.getName());
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		DefaultKafkaConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
		consumer = cf.createConsumer();
		embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "counts-id");
	}

	@AfterClass
	public static void tearDown() {
		consumer.close();
	}

	@Test
	public void testKstreamBinderWithPojoInputAndStringOuput() throws Exception {
		SpringApplication app = new SpringApplication(DemoApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);

		ConfigurableApplicationContext context = app.run(
				"--spring.cloud.stream.kafka.streams.binder.brokers=" + embeddedKafka.getBrokersAsString(),
				"--spring.cloud.stream.kafka.streams.binder.zkNodes=" + embeddedKafka.getZookeeperConnectionString());

		try {
			receiveAndValidateFoo(context);
		} finally {
			context.close();
		}
	}

	private void receiveAndValidateFoo(ConfigurableApplicationContext context) throws Exception {
		Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka);
		DefaultKafkaProducerFactory<Integer, String> pf = new DefaultKafkaProducerFactory<>(senderProps);
		KafkaTemplate<Integer, String> template = new KafkaTemplate<>(pf, true);
		template.setDefaultTopic("foos");
		template.sendDefault("{\"id\":\"123\"}");
		ConsumerRecord<Integer, String> cr = KafkaTestUtils.getSingleRecord(consumer, "counts-id");

		assertThat(cr.key()).isEqualTo(123);
		ObjectMapper om = new ObjectMapper();
		Long aLong = om.readValue(cr.value(), Long.class);
		assertThat(aLong).isEqualTo(1L);
	}


}
