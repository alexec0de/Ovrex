package dev.ovrex.api.command;

public interface CommandSender {

    void sendMessage(String message);
    boolean hasPermission(String permission);
}
