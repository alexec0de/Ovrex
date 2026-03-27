package dev.ovrex.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OvrexBootstrap {
    public static void main(String[] args) {
        log.info("=================================================");
        log.info("   Ovrex - created by alexec0de (https://t.me/alexec0dec) ");
        log.info("=================================================");

        final OvrexServer proxy = new OvrexServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook triggered");
            proxy.shutdown();
        }));

        try {
            proxy.start();
        } catch (Exception e) {
            log.error("Fatal error during startup", e);
            System.exit(1);
        }
    }
}
