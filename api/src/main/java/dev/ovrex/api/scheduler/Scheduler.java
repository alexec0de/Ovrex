package dev.ovrex.api.scheduler;

import com.sun.source.util.Plugin;
import dev.ovrex.api.plugin.OvrexPlugin;

import java.util.concurrent.TimeUnit;

public interface Scheduler {

    ScheduledTask runAsync(OvrexPlugin plugin, Runnable task);
    ScheduledTask schedule(OvrexPlugin plugin, Runnable task, long delay, TimeUnit unit);
    ScheduledTask scheduleRepeating(OvrexPlugin plugin, Runnable task, long delay, long period, TimeUnit unit);

    void cancelAll(Plugin plugin);
}
