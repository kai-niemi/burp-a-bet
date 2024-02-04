package io.burpabet.betting.config;

import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class DataSourceConfiguration {
    public static final String SQL_TRACE_LOGGER = "io.burpabet.SQL_TRACE";

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @Lazy
    public DataSource primaryDataSource() {
        LazyConnectionDataSourceProxy proxy = new LazyConnectionDataSourceProxy();
        proxy.setTargetDataSource(loggingProxy(targetDataSource()));
        proxy.setDefaultAutoCommit(true);
        proxy.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        return proxy;
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource targetDataSource() {
        HikariDataSource ds = dataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        ds.addDataSourceProperty("reWriteBatchedInserts", "true");
        ds.addDataSourceProperty("ApplicationName", applicationContext.getApplicationName());
        return ds;
    }

    private DataSource loggingProxy(DataSource dataSource) {
        return ProxyDataSourceBuilder
                .create(dataSource)
                .name("SQL-Trace")
                .countQuery()
                .multiline()
                .logQueryBySlf4j(SLF4JLogLevel.TRACE, SQL_TRACE_LOGGER)
                .build();
    }
}
