package dev.ovrex.core.impl;

import dev.ovrex.api.command.Command;
import dev.ovrex.api.command.CommandManager;
import dev.ovrex.api.command.CommandSender;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OvrexCommandManager implements CommandManager {
    private final Map<String, Command> commands = new ConcurrentHashMap<>();

    @Override
    public void register(Command command) {
        commands.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias.toLowerCase(), command);
        }
        log.debug("Command registered: {}", command.getName());
    }

    @Override
    public void unregister(String name) {
        commands.remove(name.toLowerCase());
    }

    @Override
    public boolean execute(CommandSender sender, String input) {
        String[] parts = input.split("\\s+");
        if (parts.length == 0) return false;

        String cmdName = parts[0].toLowerCase();
        Command command = commands.get(cmdName);

        if (command == null) {
            return false;
        }

        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        try {
            command.execute(sender, args);
        } catch (Exception e) {
            log.error("Error executing command: {}", cmdName, e);
            sender.sendMessage("§cAn error occurred while executing the command");
        }

        return true;
    }
}
