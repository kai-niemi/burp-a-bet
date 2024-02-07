package io.burpabet.betting.config;

import io.burpabet.betting.shell.support.WorkloadExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableScheduling
public class AsyncConfiguration implements AsyncConfigurer {
    @Value("${burp.maximum-threads}")
    private int threadPoolSize;

    @Bean
    public ThreadPoolTaskExecutor getAsyncExecutor() {
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
        return new WorkloadExecutor(getAsyncExecutor());
    }
}
