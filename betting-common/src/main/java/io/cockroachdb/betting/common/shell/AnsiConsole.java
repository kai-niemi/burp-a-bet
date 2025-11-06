package io.cockroachdb.betting.common.shell;

import org.jline.terminal.Terminal;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.util.Assert;

public class AnsiConsole {
    private final Terminal terminal;

    public AnsiConsole(Terminal terminal) {
        Assert.notNull(terminal, "terminal is null");
        this.terminal = terminal;
    }

    public AnsiConsole cyan(String text) {
        write(AnsiColor.BRIGHT_CYAN, text);
        return this;
    }

    public AnsiConsole red(String text) {
        write(AnsiColor.BRIGHT_RED, text);
        return this;
    }

    public AnsiConsole green(String text) {
        write(AnsiColor.BRIGHT_GREEN, text);
        return this;
    }

    public AnsiConsole blue(String text) {
        write(AnsiColor.BRIGHT_BLUE, text);
        return this;
    }

    public AnsiConsole yellow(String text) {
        write(AnsiColor.BRIGHT_YELLOW, text);
        return this;
    }

    public AnsiConsole magenta(String text) {
        write(AnsiColor.BRIGHT_MAGENTA, text);
        return this;
    }

    private synchronized AnsiConsole write(AnsiColor color, String text) {
        try {
            terminal.writer().write(AnsiOutput.toString(color, text, AnsiColor.DEFAULT));
            terminal.writer().flush();
        } catch (BeanCreationNotAllowedException e) {
            // during shutdown
            System.out.printf(text);
        }
        return this;
    }

    public synchronized AnsiConsole nl() {
        try {
            terminal.writer().println();
            terminal.writer().flush();
        } catch (BeanCreationNotAllowedException e) {
            // during shutdown
            System.out.println();
        }
        return this;
    }
}
