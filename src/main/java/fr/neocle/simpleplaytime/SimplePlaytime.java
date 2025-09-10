package fr.neocle.simpleplaytime;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import fr.neocle.simpleplaytime.config.PlaytimeConfig;
import fr.neocle.simpleplaytime.data.PlaytimeManager;
import fr.neocle.simpleplaytime.commands.PlaytimeCommand;
import fr.neocle.simpleplaytime.integration.LuckPermsIntegration;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(SimplePlaytime.MOD_ID)
public class SimplePlaytime {

    public static final String MOD_ID = "simpleplaytime";
    private int tickCounter = 0;

    public SimplePlaytime(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, PlaytimeConfig.SPEC);

        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlaytimeManager.getInstance().checkAndGiveRewards(player);
        }

        tickCounter++;

        if (tickCounter >= 1200) {
            PlaytimeManager.getInstance().saveData();
            tickCounter = 0;
        }
    }

    private void onServerStarting(ServerStartingEvent event) {
        LuckPermsIntegration.initialize();
        PlaytimeManager.getInstance().loadData();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        PlaytimeManager.getInstance().saveData();
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PlaytimeManager.getInstance().onPlayerLogin((ServerPlayer) event.getEntity());
    }

    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PlaytimeManager.getInstance().onPlayerLogout((ServerPlayer) event.getEntity());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        PlaytimeCommand.register(event.getDispatcher());
    }
}