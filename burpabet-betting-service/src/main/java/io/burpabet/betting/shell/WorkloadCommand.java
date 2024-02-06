package io.burpabet.betting.shell;

import io.burpabet.betting.shell.support.CallMetrics;
import io.burpabet.betting.shell.support.MetricsListener;
import io.burpabet.betting.shell.support.WorkloadExecutor;
import io.burpabet.common.shell.AnsiConsole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@ShellComponent
@ShellCommandGroup("Workload Commands")
public class WorkloadCommand extends AbstractShellComponent {
    private static final ReentrantLock mutex = new ReentrantLock();

    @Autowired
    private AnsiConsole ansiConsole;

    @Autowired
    private WorkloadExecutor workloadExecutor;

    @Scheduled(initialDelay = 2, fixedDelay = 6, timeUnit = TimeUnit.SECONDS)
    public void printMetrics() {
        if (workloadExecutor.hasActiveWorkers()) {
            try {
                mutex.lock();
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
            } finally {
                mutex.unlock();
            }
        }
    }

    @ShellMethod(value = "Clear workload metrics", key = {"c", "clear-metrics"})
    public void clearMetrics() {
        CallMetrics.clear();
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
