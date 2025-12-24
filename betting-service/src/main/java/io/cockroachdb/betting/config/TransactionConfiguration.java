package io.cockroachdb.betting.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.cockroachdb.betting.BettingApplication;
import io.cockroachdb.betting.common.aspect.AdvisorOrder;
import io.cockroachdb.betting.common.aspect.OutboxAspect;
import io.cockroachdb.betting.common.aspect.TransactionAttributesAspect;
import io.cockroachdb.betting.common.outbox.OutboxJdbcRepository;
import io.cockroachdb.betting.common.outbox.OutboxRepository;

@Configuration
@EnableTransactionManagement(order = AdvisorOrder.TRANSACTION_BOUNDARY_ADVISOR, proxyTargetClass = true)
@EnableJpaRepositories(basePackageClasses = BettingApplication.class, enableDefaultTransactions = false)
@EnableResilientMethods(proxyTargetClass = true, order = AdvisorOrder.TRANSACTION_BEFORE_ADVISOR)
@EnableJpaAuditing
public class TransactionConfiguration {
    @Bean
    public OutboxRepository outboxRepository() {
        return new OutboxJdbcRepository();
    }

    @Bean
    public OutboxAspect outboxAspect() {
        return new OutboxAspect(outboxRepository());
    }

    @Bean
    public TransactionAttributesAspect transactionAttributesAspect(DataSource dataSource) {
        return new TransactionAttributesAspect(dataSource);
    }
}
