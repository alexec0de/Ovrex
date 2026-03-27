package dev.ovrex.core.command;

import dev.ovrex.api.command.Command;
import dev.ovrex.api.command.CommandSender;
import dev.ovrex.api.player.ProxyPlayer;
import dev.ovrex.api.server.BackendServer;
import dev.ovrex.api.server.ServerManager;

import java.util.Optional;

public class ServerCommand extends Command {

    private final ServerManager serverManager;

    public ServerCommand(ServerManager serverManager) {
        super("server", "sv");
        this.serverManager = serverManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxyPlayer player)) {
            sender.sendMessage("This command can only be used by players");
            return;
        }

        if (args.length == 0) {
            sender.sendMessage("§eAvailable servers:");
            serverManager.getAllServers().forEach(server ->
                    sender.sendMessage("  §7- §a" + server.getName() +
                            " §7[" + server.getPlayers().size() + " players] [" + server.getServerType() + "]"));
            return;
        }

        final String serverName = args[0];
        final Optional<BackendServer> server = serverManager.getServer(serverName);

        if (server.isEmpty()) {
            sender.sendMessage("§cServer not found: " + serverName);
            return;
        }

        player.connect(server.get());
        sender.sendMessage("§aConnecting to " + serverName + "...");
    }
}
