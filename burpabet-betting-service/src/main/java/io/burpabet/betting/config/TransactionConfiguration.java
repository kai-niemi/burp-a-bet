package io.burpabet.betting.config;

import io.burpabet.betting.BettingApplication;
import io.burpabet.common.aspect.*;
import io.burpabet.common.outbox.OutboxJdbcRepository;
import io.burpabet.common.outbox.OutboxRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement(order = AdvisorOrder.TRANSACTION_MANAGER_ADVISOR, proxyTargetClass = true)
@EnableJpaRepositories(basePackageClasses = BettingApplication.class, enableDefaultTransactions = false)
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
    public RetryHandler retryHandler() {
        return new ExponentialBackoffRetryHandler();
    }

    @Bean
    public TransactionRetryAspect transactionRetryAspect(RetryHandler retryHandler) {
        return new TransactionRetryAspect(retryHandler);
    }

    @Bean
    public TransactionDecoratorAspect transactionDecoratorAspect(DataSource dataSource) {
        return new TransactionDecoratorAspect(new JdbcTemplate(dataSource));
    }
}
