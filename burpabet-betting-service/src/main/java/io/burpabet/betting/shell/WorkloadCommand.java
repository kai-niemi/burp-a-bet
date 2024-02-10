package io.burpabet.betting.shell;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import io.burpabet.betting.shell.support.CallMetrics;
import io.burpabet.betting.shell.support.MetricsListener;
import io.burpabet.betting.shell.support.WorkloadExecutor;
import io.burpabet.common.shell.AnsiConsole;
import io.burpabet.common.shell.CommandGroups;

@ShellComponent
@ShellCommandGroup(CommandGroups.OPERATOR)
public class WorkloadCommand extends AbstractShellComponent {
    @Autowired
    private AnsiConsole ansiConsole;

    @Autowired
    private WorkloadExecutor workloadExecutor;

    private final AtomicBoolean toggle = new AtomicBoolean();

    @Scheduled(initialDelay = 5, fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void printMetrics() {
        if (workloadExecutor.hasActiveWorkers() && toggle.get()) {
            CallMetrics.print(new MetricsListener() {
                @Override
                public void header(String text) {
                    ansiConsole.yellow(text).nl();
                }

                @Override
                public void body(String text) {
                    ansiConsole.magenta(text).nl();
                }

                @Override
                public void footer(String text) {
                    ansiConsole.blue(text).nl();
                }
            });
        }
    }

    @ShellMethod(value = "Clear workload metrics", key = {"cm", "clear-metrics"})
    public void clearMetrics() {
        CallMetrics.clear();
    }

    @ShellMethod(value = "Pause metrics", key = {"pm", "pause-metrics"})
    public void pauseMetrics() {
        toggle.set(!toggle.get());
        ansiConsole.cyan("Metrics " + (toggle.get() ? "on" : "off")).nl();
    }

    @ShellMethod(value = "Cancel workload(s)", key = {"x", "cancel"})
    public void cancel(@ShellOption(help = "worker name (all if omitted)", defaultValue = ShellOption.NULL) String id) {
        if (id != null) {
            workloadExecutor.cancelFuture(id);
        } else {
            workloadExecutor.cancelAllFutures();
        }
    }
}
