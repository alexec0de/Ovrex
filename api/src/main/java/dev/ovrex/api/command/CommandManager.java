package dev.ovrex.api.command;

public interface CommandManager {
    void register(Command command);

    void unregister(String name);

    boolean execute(CommandSender sender, String input);
}
