package io.burpabet.common.shell;

import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@ShellCommandGroup(CommandGroups.OPERATOR)
public class ToggleCommand {
    public static final String TRACE_LOGGER = "io.burpabet";

    public static final String SQL_TRACE_LOGGER = "io.burpabet.SQL_TRACE";

    @ShellMethod(value = "Cycle log levels", key = {"l", "log-level"})
    public void cycleLogLevels() {
        cycleLevel(TRACE_LOGGER, Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR);
        cycleLevel(SQL_TRACE_LOGGER, Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR);
    }

    private void cycleLevel(String name, Level l1, Level l2, Level l3, Level l4, Level l5) {
        ch.qos.logback.classic.LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext) LoggerFactory
                .getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger(name);
        if (logger.getLevel().isGreaterOrEqual(l5)) {
            logger.setLevel(l1);
        } else if (logger.getLevel().isGreaterOrEqual(l4)) {
            logger.setLevel(l5);
        } else if (logger.getLevel().isGreaterOrEqual(l3)) {
            logger.setLevel(l4);
        } else if (logger.getLevel().isGreaterOrEqual(l2)) {
            logger.setLevel(l3);
        } else if (logger.getLevel().isGreaterOrEqual(l1)) {
            logger.setLevel(l2);
        }

        if (logger.getLevel().equals(Level.ERROR)) {
            logger.error("Log level for '%s' is %s".formatted(name, logger.getLevel()));
        } else if (logger.getLevel().equals(Level.WARN)) {
            logger.warn("Log level for '%s' is %s".formatted(name, logger.getLevel()));
        } else if (logger.getLevel().equals(Level.INFO)) {
            logger.info("Log level for '%s' is %s".formatted(name, logger.getLevel()));
        } else if (logger.getLevel().equals(Level.DEBUG)) {
            logger.debug("Log level for '%s' is %s".formatted(name, logger.getLevel()));
        } else if (logger.getLevel().equals(Level.TRACE)) {
            logger.trace("Log level for '%s' is %s".formatted(name, logger.getLevel()));
        }
    }
}
