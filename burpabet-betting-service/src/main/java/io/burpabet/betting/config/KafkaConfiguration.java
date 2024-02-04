package io.burpabet.betting.config;

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

import io.burpabet.betting.service.BetPlacementService;
import io.burpabet.betting.service.BetSettlementService;
import io.burpabet.common.domain.BetPlacementEvent;
import io.burpabet.common.domain.BetSettlementEvent;
import io.burpabet.common.domain.TopicNames;

@Configuration
@EnableKafkaStreams
public class KafkaConfiguration {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BetPlacementService betPlacementService;

    @Autowired
    private BetSettlementService betSettlementService;

    @Bean
    public NewTopic walletPlacementTopic() {
        return TopicBuilder.name(TopicNames.WALLET_PLACEMENT)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
    public NewTopic bettingPlacementTopic() {
        return TopicBuilder.name(TopicNames.CUSTOMER_PLACEMENT)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
    public KStream<String, BetPlacementEvent> betPlacementStream(StreamsBuilder builder) {
        JsonSerde<BetPlacementEvent> placementSerde = new JsonSerde<>(BetPlacementEvent.class);

        KStream<String, BetPlacementEvent> walletStream = builder
                .stream(TopicNames.WALLET_PLACEMENT, Consumed.with(Serdes.String(), placementSerde));

        walletStream.join(builder.stream(TopicNames.CUSTOMER_PLACEMENT, Consumed.with(Serdes.String(), placementSerde)),
                        betPlacementService::confirmPlacement,
                        JoinWindows.ofTimeDifferenceAndGrace(Duration.ofMinutes(4), Duration.ofMinutes(2)),
                        StreamJoined.with(Serdes.String(), placementSerde, placementSerde))
                .peek((key, value) -> {
                    logger.debug("Stream join peek - key: {} payload: {}", key, value);
                })
                .filter((key, value) -> {
                    logger.debug("Stream join output predicate - key: {} payload: {}", key, value);
                    return Objects.nonNull(value);
                })
                .to(TopicNames.PLACEMENT);

        return walletStream;
    }

    @Bean
    public NewTopic walletSettlementTopic() {
        return TopicBuilder.name(TopicNames.WALLET_SETTLEMENT)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
    public NewTopic bettingSettlementTopic() {
        return TopicBuilder.name(TopicNames.CUSTOMER_SETTLEMENT)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
    public KStream<String, BetSettlementEvent> betSettelemtKStream(StreamsBuilder builder) {
        JsonSerde<BetSettlementEvent> settlementSerde = new JsonSerde<>(BetSettlementEvent.class);

        KStream<String, BetSettlementEvent> walletStream = builder
                .stream(TopicNames.WALLET_SETTLEMENT, Consumed.with(Serdes.String(), settlementSerde));

        walletStream.join(
                        builder.stream(TopicNames.CUSTOMER_SETTLEMENT, Consumed.with(Serdes.String(), settlementSerde)),
                        betSettlementService::confirmSettlement,
                        JoinWindows.ofTimeDifferenceAndGrace(Duration.ofMinutes(4), Duration.ofMinutes(2)),
                        StreamJoined.with(Serdes.String(), settlementSerde, settlementSerde))
                .peek((key, value) -> {
                    logger.debug("Stream join peek - key: {} payload: {}", key, value);
                })
                .filter((key, value) -> {
                    logger.debug("Stream join output predicate - key: {} payload: {}", key, value);
                    return Objects.nonNull(value);
                })
                .to(TopicNames.SETTLEMENT);

        return walletStream;
    }

}
