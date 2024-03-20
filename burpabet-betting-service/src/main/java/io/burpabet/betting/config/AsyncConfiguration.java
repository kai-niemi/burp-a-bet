package io.burpabet.betting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.burpabet.betting.shell.support.WorkloadExecutor;

@Configuration
@EnableScheduling
public class AsyncConfiguration {
    @Value("${burp.maximum-threads}")
    private int threadPoolSize;

    @Bean
    public ThreadPoolTaskExecutor getThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(threadPoolSize);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setPrestartAllCoreThreads(false);
        executor.setThreadNamePrefix("worker-");
        return executor;
    }

    @Bean
    public WorkloadExecutor workloadExecutor() {
        return new WorkloadExecutor(getThreadPoolExecutor());
    }
}
