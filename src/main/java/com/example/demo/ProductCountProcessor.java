package com.example.demo;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.messaging.handler.annotation.SendTo;

@EnableBinding(KafkaStreamsProcessor.class)
public class ProductCountProcessor {

    @StreamListener("input")
    @SendTo("output")
    public KStream<Integer, Long> process(KStream<Object, Product> input) {
        return input
                .filter((key, product) -> product.getId() == 123)
                .map((key, value) -> new KeyValue<>(value, value))
                .groupByKey(Serialized.with(new JsonSerde<>(Product.class), new JsonSerde<>(Product.class)))
                .windowedBy(TimeWindows.of(5000))
                .count(Materialized.as("id-count-store-x"))
                .toStream()
                .map((key, value) -> new KeyValue<>(key.key().id, value));
    }
}
