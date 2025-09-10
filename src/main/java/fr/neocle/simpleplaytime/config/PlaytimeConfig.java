package fr.neocle.simpleplaytime.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

public class PlaytimeConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_REWARDS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> REWARD_CONFIGS;

    static {
        BUILDER.push("rewards");

        ENABLE_REWARDS = BUILDER
                .comment("Enable automatic rewards based on playtime")
                .define("enableRewards", true);

        REWARD_CONFIGS = BUILDER
                .comment("Reward configurations in format: 'hours:type:value'",
                        "Types: 'group' for adding to group, 'permission' for adding permission, 'command' for custom command",
                        "Examples:",
                        "  '24:group:apprentice' - adds player to 'apprentice' group after 24 hours",
                        "  '168:group:architect' - adds player to 'architect' group after 168 hours (1 week)",
                        "  '1:permission:example.reward' - gives permission after 1 hour",
                        "  '48:command:say %player% is awesome!' - executes custom command after 48 hours",
                        " You can put multiple rewards for the same hour, they will all be processed.")
                .defineList("rewardConfigs",
                        List.of(
                                "1:command:say %player% has played for 1 hour!",
                                "24:group:apprentice",
                                "168:group:architect",
                                "720:group:bliblablu"
                        ),
                        obj -> obj instanceof String);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}