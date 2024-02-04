package io.burpabet.wallet.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@EnableKafka
public class KafkaConfiguration {
    @Autowired
    private KafkaProperties properties;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        KafkaAdmin kafkaAdmin = new KafkaAdmin(this.properties.buildAdminProperties((null)));
        kafkaAdmin.setFatalIfBrokerNotAvailable(this.properties.getAdmin().isFailFast());
        return kafkaAdmin;
    }

    @Bean
    public AdminClient kafkaAdminClient() {
        return AdminClient.create(kafkaAdmin().getConfigurationProperties());
    }
}
