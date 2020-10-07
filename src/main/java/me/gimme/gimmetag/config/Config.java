package me.gimme.gimmetag.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.Set;

public abstract class Config {

    public static final AbstractConfig<Boolean> DISABLE_HUNGER = new ValueConfig<>("disable-hunger", Boolean.class);
    public static final AbstractConfig<Boolean> ENABLE_PVP = new ValueConfig<>("enable-pvp", Boolean.class);
    public static final AbstractConfig<String> GAME_MODE = new ValueConfig<>("game-mode", String.class);
    public static final AbstractConfig<Set<String>> ENABLE_KNOCKBACK = new SetConfig<>("enable-knockback");


    public static final AbstractConfig<Integer> NUMBER_OF_HUNTERS = new ValueConfig<>("number-of-hunters", Integer.class);

    private static final AbstractConfig<ConfigurationSection> TAG = new ValueConfig<>("tag", ConfigurationSection.class);
    public static final AbstractConfig<Integer> TAG_SLEEP_TIME = new ValueConfig<>(TAG, "sleep-time", Integer.class);
    public static final AbstractConfig<Integer> TAG_DEATH_DISTANCE = new ValueConfig<>(TAG, "death-distance", Integer.class);


    private static final AbstractConfig<ConfigurationSection> SCORING = new ValueConfig<>("scoring", ConfigurationSection.class);
    public static final AbstractConfig<Integer> SCORING_STARTING_LEVEL = new ValueConfig<>(SCORING, "starting-level", Integer.class);
    public static final AbstractConfig<Integer> SCORING_INITIAL_HUNTER_STARTING_LEVEL = new ValueConfig<>(SCORING, "initial-hunter-starting-level", Integer.class);
    public static final AbstractConfig<Integer> SCORING_LEVELS_TO_END = new ValueConfig<>(SCORING, "levels-to-end", Integer.class);
    public static final AbstractConfig<Boolean> SCORING_END_CONDITION_OVER = new ValueConfig<>(SCORING, "end-condition-over", Boolean.class);
    public static final AbstractConfig<Boolean> SCORING_HIGHEST_SCORE_WINS = new ValueConfig<>(SCORING, "highest-score-wins", Boolean.class);
    public static final AbstractConfig<Integer> SCORING_POINTS_PER_LEVEL = new ValueConfig<>(SCORING, "points-per-level", Integer.class);
    private static final AbstractConfig<ConfigurationSection> SCORING_UPDATE_TICK = new ValueConfig<>(SCORING, "update-tick", ConfigurationSection.class);
    public static final AbstractConfig<Integer> SCORING_PERIOD = new ValueConfig<>(SCORING_UPDATE_TICK, "period", Integer.class);
    public static final AbstractConfig<Integer> SCORING_RUNNER_POINTS_PER_TICK = new ValueConfig<>(SCORING_UPDATE_TICK, "runner-points-per-tick", Integer.class);
    public static final AbstractConfig<Integer> SCORING_HUNTER_POINTS_PER_TICK = new ValueConfig<>(SCORING_UPDATE_TICK, "hunter-points-per-tick", Integer.class);
    public static final AbstractConfig<Integer> SCORING_POINTS_ON_TAG = new ValueConfig<>(SCORING, "points-on-tag", Integer.class);
    public static final AbstractConfig<Integer> SCORING_POINTS_ON_TAGGED = new ValueConfig<>(SCORING, "points-on-tagged", Integer.class);
    public static final AbstractConfig<Integer> SCORING_HUNTER_DISTANCE = new ValueConfig<>(SCORING, "hunter-distance", Integer.class);


    private static final AbstractConfig<ConfigurationSection> HUNTER = new ValueConfig<>("hunter", ConfigurationSection.class);
    public static final AbstractConfig<Integer> HUNTER_LEATHER_COLOR = new ValueConfig<>(HUNTER, "leather-color", Integer.class);
    public static final AbstractConfig<Boolean> HUNTER_TEAMMATE_OUTLINE = new ValueConfig<>(HUNTER, "teammate-outline", Boolean.class);
    public static final AbstractConfig<Boolean> HUNTER_HIDE_NAME_TAG = new ValueConfig<>(HUNTER, "hide-name-tag", Boolean.class);
    public static final AbstractConfig<Map<String, Integer>> HUNTER_ITEMS = new MapConfig<>(HUNTER, "items");

    private static final AbstractConfig<ConfigurationSection> RUNNER = new ValueConfig<>("runner", ConfigurationSection.class);
    public static final AbstractConfig<Boolean> RUNNER_TEAMMATE_OUTLINE = new ValueConfig<>(RUNNER, "teammate-outline", Boolean.class);
    public static final AbstractConfig<Boolean> RUNNER_HIDE_NAME_TAG = new ValueConfig<>(RUNNER, "hide-name-tag", Boolean.class);
    public static final AbstractConfig<Boolean> RUNNER_COLLISION_WITH_RUNNER = new ValueConfig<>(RUNNER, "collision-with-runner", Boolean.class);
    public static final AbstractConfig<Boolean> RUNNER_COLLISION_WITH_HUNTER = new ValueConfig<>(RUNNER, "collision-with-hunter", Boolean.class);
    public static final AbstractConfig<Map<String, Integer>> RUNNER_ITEMS = new MapConfig<>(RUNNER, "items");


    private static final AbstractConfig<ConfigurationSection> CUSTOM_ITEM = new ValueConfig<>("custom-item", ConfigurationSection.class);

    public static final AbilityItemConfig SWAPPER_BALL = new AbilityItemConfig(CUSTOM_ITEM, "swapper_ball");
    public static final AbstractConfig<Boolean> SWAPPER_ALLOW_HUNTER_SWAP = new ValueConfig<>(SWAPPER_BALL, "allow-hunter-swap", Boolean.class);
    public static final AbstractConfig<ConfigurationSection> SPEED_BOOSTS = new ValueConfig<>(CUSTOM_ITEM, "speed-boosts", ConfigurationSection.class);
    private static final AbstractConfig<ConfigurationSection> INVIS_POTION = new ValueConfig<>(CUSTOM_ITEM, "invis_potion", ConfigurationSection.class);
    public static final AbstractConfig<Number> INVIS_POTION_DURATION = new ValueConfig<>(INVIS_POTION, "duration", Number.class);
    public static final AbilityItemConfig BALLOON_GRENADE = new AbilityItemConfig(CUSTOM_ITEM, "balloon_grenade");
    public static final AbilityItemConfig HUNTER_COMPASS = new AbilityItemConfig(CUSTOM_ITEM, "hunter_compass");
    public static final AbilityItemConfig HUNTER_RADAR = new AbilityItemConfig(CUSTOM_ITEM, "hunter_radar");

    private static final BouncyProjectileConfig DEFAULT_BOUNCY_PROJECTILE = new BouncyProjectileConfig("default-bouncy-projectile", null);
    public static final BouncyProjectileConfig SMOKE_GRENADE = new BouncyProjectileConfig(CUSTOM_ITEM, "smoke_grenade", DEFAULT_BOUNCY_PROJECTILE);
    public static final AbstractConfig<Integer> SMOKE_GRENADE_COLOR = new ValueConfig<>(SMOKE_GRENADE, "color", Integer.class);
    public static final AbstractConfig<Boolean> SMOKE_GRENADE_USE_TEAM_COLOR = new ValueConfig<>(SMOKE_GRENADE, "use-team-color", Boolean.class);
    public static final BouncyProjectileConfig IMPULSE_GRENADE = new BouncyProjectileConfig(CUSTOM_ITEM, "impulse_grenade", DEFAULT_BOUNCY_PROJECTILE);
}
