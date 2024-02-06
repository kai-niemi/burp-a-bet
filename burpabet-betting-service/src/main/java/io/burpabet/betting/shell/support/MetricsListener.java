package io.burpabet.betting.shell.support;

public interface MetricsListener {
    void header(String text);

    void body(String text);

    void footer(String text);
}
