package net.ldm.filecraft.command;

import com.jcraft.jsch.JSchException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.ldm.filecraft.networking.ssh.SshConnector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.HashMap;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Connect to and use SSH
 */
public class SshCommand {
    private static final HashMap<ServerPlayerEntity, SshConnector> PLAYER_CONNECTORS = new HashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("ssh")
                .then(literal("connect")
                        .then(argument("username", StringArgumentType.string())
                                .then(argument("host", StringArgumentType.string())
                                        .executes(context -> {
                                            connect(context, StringArgumentType.getString(context, "username"),
                                                    null, StringArgumentType.getString(context, "host"));
                                            return 1;
                                        })
                                )
                                .then(argument("password", StringArgumentType.string())
                                        .then(argument("host", StringArgumentType.string())
                                                .executes(context -> {
                                                    connect(context, StringArgumentType.getString(context, "username"),
                                                            StringArgumentType.getString(context, "password"),
                                                            StringArgumentType.getString(context, "host"));
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
                .then(literal("disconnect").executes(SshCommand::disconnect))
                .then(literal("execute")
                        .then(argument("command", StringArgumentType.greedyString())
                            .executes(SshCommand::execute)
                        )
                )
        );
    }

    private static void connect(CommandContext<ServerCommandSource> context, String username, String password, String host) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        SshConnector c = PLAYER_CONNECTORS.get(player);
        if (c != null) {
            context.getSource().sendFeedback(() -> Text.translatable("commands.ssh.connect.already_connected", c.getHost()), false);
            return;
        }

        context.getSource().sendFeedback(() -> Text.translatable("commands.ssh.connect.already_connected", host), false);
        try {
            PLAYER_CONNECTORS.put(player, new SshConnector(username, host));
            SshConnector connector = PLAYER_CONNECTORS.get(player);
            if (password != null) connector.setPassword(password);
            connector.connect();
            context.getSource().sendFeedback(() -> Text.translatable("commands.ssh.connect.success", host, username), false);
        } catch (JSchException e) {
            context.getSource().sendError(Text.translatable("commands.ssh.connect.error", e.getLocalizedMessage()));
        }
    }

    @SuppressWarnings("SameReturnValue")
    private static int disconnect(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        SshConnector connector = PLAYER_CONNECTORS.get(player);
        if (connector == null) {
            context.getSource().sendFeedback(() -> Text.translatable("commands.ssh.error.not_connected"), false);
            return 1;
        }
        connector.disconnect();
        PLAYER_CONNECTORS.remove(player);
        context.getSource().sendFeedback(() -> Text.translatable("commands.ssh.disconnect"), false);
        return 1;
    }

    @SuppressWarnings("SameReturnValue")
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        SshConnector connector = PLAYER_CONNECTORS.get(player);
        if (connector == null) {
            context.getSource().sendFeedback(() -> Text.translatable("commands.ssh.error.not_connected"), false);
            return 1;
        }
        try {
            String out = connector.execute(StringArgumentType.getString(context, "command"));
            context.getSource().sendFeedback(() -> Text.literal(out), false);
        } catch (JSchException | IOException | InterruptedException e) {
            context.getSource().sendError(Text.translatable("command.ssh.execute.error", e.getLocalizedMessage()));
        }
        return 1;
    }
}
