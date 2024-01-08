package net.ldm.filecraft.command;

import com.jcraft.jsch.JSchException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.ldm.filecraft.networking.ssh.SshConnector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

/**
 * Connect to and use SSH
 */
public class SshCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("ssh")
                .then(literal("connect")
                        .then(argument("username", StringArgumentType.string())
                                .then(argument("host", StringArgumentType.string())
                                        .executes(SshCommand::connect)
                                )
                                .then(argument("password", StringArgumentType.string())
                                        .then(argument("host", StringArgumentType.string())
                                                .executes(SshCommand::connect)
                                        )
                                )
                        )
                )
        );
    }

    private static int connect(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("Attempting to connect"), false);
        try {
            SshConnector connector = new SshConnector(StringArgumentType.getString(context, "username"), StringArgumentType.getString(context, "host"));
            connector.setPassword(StringArgumentType.getString(context, "password"));
            connector.connect();
            context.getSource().sendFeedback(() -> Text.literal("Connected"), false);
        } catch (JSchException e) {
            context.getSource().sendError(Text.literal(e.getLocalizedMessage()));
        }
        return 1;
    }
}
