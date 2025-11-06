package io.cockroachdb.customer.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.cockroachdb.betting.common.aspect.AdvisorOrder;
import io.cockroachdb.betting.common.aspect.ExponentialBackoffRetryHandler;
import io.cockroachdb.betting.common.aspect.OutboxAspect;
import io.cockroachdb.betting.common.aspect.RetryHandler;
import io.cockroachdb.betting.common.aspect.TransactionDecoratorAspect;
import io.cockroachdb.betting.common.aspect.TransactionRetryAspect;
import io.cockroachdb.betting.common.outbox.OutboxJdbcRepository;
import io.cockroachdb.betting.common.outbox.OutboxRepository;
import io.cockroachdb.customer.CustomerApplication;

@Configuration
@EnableTransactionManagement(order = AdvisorOrder.TRANSACTION_MANAGER_ADVISOR, proxyTargetClass = true)
@EnableJpaRepositories(basePackageClasses = CustomerApplication.class, enableDefaultTransactions = false)
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
