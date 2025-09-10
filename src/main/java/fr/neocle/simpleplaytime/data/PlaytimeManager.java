package fr.neocle.simpleplaytime.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.fml.loading.FMLPaths;
import fr.neocle.simpleplaytime.config.PlaytimeConfig;
import fr.neocle.simpleplaytime.integration.LuckPermsIntegration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlaytimeManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static PlaytimeManager INSTANCE;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<UUID, Long> playtimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> sessionStartTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Set<Integer>> givenRewards = new ConcurrentHashMap<>();
    private final Path configDir;
    private final Path dataFile;

    private PlaytimeManager() {
        this.configDir = FMLPaths.CONFIGDIR.get().resolve("simpleplaytime");
        this.dataFile = configDir.resolve("playtimes.json");

        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
        }
    }

    public static PlaytimeManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlaytimeManager();
        }
        return INSTANCE;
    }

    public void onPlayerLogin(ServerPlayer player) {
        UUID playerId = player.getUUID();
        sessionStartTimes.put(playerId, System.currentTimeMillis());

        if (!playtimes.containsKey(playerId)) {
            playtimes.put(playerId, 0L);
        }

        if (!givenRewards.containsKey(playerId)) {
            givenRewards.put(playerId, new HashSet<>());
        }
    }

    public void onPlayerLogout(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Long sessionStart = sessionStartTimes.remove(playerId);

        if (sessionStart != null) {
            long sessionTime = System.currentTimeMillis() - sessionStart;
            playtimes.merge(playerId, sessionTime, Long::sum);

            checkAndGiveRewards(player);
            saveData();
        }
    }

    public long getPlaytime(UUID playerId) {
        long totalTime = playtimes.getOrDefault(playerId, 0L);

        Long sessionStart = sessionStartTimes.get(playerId);
        if (sessionStart != null) {
            totalTime += System.currentTimeMillis() - sessionStart;
        }

        return totalTime;
    }

    public String getFormattedPlaytime(UUID playerId) {
        long millis = getPlaytime(playerId);
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (millis % (1000 * 60)) / 1000;

        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    public void checkAndGiveRewards(ServerPlayer player) {
        if (!PlaytimeConfig.ENABLE_REWARDS.get()) {
            return;
        }

        UUID playerId = player.getUUID();
        long playtimeMillis = getPlaytime(playerId);
        long currentHours = playtimeMillis / (1000 * 60 * 60);

        Set<Integer> playerRewards = givenRewards.getOrDefault(playerId, new HashSet<>());
        List<? extends String> rewardConfigs = PlaytimeConfig.REWARD_CONFIGS.get();

        for (int i = 0; i < rewardConfigs.size(); i++) {
            String config = rewardConfigs.get(i);

            if (playerRewards.contains(i)) {
                continue;
            }

            String[] parts = config.split(":", 3);
            if (parts.length < 3) {
                continue;
            }

            try {
                int requiredHours = Integer.parseInt(parts[0]);
                String type = parts[1].toLowerCase();
                String value = parts[2];

                if (currentHours >= requiredHours) {
                    boolean success = false;
                    switch (type) {
                        case "group":
                            success = LuckPermsIntegration.addPlayerToGroup(player, value);
                            break;
                        case "permission":
                            success = LuckPermsIntegration.givePlayerPermission(player, value);
                            break;
                        case "command":
                            String command = value.replace("%player%", player.getGameProfile().getName());
                            success = executeCommand(player, command);
                            break;
                        default:
                            LOGGER.warn("Unknown reward type: {}", type);
                            continue;
                    }

                    if (success) {
                        playerRewards.add(i);
                        givenRewards.put(playerId, playerRewards);
                    }
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid reward config (bad number): {}", config);
            }
        }
    }

    private boolean executeCommand(ServerPlayer player, String command) {
        try {
            CommandSourceStack source = player.getServer().createCommandSourceStack();
            player.getServer().getCommands().performPrefixedCommand(source, command);
            return true;
        } catch (Exception e) {
            LOGGER.error("Exception while executing reward command: {}", command, e);
            return false;
        }
    }


    public void saveData() {
        for (Map.Entry<UUID, Long> entry : sessionStartTimes.entrySet()) {
            UUID playerId = entry.getKey();
            long sessionStart = entry.getValue();
            long sessionTime = System.currentTimeMillis() - sessionStart;
            playtimes.merge(playerId, sessionTime, Long::sum);

            sessionStartTimes.put(playerId, System.currentTimeMillis());
        }

        try {
            PlaytimeData data = new PlaytimeData();
            data.playtimes = new HashMap<>(playtimes);
            data.givenRewards = new HashMap<>(givenRewards);

            String json = gson.toJson(data);
            Files.write(dataFile, json.getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to save playtime data", e);
        }
    }


    public void loadData() {
        if (!Files.exists(dataFile)) {
            return;
        }

        try {
            String json = Files.readString(dataFile);
            PlaytimeData data = gson.fromJson(json, PlaytimeData.class);

            if (data.playtimes != null) {
                playtimes.clear();
                playtimes.putAll(data.playtimes);
            }

            if (data.givenRewards != null) {
                givenRewards.clear();
                givenRewards.putAll(data.givenRewards);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load playtime data", e);
        }
    }

    public void setPlaytimeMillis(UUID playerId, long millis) {
        playtimes.put(playerId, millis);

        if (sessionStartTimes.containsKey(playerId)) {
            sessionStartTimes.put(playerId, System.currentTimeMillis());
        }
        
        saveData();
    }
 
    private static class PlaytimeData {
        public Map<UUID, Long> playtimes;
        public Map<UUID, Set<Integer>> givenRewards;
    }
}