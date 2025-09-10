package fr.neocle.simpleplaytime.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import fr.neocle.simpleplaytime.data.PlaytimeManager;

public class PlaytimeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playtime")
                .executes(PlaytimeCommand::showOwnPlaytime)
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(source -> source.hasPermission(2))
                        .executes(PlaytimeCommand::showPlayerPlaytime))
        );
    }

    private static int showOwnPlaytime(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        String playtime = PlaytimeManager.getInstance().getFormattedPlaytime(player.getUUID());
        source.sendSuccess(() -> Component.literal("Your playtime: " + playtime), false);

        return 1;
    }

    private static int showPlayerPlaytime(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");

        String playtime = PlaytimeManager.getInstance().getFormattedPlaytime(targetPlayer.getUUID());
        source.sendSuccess(() -> Component.literal(targetPlayer.getGameProfile().getName() + "'s playtime: " + playtime), false);

        return 1;
    }
}