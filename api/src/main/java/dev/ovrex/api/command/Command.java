package dev.ovrex.api.command;

import lombok.Getter;

@Getter
public abstract class Command {
    private final String name;
    private final String[] aliases;

    protected Command(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public abstract void execute(CommandSender sender, String[] args);
}
