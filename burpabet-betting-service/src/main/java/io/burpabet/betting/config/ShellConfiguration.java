package io.burpabet.betting.config;

import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import io.burpabet.betting.shell.CustomerValueProvider;
import io.burpabet.betting.shell.RaceValueProvider;
import io.burpabet.common.shell.AnsiConsole;
import io.burpabet.common.shell.ExitCommand;
import io.burpabet.common.shell.ToggleCommand;

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
    public CustomerValueProvider customerValueProvider() {
        return new CustomerValueProvider();
    }

    @Bean
    public RaceValueProvider raceValueProvider() {
        return new RaceValueProvider();
    }

    @Bean
    public AnsiConsole ansiConsole(@Autowired @Lazy Terminal terminal) {
        return new AnsiConsole(terminal);
    }
}
