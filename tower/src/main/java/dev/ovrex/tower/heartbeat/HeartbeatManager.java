package dev.ovrex.tower.heartbeat;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HeartbeatManager {
    private final Map<Channel, Long> lastHeartbeat = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final long timeoutMs;
    private final Runnable onTimeout;

    public HeartbeatManager(long timeoutMs) {
        this.timeoutMs = timeoutMs;
        this.onTimeout = () -> {};
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkHeartbeats, 5, 5, TimeUnit.SECONDS);
    }

    public void registerChannel(Channel channel) {
        lastHeartbeat.put(channel, System.currentTimeMillis());
    }

    public void updateHeartbeat(Channel channel) {
        lastHeartbeat.put(channel, System.currentTimeMillis());
    }

    public void removeChannel(Channel channel) {
        lastHeartbeat.remove(channel);
    }

    private void checkHeartbeats() {
        long now = System.currentTimeMillis();
        lastHeartbeat.forEach((channel, lastTime) -> {
            if (now - lastTime > timeoutMs) {
                log.warn("Heartbeat timeout for channel: {}", channel.remoteAddress());
                channel.close();
                lastHeartbeat.remove(channel);
            }
        });
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
