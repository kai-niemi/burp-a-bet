package io.burpabet.wallet.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.burpabet.common.aspect.AdvisorOrder;
import io.burpabet.common.aspect.ExponentialBackoffRetryHandler;
import io.burpabet.common.aspect.OutboxAspect;
import io.burpabet.common.aspect.RetryHandler;
import io.burpabet.common.aspect.TransactionDecoratorAspect;
import io.burpabet.common.aspect.TransactionRetryAspect;
import io.burpabet.common.outbox.OutboxJdbcRepository;
import io.burpabet.common.outbox.OutboxRepository;
import io.burpabet.wallet.WalletApplication;

@Configuration
@EnableTransactionManagement(order = AdvisorOrder.TRANSACTION_MANAGER_ADVISOR, proxyTargetClass = true)
@EnableJpaRepositories(basePackageClasses = WalletApplication.class,enableDefaultTransactions = false)
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
