package io.burpabet.customer.shell;

import java.io.IOException;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.burpabet.common.shell.AnsiConsole;
import io.burpabet.common.shell.CommandGroups;
import io.burpabet.common.util.Networking;
import io.burpabet.customer.saga.CustomerBettingFacade;
import io.burpabet.customer.service.CustomerService;

@ShellComponent
@ShellCommandGroup(CommandGroups.ADMIN)
public class AdminCommand extends AbstractShellComponent {
    @Autowired
    private Flyway flyway;

    @Value("${server.port}")
    private int port;

    @Autowired
    private AnsiConsole ansiConsole;

    @Autowired
    private CustomerService customerService;

    @ShellMethod(value = "Reset all customer data", key = {"reset"})
    public void reset() {
        customerService.deleteAllInBatch();
        ansiConsole.cyan("Done!").nl();
    }

    @ShellMethod(value = "Run flyway clean+migrate to reset changefeed's (this will drop the schema)",
            key = {"migrate"})
    public void migrate() {
        flyway.clean();
        flyway.migrate();
        ansiConsole.cyan("Done!").nl();
    }

    @ShellMethod(value = "Print and API index url", key = {"u", "url"})
    public void url() throws IOException {
        ansiConsole.cyan("Public URL: %s"
                .formatted(ServletUriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(Networking.getPublicIP())
                        .port(port)
                        .build()
                        .toUriString())).nl();

        ansiConsole.cyan("Local URL: %s"
                .formatted(ServletUriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(Networking.getLocalIP())
                        .port(port)
                        .build()
                        .toUriString())).nl();
    }
}
