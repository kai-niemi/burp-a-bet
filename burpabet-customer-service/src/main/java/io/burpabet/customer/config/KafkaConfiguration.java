package io.burpabet.customer.config;

import java.time.Duration;
import java.util.Objects;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.scheduling.annotation.EnableAsync;

import io.burpabet.common.domain.RegistrationEvent;
import io.burpabet.common.domain.TopicNames;
import io.burpabet.customer.service.CustomerService;

@EnableKafkaStreams
@EnableAsync
@Configuration
public class KafkaConfiguration {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerService customerService;

    @Bean
    public NewTopic registrationTopic() {
        return TopicBuilder.name(TopicNames.REGISTRATION)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
    public NewTopic walletRegistrationTopic() {
        return TopicBuilder.name(TopicNames.WALLET_REGISTRATION)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
    public NewTopic bettingRegistrationTopic() {
        return TopicBuilder.name(TopicNames.BETTING_REGISTRATION)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
    public KStream<String, RegistrationEvent> registrationStream(StreamsBuilder builder) {
        JsonSerde<RegistrationEvent> registrationSerde = new JsonSerde<>(RegistrationEvent.class);

        KStream<String, RegistrationEvent> walletStream = builder
                .stream(TopicNames.WALLET_REGISTRATION, Consumed.with(Serdes.String(), registrationSerde));

        walletStream.join(
                        builder.stream(TopicNames.BETTING_REGISTRATION, Consumed.with(Serdes.String(), registrationSerde)),
                        customerService::confirmRegistration,
                        JoinWindows.ofTimeDifferenceAndGrace(Duration.ofMinutes(4), Duration.ofMinutes(2)),
                        StreamJoined.with(Serdes.String(), registrationSerde, registrationSerde))
                .peek((key, value) -> {
                    logger.debug("Stream join peek - key: {} payload: {}", key, value);
                })
                .filter((key, value) -> {
                    logger.debug("Stream join output predicate - key: {} payload: {}", key, value);
                    return Objects.nonNull(value);
                })
                .to(TopicNames.REGISTRATION);

        return walletStream;
    }
}
