package net.ldm.filecraft.command;

import com.jcraft.jsch.JSchException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.ldm.filecraft.networking.ssh.SshConnector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Connect to and use SSH
 */
public class SshCommand {
    // TODO: 2024-01-08 Create a lookup table for Players-SshConnectors, then use that when they try to send commands.

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
        );
    }

    private static void connect(CommandContext<ServerCommandSource> context, String username, String password, String host) {
        context.getSource().sendFeedback(() -> Text.translatable("commands.ssh.connect", host, username), false);
        try {
            SshConnector connector = new SshConnector(username, host);
            if (password != null) connector.setPassword(password);
            connector.connect();
            context.getSource().sendFeedback(() -> Text.translatable("commands.ssh.connect.success", host, username), false);
        } catch (JSchException e) {
            context.getSource().sendError(Text.translatable("commands.ssh.connect.error", e.getLocalizedMessage()));
        }
    }
}
