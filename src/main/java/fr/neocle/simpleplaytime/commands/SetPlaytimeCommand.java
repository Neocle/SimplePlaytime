package fr.neocle.simpleplaytime.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import fr.neocle.simpleplaytime.data.PlaytimeManager;
import fr.neocle.simpleplaytime.util.TimeParser;

public class SetPlaytimeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setplaytime")
                .requires(source ->
                        source.hasPermission(2)
                )
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("time", StringArgumentType.string())
                                .executes(SetPlaytimeCommand::setPlaytime)
                        )
                )
        );
    }

    private static int setPlaytime(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
        String timeString = StringArgumentType.getString(context, "time");

        long millis;
        try {
            millis = TimeParser.parseTimeString(timeString);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid time format: " + e.getMessage()));
            return 0;
        }

        PlaytimeManager.getInstance().setPlaytimeMillis(targetPlayer.getUUID(), millis);

        source.sendSuccess(
                () -> Component.literal("Set " + targetPlayer.getGameProfile().getName() +
                        "'s playtime to " + timeString + "."),
                true
        );

        targetPlayer.sendSystemMessage(
                Component.literal("Your playtime has been set to " + timeString + ".")
        );

        return 1;
    }
}
