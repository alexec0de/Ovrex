package dev.ovrex.core.command;

import dev.ovrex.api.OvrexAPI;
import dev.ovrex.api.command.Command;
import dev.ovrex.api.command.CommandSender;

public class EndCommand extends Command {
    private final OvrexAPI api;

    public EndCommand(OvrexAPI api) {
        super("end", "stop", "shutdown");
        this.api = api;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("§cShutting down proxy...");
        api.shutdown();
    }
}
