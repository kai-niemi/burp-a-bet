package io.burpabet.wallet.config;

import io.burpabet.common.shell.AnsiConsole;
import io.burpabet.wallet.shell.OperatorValueProvider;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.burpabet.common.shell.ExitCommand;
import io.burpabet.common.shell.JurisdictionValueProvider;
import io.burpabet.common.shell.ToggleCommand;

import org.springframework.context.annotation.Lazy;

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
    public OperatorValueProvider operatorValueProvider() {
        return new OperatorValueProvider();
    }

    @Bean
    public AnsiConsole ansiConsole(@Autowired @Lazy Terminal terminal) {
        return new AnsiConsole(terminal);
    }
}
