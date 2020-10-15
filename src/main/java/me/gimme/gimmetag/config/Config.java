package me.gimme.gimmetag.config;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.roleclass.RoleClass;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Set;

public abstract class Config {
    private static final AbstractConfig<ConfigurationSection> CONFIG = new ValueConfig<>(GimmeTag.getInstance().getConfig(), "");
    private static final AbstractConfig<ConfigurationSection> CLASSES_CONFIG = new ValueConfig<>(GimmeTag.getInstance().getClassesConfig(), "");
    private static final AbstractConfig<ConfigurationSection> ITEMS_CONFIG = new ValueConfig<>(GimmeTag.getInstance().getItemsConfig(), "");


    public static final AbstractConfig<Boolean> DISABLE_HUNGER = new ValueConfig<>(CONFIG, "disable-hunger");
    public static final AbstractConfig<Boolean> DISABLE_ARROW_DAMAGE = new ValueConfig<>(CONFIG, "disable-arrow-damage");
    public static final AbstractConfig<Boolean> ENABLE_PVP = new ValueConfig<>(CONFIG, "enable-pvp");
    public static final AbstractConfig<String> GAME_MODE = new ValueConfig<>(CONFIG, "game-mode");
    public static final AbstractConfig<Set<String>> ENABLE_KNOCKBACK = new SetConfig<>(CONFIG, "enable-knockback");


    public static final AbstractConfig<Integer> NUMBER_OF_HUNTERS = new ValueConfig<>(CONFIG, "number-of-hunters");

    private static final AbstractConfig<ConfigurationSection> TAG = new ValueConfig<>(CONFIG, "tag");
    public static final AbstractConfig<Integer> TAG_SLEEP_TIME = new ValueConfig<>(TAG, "sleep-time");
    public static final AbstractConfig<Integer> TAG_DEATH_DISTANCE = new ValueConfig<>(TAG, "death-distance");


    private static final AbstractConfig<ConfigurationSection> SCORING = new ValueConfig<>(CONFIG, "scoring");
    public static final AbstractConfig<Integer> SCORING_STARTING_LEVEL = new ValueConfig<>(SCORING, "starting-level");
    public static final AbstractConfig<Integer> SCORING_INITIAL_HUNTER_STARTING_LEVEL = new ValueConfig<>(SCORING, "initial-hunter-starting-level");
    public static final AbstractConfig<Integer> SCORING_LEVELS_TO_END = new ValueConfig<>(SCORING, "levels-to-end");
    public static final AbstractConfig<Boolean> SCORING_END_CONDITION_OVER = new ValueConfig<>(SCORING, "end-condition-over");
    public static final AbstractConfig<Boolean> SCORING_HIGHEST_SCORE_WINS = new ValueConfig<>(SCORING, "highest-score-wins");
    public static final AbstractConfig<Integer> SCORING_POINTS_PER_LEVEL = new ValueConfig<>(SCORING, "points-per-level");
    private static final AbstractConfig<ConfigurationSection> SCORING_UPDATE_TICK = new ValueConfig<>(SCORING, "update-tick");
    public static final AbstractConfig<Integer> SCORING_PERIOD = new ValueConfig<>(SCORING_UPDATE_TICK, "period");
    public static final AbstractConfig<Integer> SCORING_RUNNER_POINTS_PER_TICK = new ValueConfig<>(SCORING_UPDATE_TICK, "runner-points-per-tick");
    public static final AbstractConfig<Integer> SCORING_HUNTER_POINTS_PER_TICK = new ValueConfig<>(SCORING_UPDATE_TICK, "hunter-points-per-tick");
    public static final AbstractConfig<Integer> SCORING_POINTS_ON_TAG = new ValueConfig<>(SCORING, "points-on-tag");
    public static final AbstractConfig<Integer> SCORING_POINTS_ON_TAGGED = new ValueConfig<>(SCORING, "points-on-tagged");
    public static final AbstractConfig<Integer> SCORING_HUNTER_DISTANCE = new ValueConfig<>(SCORING, "hunter-distance");


    private static final AbstractConfig<ConfigurationSection> HUNTER = new ValueConfig<>(CONFIG, "hunter");
    public static final AbstractConfig<Integer> HUNTER_DEFAULT_OUTFIT_COLOR = new ValueConfig<>(HUNTER, "default-outfit-color");
    public static final AbstractConfig<Boolean> HUNTER_TEAMMATE_OUTLINE = new ValueConfig<>(HUNTER, "teammate-outline");
    public static final AbstractConfig<Boolean> HUNTER_HIDE_NAME_TAG = new ValueConfig<>(HUNTER, "hide-name-tag");
    public static final AbstractConfig<Boolean> HUNTER_OWN_TEAM_COLLISION = new ValueConfig<>(HUNTER, "own-team-collision");
    public static final AbstractConfig<Boolean> HUNTER_ALLOW_CLASS_CHANGE_ON_RESPAWN = new ValueConfig<>(HUNTER, "allow-class-change-on-respawn");

    private static final AbstractConfig<ConfigurationSection> RUNNER = new ValueConfig<>(CONFIG, "runner");
    public static final AbstractConfig<Integer> RUNNER_DEFAULT_OUTFIT_COLOR = new ValueConfig<>(RUNNER, "default-outfit-color");
    public static final AbstractConfig<Boolean> RUNNER_TEAMMATE_OUTLINE = new ValueConfig<>(RUNNER, "teammate-outline");
    public static final AbstractConfig<Boolean> RUNNER_HIDE_NAME_TAG = new ValueConfig<>(RUNNER, "hide-name-tag");
    public static final AbstractConfig<Boolean> RUNNER_OWN_TEAM_COLLISION = new ValueConfig<>(RUNNER, "own-team-collision");
    public static final AbstractConfig<Boolean> RUNNER_OTHER_TEAM_COLLISION = new ValueConfig<>(RUNNER, "other-team-collision");
    public static final AbstractConfig<Boolean> RUNNER_ALLOW_CLASS_CHANGE_ON_RESPAWN = new ValueConfig<>(RUNNER, "allow-class-change-on-respawn");


    public static final AbstractConfig<List<String>> RUNNER_CLASSES = new ListConfig<>(CLASSES_CONFIG, "runner-classes");
    public static final AbstractConfig<List<String>> HUNTER_CLASSES = new ListConfig<>(CLASSES_CONFIG, "hunter-classes");
    public static final AbstractConfig<String> DEFAULT_RUNNER_CLASS = new ValueConfig<>(CLASSES_CONFIG, "default-runner-class");
    public static final AbstractConfig<String> DEFAULT_HUNTER_CLASS = new ValueConfig<>(CLASSES_CONFIG, "default-hunter-class");
    public static final AbstractConfig<List<RoleClass>> CLASSES = new ListConfig<>(CLASSES_CONFIG, "classes");


    public static final AbstractConfig<Boolean> SOULBOUND_ITEMS = new ValueConfig<>(ITEMS_CONFIG, "soulbound-items");

    private static final AbstractConfig<ConfigurationSection> CUSTOM_ITEM = new ValueConfig<>(ITEMS_CONFIG, "custom-item");
    public static final AbstractConfig<ConfigurationSection> SPEED_BOOSTS = new ValueConfig<>(CUSTOM_ITEM, "speed-boosts");
    private static final AbstractConfig<ConfigurationSection> INVIS_POTION = new ValueConfig<>(CUSTOM_ITEM, "invis_potion");
    public static final AbstractConfig<Number> INVIS_POTION_DURATION = new ValueConfig<>(INVIS_POTION, "duration");
    public static final AbilityItemConfig BALLOON_GRENADE = new AbilityItemConfig(CUSTOM_ITEM, "balloon_grenade");
    public static final AbilityItemConfig HUNTER_COMPASS = new AbilityItemConfig(CUSTOM_ITEM, "hunter_compass");
    public static final AbilityItemConfig HUNTER_RADAR = new AbilityItemConfig(CUSTOM_ITEM, "hunter_radar");
    public static final AbilityItemConfig SPY_EYE = new AbilityItemConfig(CUSTOM_ITEM, "spy_eye");
    public static final AbstractConfig<Boolean> SPY_EYE_SELF_GLOW = new ValueConfig<>(SPY_EYE, "self-glow");

    private static final BouncyProjectileConfig DEFAULT_BOUNCY_PROJECTILE = new BouncyProjectileConfig(ITEMS_CONFIG, "default-bouncy-projectile", null);
    public static final BouncyProjectileConfig PYKES_HOOK = new BouncyProjectileConfig(CUSTOM_ITEM, "pykes_hook", DEFAULT_BOUNCY_PROJECTILE);
    public static final BouncyProjectileConfig SWAPPER_BALL = new BouncyProjectileConfig(CUSTOM_ITEM, "swapper_ball", DEFAULT_BOUNCY_PROJECTILE);
    public static final AbstractConfig<Boolean> SWAPPER_ALLOW_HUNTER_SWAP = new ValueConfig<>(SWAPPER_BALL, "allow-hunter-swap");
    public static final BouncyProjectileConfig SMOKE_GRENADE = new BouncyProjectileConfig(CUSTOM_ITEM, "smoke_grenade", DEFAULT_BOUNCY_PROJECTILE);
    public static final AbstractConfig<Integer> SMOKE_GRENADE_COLOR = new ValueConfig<>(SMOKE_GRENADE, "color");
    public static final AbstractConfig<Boolean> SMOKE_GRENADE_USE_TEAM_COLOR = new ValueConfig<>(SMOKE_GRENADE, "use-team-color");
    public static final BouncyProjectileConfig IMPULSE_GRENADE = new BouncyProjectileConfig(CUSTOM_ITEM, "impulse_grenade", DEFAULT_BOUNCY_PROJECTILE);
}
