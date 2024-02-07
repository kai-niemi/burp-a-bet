package io.burpabet.common.shell;

import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import ch.qos.logback.classic.Level;

@ShellComponent
@ShellCommandGroup(CommandGroups.OPERATOR)
public class ToggleCommand {
    public static final String TRACE_LOGGER = "io.burpabet";

    public static final String SQL_TRACE_LOGGER = "io.burpabet.SQL_TRACE";

    @ShellMethod(value = "Toggle console trace logging", key = {"tt", "toggle-trace"})
    public void toggleTrace() {
        toggleLogLevel(TRACE_LOGGER, Level.TRACE, Level.INFO);
        toggleLogLevel(SQL_TRACE_LOGGER, Level.TRACE, Level.INFO);
    }

    @ShellMethod(value = "Toggle console logging silence", key = {"ts", "toggle-silence"})
    public void toggleSilence() {
        toggleLogLevel(TRACE_LOGGER, Level.INFO, Level.WARN);
        toggleLogLevel(SQL_TRACE_LOGGER, Level.INFO, Level.WARN);
    }

    private void toggleLogLevel(String name, Level verboseLevel, Level normalLevel) {
        ch.qos.logback.classic.LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext) LoggerFactory
                .getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger(name);
        if (logger.getLevel().isGreaterOrEqual(normalLevel)) {
            logger.setLevel(verboseLevel);
            logger.warn("Set log level %s for %s".formatted(verboseLevel, name));
        } else {
            logger.setLevel(normalLevel);
            logger.warn("Set log level %s for %s".formatted(normalLevel, name));
        }
    }
}
