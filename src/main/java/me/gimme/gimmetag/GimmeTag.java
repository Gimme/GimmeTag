package me.gimme.gimmetag;

import me.gimme.gimmecore.command.CommandManager;
import me.gimme.gimmecore.util.ConfigUtils;
import me.gimme.gimmetag.command.ArgPlaceholder;
import me.gimme.gimmetag.command.commands.*;
import me.gimme.gimmetag.config.AbstractConfig;
import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.config.type.AbilityItemConfig;
import me.gimme.gimmetag.config.type.BouncyProjectileConfig;
import me.gimme.gimmetag.config.type.ValueConfig;
import me.gimme.gimmetag.extension.*;
import me.gimme.gimmetag.gamerule.DisableArrowDamage;
import me.gimme.gimmetag.gamerule.DisableHunger;
import me.gimme.gimmetag.gamerule.EnableProjectileKnockback;
import me.gimme.gimmetag.item.CustomItem;
import me.gimme.gimmetag.item.ItemManager;
import me.gimme.gimmetag.item.items.*;
import me.gimme.gimmetag.roleclass.ClassSelectionManager;
import me.gimme.gimmetag.roleclass.RoleClass;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public final class GimmeTag extends JavaPlugin {

    public static final String TAG_COMMAND = "tag";
    public static final String PERMISSIONS_PATH = "gimmetag";
    public static final String DEV_PERMISSIONS_PATH = PERMISSIONS_PATH + ".dev";

    private static final String PROTOCOL_LIB_NAME = "ProtocolLib";

    private static final String CLASSES_CONFIG_PATH = "classes.yml";
    private static final String ITEMS_CONFIG_PATH = "items.yml";

    private YamlConfiguration classesConfig;
    private YamlConfiguration itemsConfig;

    private CommandManager commandManager;
    private ItemManager itemManager;
    private TagManager tagManager;
    private ClassSelectionManager classSelectionManager;

    @NotNull
    public TagManager getTagManager() {
        return tagManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        ConfigurationSerialization.registerClass(RoleClass.class);

        init();
    }

    private void init() {
        saveDefaultConfig();
        ConfigUtils.saveDefaultConfig(this, CLASSES_CONFIG_PATH);
        ConfigUtils.saveDefaultConfig(this, ITEMS_CONFIG_PATH);

        commandManager = new CommandManager(this);
        itemManager = new ItemManager(this);
        classSelectionManager = new ClassSelectionManager(this, itemManager);
        tagManager = new TagManager(this, itemManager, classSelectionManager);

        registerCommands();
        registerEvents();
        registerCustomItems();

        getLogger().info("Loaded!");
    }

    @Override
    public void onDisable() {
        tagManager.onDisable();
        itemManager.onDisable();
    }

    public void reload() {
        getLogger().info("Reloading...");

        onDisable();
        getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        reloadConfig();
        classesConfig = ConfigUtils.reloadConfig(this, CLASSES_CONFIG_PATH);
        itemsConfig = ConfigUtils.reloadConfig(this, ITEMS_CONFIG_PATH);

        init();
    }

    private void registerCommands() {
        commandManager.registerPlaceholder(ArgPlaceholder.ONLINE_PLAYERS, () -> getServer().getOnlinePlayers(), Player::getName);
        commandManager.registerPlaceholder(ArgPlaceholder.ITEM_IDS, () -> itemManager.getCustomItems().values(), CustomItem::getId);

        commandManager.registerBasicHelpCommand(TAG_COMMAND);
        registerCommand(new StartCommand(tagManager));
        registerCommand(new StopCommand(tagManager));
        registerCommand(new HunterCommand(tagManager));
        registerCommand(new RunnerCommand(tagManager));
        registerCommand(new SuicideCommand());
        registerCommand(new GiveCommand(getServer(), itemManager));
        registerCommand(new ClassCommand(classSelectionManager));
        registerCommand(new ReloadCommand(this));
        registerCommand(new TestCommand());
    }

    private void registerEvents() {
        registerEvents(tagManager);
        if (Config.DISABLE_HUNGER.getValue()) registerEvents(new DisableHunger(() -> tagManager.isActiveRound()));
        if (Config.DISABLE_ARROW_DAMAGE.getValue())
            registerEvents(new DisableArrowDamage(() -> tagManager.isActiveRound()));
        registerEvents(new EnableProjectileKnockback(() -> tagManager.isActiveRound()));
        registerEvents(new SleepProgressbar(this, tagManager));
        registerEvents(new EventEffects(getServer()));
        registerEvents(new ResultsDisplay(getServer()));
        if (getServer().getPluginManager().getPlugin(PROTOCOL_LIB_NAME) != null)
            registerEvents(new TeamOutline(this, tagManager));
        else getLogger().warning(PROTOCOL_LIB_NAME + " is needed to show team outlines.");
        registerEvents(new SoulboundItems());
        registerEvents(new SleepStun(tagManager));
        registerEvents(new BindingCurse());
    }

    private void registerCustomItems() {
        registerCustomItem(Config.SPEED_BOOST, (id, c) -> new SpeedBoost(id, new AbilityItemConfig(c)));
        registerCustomItem(Config.SWAPPER_BALL, (id, c) -> new SwapperBall(
                id,
                new BouncyProjectileConfig(c, Config.DEFAULT_BOUNCY_PROJECTILE),
                Config.SWAPPER_ALLOW_HUNTER_SWAP.getValue(c),
                this,
                tagManager
        ));
        itemManager.registerItem(new HunterBow());
        registerCustomItem(Config.INVIS_POTION, (id, c) -> new InvisPotion(id, Config.INVIS_POTION_DURATION.getValue(c).doubleValue()));
        registerCustomItem(Config.BALLOON_GRENADE, (id, c) -> new BalloonGrenade(id, new AbilityItemConfig(c)));
        registerCustomItem(Config.HUNTER_COMPASS, (id, c) -> new HunterCompass(id, new AbilityItemConfig(c), tagManager));
        registerCustomItem(Config.HUNTER_RADAR, (id, c) -> new HunterRadar(id, new AbilityItemConfig(c), tagManager));
        registerCustomItem(Config.SPY_EYE, (id, c) -> new SpyEye(id, new AbilityItemConfig(c), Config.SPY_EYE_SELF_GLOW.getValue(c), this));
        registerCustomItem(Config.SMOKE_GRENADE, (id, c) -> new SmokeGrenade(
                id,
                new BouncyProjectileConfig(c, Config.DEFAULT_BOUNCY_PROJECTILE),
                Config.SMOKE_GRENADE_COLOR.getValue(c),
                Config.SMOKE_GRENADE_USE_TEAM_COLOR.getValue(c),
                this
        ));
        registerCustomItem(Config.IMPULSE_GRENADE, (id, c) -> new ImpulseGrenade(id, new BouncyProjectileConfig(c, Config.DEFAULT_BOUNCY_PROJECTILE), this));
        registerCustomItem(Config.COOKED_EGG, (id, c) -> new CookedEgg(id, new BouncyProjectileConfig(c, Config.DEFAULT_BOUNCY_PROJECTILE), this));
        registerCustomItem(Config.PYKES_HOOK, (id, c) -> new PykesHook(id, new BouncyProjectileConfig(c, Config.DEFAULT_BOUNCY_PROJECTILE), this));
    }

    private void registerCommand(me.gimme.gimmecore.command.BaseCommand command) {
        commandManager.register(command);
    }

    private void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerCustomItem(@NotNull AbstractConfig<ConfigurationSection> config,
                                    @NotNull BiFunction<@NotNull String, @NotNull ConfigurationSection, @NotNull CustomItem> function) {
        String path = config.getPath();

        if (config.getValue() != null) {
            CustomItem customItem = function.apply(path, config.getValue());
            itemManager.registerItem(customItem);
        }

        AbstractConfig<ConfigurationSection> multi = new ValueConfig<>(config.getParent(), "_" + path);
        ConfigurationSection multiSection = multi.getValue();
        if (multiSection != null) {
            for (String id : multiSection.getKeys(false)) {
                CustomItem customItem = function.apply(id, multiSection.getConfigurationSection(id));
                itemManager.registerItem(customItem);
            }
        }
    }


    private static GimmeTag instance;

    @NotNull
    public static GimmeTag getInstance() {
        if (instance == null) throw new IllegalStateException("Plugin has not been enabled yet");
        return instance;
    }

    @NotNull
    public YamlConfiguration getClassesConfig() {
        if (classesConfig == null) classesConfig = ConfigUtils.reloadConfig(this, CLASSES_CONFIG_PATH);
        return classesConfig;
    }

    @NotNull
    public YamlConfiguration getItemsConfig() {
        if (itemsConfig == null) itemsConfig = ConfigUtils.reloadConfig(this, ITEMS_CONFIG_PATH);
        return itemsConfig;
    }
}
