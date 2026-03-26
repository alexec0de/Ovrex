package dev.ovrex.api.scheduler;

public interface ScheduledTask {
    int getId();
    void cancel();
    boolean isCancelled();
}
