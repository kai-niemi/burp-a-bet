package io.burpabet.betting.shell.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Predicate;

public class WorkloadExecutor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final LinkedHashMap<String, LinkedList<Future<?>>> futures = new LinkedHashMap<>();

    private final ThreadPoolTaskExecutor threadPoolExecutor;

    public WorkloadExecutor(ThreadPoolTaskExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public <V> Future<V> submit(String id, Callable<V> runnable, Predicate<Integer> completion) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        CallContext context = CallMetrics.of(id,
                () -> (int) futures.getOrDefault(id, new LinkedList<>())
                        .stream()
                        .filter(voidFuture -> !(voidFuture.isDone() || voidFuture.isCancelled()))
                        .count());

        Future<V> future = threadPoolExecutor.submit(() -> {
            logger.info("Started '{}'", id);

            int i = 0;

            while (completion.test(++i)) {
                if (Thread.interrupted()) {
                    logger.warn("Interrupted '{}'", id);
                    break;
                }

                final long callTime = context.before();
                try {
                    runnable.call();
                    context.after(callTime, null);
                } catch (Throwable e) {
                    context.after(callTime, e);
                    logger.error("Worker '" + id + "' error (cancelling)", e);
                    break;
                }
            }

            logger.info("Finished '{}'", id);

            return null;
        });

        futures.computeIfAbsent(id, s -> new LinkedList<>()).add(future);

        return future;
    }

    public boolean hasActiveWorkers() {
        return threadPoolExecutor.getActiveCount() > 0;
    }

    public void cancelFuture(String id) {
        cancel(futures.remove(id));
    }

    public void cancelAllFutures() {
        futures.values().forEach(this::cancel);
        futures.clear();
    }

    private void cancel(LinkedList<Future<?>> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        logger.info("Cancelling {} futures", list.size());

        while (!list.isEmpty()) {
            Future<?> f = list.pop();
            try {
                f.cancel(true);
                f.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.debug("", e);
            }
        }
    }
}