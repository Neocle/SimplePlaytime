package fr.neocle.simpleplaytime.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LuckPermsIntegration {
    private static final Logger LOGGER = LogManager.getLogger();
    private static LuckPerms luckPerms;

    public static void initialize() {
        try {
            luckPerms = LuckPermsProvider.get();
            LOGGER.info("LuckPerms integration initialized successfully");
        } catch (IllegalStateException e) {
            LOGGER.warn("LuckPerms not found, group/permission rewards will not work");
            luckPerms = null;
        }
    }

    public static boolean isAvailable() {
        return luckPerms != null;
    }

    public static boolean addPlayerToGroup(ServerPlayer player, String groupName) {
        if (!isAvailable()) {
            LOGGER.warn("LuckPerms not available, cannot add player {} to group {}", player.getGameProfile().getName(), groupName);
            return false;
        }

        UUID playerId = player.getUUID();

        try {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) {
                CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(playerId);
                user = userFuture.join();
            }

            if (user == null) {
                LOGGER.error("Could not load user data for {}", player.getGameProfile().getName());
                return false;
            }

            InheritanceNode node = InheritanceNode.builder(groupName).build();

            if (user.data().contains(node, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).asBoolean()) {
                LOGGER.info("Player {} already has group {}", player.getGameProfile().getName(), groupName);
                return true;
            }

            user.data().add(node);
            luckPerms.getUserManager().saveUser(user);

            LOGGER.info("Successfully added player {} to group {}", player.getGameProfile().getName(), groupName);
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to add player {} to group {}", player.getGameProfile().getName(), groupName, e);
            return false;
        }
    }

    public static boolean givePlayerPermission(ServerPlayer player, String permission) {
        if (!isAvailable()) {
            LOGGER.warn("LuckPerms not available, cannot give permission {} to player {}", permission, player.getGameProfile().getName());
            return false;
        }

        UUID playerId = player.getUUID();

        try {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) {
                CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(playerId);
                user = userFuture.join();
            }

            if (user == null) {
                LOGGER.error("Could not load user data for {}", player.getGameProfile().getName());
                return false;
            }

            PermissionNode node = PermissionNode.builder(permission).build();

            if (user.data().contains(node, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).asBoolean()) {
                LOGGER.info("Player {} already has permission {}", player.getGameProfile().getName(), permission);
                return true;
            }

            user.data().add(node);
            luckPerms.getUserManager().saveUser(user);

            LOGGER.info("Successfully gave permission {} to player {}", permission, player.getGameProfile().getName());
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to give permission {} to player {}", permission, player.getGameProfile().getName(), e);
            return false;
        }
    }

    public static boolean removePlayerFromGroup(ServerPlayer player, String groupName) {
        if (!isAvailable()) {
            LOGGER.warn("LuckPerms not available, cannot remove player {} from group {}", player.getGameProfile().getName(), groupName);
            return false;
        }

        UUID playerId = player.getUUID();

        try {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) {
                CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(playerId);
                user = userFuture.join();
            }

            if (user == null) {
                LOGGER.error("Could not load user data for {}", player.getGameProfile().getName());
                return false;
            }

            InheritanceNode node = InheritanceNode.builder(groupName).build();
            user.data().remove(node);
            luckPerms.getUserManager().saveUser(user);

            LOGGER.info("Successfully removed player {} from group {}", player.getGameProfile().getName(), groupName);
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to remove player {} from group {}", player.getGameProfile().getName(), groupName, e);
            return false;
        }
    }

    public static boolean removePlayerPermission(ServerPlayer player, String permission) {
        if (!isAvailable()) {
            LOGGER.warn("LuckPerms not available, cannot remove permission {} from player {}", permission, player.getGameProfile().getName());
            return false;
        }

        UUID playerId = player.getUUID();

        try {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) {
                CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(playerId);
                user = userFuture.join();
            }

            if (user == null) {
                LOGGER.error("Could not load user data for {}", player.getGameProfile().getName());
                return false;
            }

            PermissionNode node = PermissionNode.builder(permission).build();
            user.data().remove(node);
            luckPerms.getUserManager().saveUser(user);

            LOGGER.info("Successfully removed permission {} from player {}", permission, player.getGameProfile().getName());
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to remove permission {} from player {}", permission, player.getGameProfile().getName(), e);
            return false;
        }
    }
}