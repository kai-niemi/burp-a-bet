package io.cockroachdb.betting.common.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Quit;

@ShellComponent
public class ExitCommand implements Quit.Command {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @ShellMethod(value = "Exit the shell", key = {"q", "quit", "exit"})
    public void quit() {
        applicationContext.close();
        throw new ExitRequest();
    }
}
