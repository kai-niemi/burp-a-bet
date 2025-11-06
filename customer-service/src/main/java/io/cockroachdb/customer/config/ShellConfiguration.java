package io.cockroachdb.customer.config;

import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import io.cockroachdb.betting.common.shell.AnsiConsole;
import io.cockroachdb.betting.common.shell.ExitCommand;
import io.cockroachdb.betting.common.shell.JurisdictionValueProvider;
import io.cockroachdb.betting.common.shell.ToggleCommand;
import io.cockroachdb.customer.shell.OperatorAccountValueProvider;

@Configuration
public class ShellConfiguration {
    @Bean
    public ExitCommand exitCommand() {
        return new ExitCommand();
    }

    @Bean
    public ToggleCommand toggleCommand() {
        return new ToggleCommand();
    }

    @Bean
    public JurisdictionValueProvider jurisdictionValueProvider() {
        return new JurisdictionValueProvider();
    }

    @Bean
    public OperatorAccountValueProvider operatorAccountValueProvider() {
        return new OperatorAccountValueProvider();
    }

    @Bean
    public AnsiConsole ansiConsole(@Autowired @Lazy Terminal terminal) {
        return new AnsiConsole(terminal);
    }
}
