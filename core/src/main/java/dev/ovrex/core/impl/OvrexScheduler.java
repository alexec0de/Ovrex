package dev.ovrex.core.impl;

import dev.ovrex.api.plugin.OvrexPlugin;
import dev.ovrex.api.scheduler.ScheduledTask;
import dev.ovrex.api.scheduler.Scheduler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class OvrexScheduler implements Scheduler {
    private final ScheduledExecutorService executor;
    private final Map<Integer, OvrexScheduledTask> tasks = new ConcurrentHashMap<>();
    private final Map<OvrexPlugin, CopyOnWriteArrayList<Integer>> pluginTasks = new ConcurrentHashMap<>();
    private final AtomicInteger taskIdCounter = new AtomicInteger(0);

    public OvrexScheduler() {
        this.executor = Executors.newScheduledThreadPool(4, r -> {
            final Thread t = new Thread(r, "Scheduler-" + taskIdCounter.get());
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public ScheduledTask runAsync(OvrexPlugin plugin, Runnable task) {
        final int id = taskIdCounter.incrementAndGet();
        final Future<?> future = executor.submit(wrapTask(task));
        final OvrexScheduledTask scheduledTask = new OvrexScheduledTask(id, future);
        registerTask(plugin, id, scheduledTask);
        return scheduledTask;
    }

    @Override
    public ScheduledTask schedule(OvrexPlugin plugin, Runnable task, long delay, TimeUnit unit) {
        final int id = taskIdCounter.incrementAndGet();
        final ScheduledFuture<?> future = executor.schedule(wrapTask(task), delay, unit);
        final OvrexScheduledTask scheduledTask = new OvrexScheduledTask(id, future);
        registerTask(plugin, id, scheduledTask);
        return scheduledTask;
    }

    @Override
    public ScheduledTask scheduleRepeating(OvrexPlugin plugin, Runnable task, long delay, long period, TimeUnit unit) {
        final int id = taskIdCounter.incrementAndGet();
        final ScheduledFuture<?> future = executor.scheduleAtFixedRate(wrapTask(task), delay, period, unit);
        final OvrexScheduledTask scheduledTask = new OvrexScheduledTask(id, future);
        registerTask(plugin, id, scheduledTask);
        return scheduledTask;
    }

    @Override
    public void cancelAll(OvrexPlugin plugin) {
        final CopyOnWriteArrayList<Integer> ids = pluginTasks.remove(plugin);
        if (ids != null) {
            ids.forEach(id -> {
                OvrexScheduledTask task = tasks.remove(id);
                if (task != null) task.cancel();
            });
        }
    }

    private void registerTask(OvrexPlugin plugin, int id, OvrexScheduledTask task) {
        tasks.put(id, task);
        pluginTasks.computeIfAbsent(plugin, k -> new CopyOnWriteArrayList<>()).add(id);
    }

    private Runnable wrapTask(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Error in scheduled task", e);
            }
        };
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private static class OvrexScheduledTask implements ScheduledTask {
        private final int id;
        private final Future<?> future;

        OvrexScheduledTask(int id, Future<?> future) {
            this.id = id;
            this.future = future;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void cancel() {
            future.cancel(false);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }
    }
}
