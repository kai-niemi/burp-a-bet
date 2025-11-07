package io.cockroachdb.betting.config;

import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import io.cockroachdb.betting.shell.CustomerValueProvider;
import io.cockroachdb.betting.shell.RaceValueProvider;
import io.cockroachdb.betting.common.shell.AnsiConsole;
import io.cockroachdb.betting.common.shell.ExitCommand;
import io.cockroachdb.betting.common.shell.JurisdictionValueProvider;
import io.cockroachdb.betting.common.shell.ToggleCommand;
import io.cockroachdb.betting.shell.StakeValueProvider;

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
    public StakeValueProvider stakeValueProvider() {
        return new StakeValueProvider();
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
    public JurisdictionValueProvider jurisdictionValueProvider() {
        return new JurisdictionValueProvider();
    }

    @Bean
    public AnsiConsole ansiConsole(@Autowired @Lazy Terminal terminal) {
        return new AnsiConsole(terminal);
    }
}
