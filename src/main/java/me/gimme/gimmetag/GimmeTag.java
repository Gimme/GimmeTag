package me.gimme.gimmetag;

import me.gimme.gimmecore.command.CommandManager;
import me.gimme.gimmecore.util.ConfigUtils;
import me.gimme.gimmetag.command.ArgPlaceholder;
import me.gimme.gimmetag.command.commands.*;
import me.gimme.gimmetag.config.AbilityItemConfig;
import me.gimme.gimmetag.config.Config;
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
        classesConfig = ConfigUtils.getYamlConfig(this, CLASSES_CONFIG_PATH);
        itemsConfig = ConfigUtils.getYamlConfig(this, ITEMS_CONFIG_PATH);

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
    }

    public void reload() {
        getLogger().info("Reloading...");

        onDisable();
        getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        reloadConfig();
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
        ConfigurationSection speedBoostSection = Config.SPEED_BOOSTS.getValue();
        for (String speedBoostId : speedBoostSection.getKeys(false)) {
            itemManager.registerItem(new SpeedBoost(
                    speedBoostId,
                    new AbilityItemConfig(speedBoostSection, speedBoostId)
            ));
        }

        itemManager.registerItem(new SwapperBall(
                Config.SWAPPER_BALL,
                Config.SWAPPER_ALLOW_HUNTER_SWAP.getValue(),
                this,
                tagManager
        ));
        itemManager.registerItem(new HunterBow());
        itemManager.registerItem(new InvisPotion(Config.INVIS_POTION_DURATION.getValue().doubleValue()));
        itemManager.registerItem(new BalloonGrenade(Config.BALLOON_GRENADE));
        itemManager.registerItem(new HunterCompass(Config.HUNTER_COMPASS, tagManager));
        itemManager.registerItem(new HunterRadar(Config.HUNTER_RADAR, tagManager));
        itemManager.registerItem(new SpyEye(Config.SPY_EYE, Config.SPY_EYE_SELF_GLOW.getValue(), this));
        itemManager.registerItem(new SmokeGrenade(
                Config.SMOKE_GRENADE,
                Config.SMOKE_GRENADE_COLOR.getValue(),
                Config.SMOKE_GRENADE_USE_TEAM_COLOR.getValue(),
                this
        ));
        itemManager.registerItem(new ImpulseGrenade(Config.IMPULSE_GRENADE, this));
        itemManager.registerItem(new PykesHook(
                Config.PYKES_HOOK,
                this
        ));
    }

    private void registerCommand(me.gimme.gimmecore.command.BaseCommand command) {
        commandManager.register(command);
    }

    private void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }


    private static GimmeTag instance;

    @NotNull
    public static GimmeTag getInstance() {
        if (instance == null) throw new IllegalStateException("Plugin has not been enabled yet");
        return instance;
    }

    @NotNull
    public YamlConfiguration getClassesConfig() {
        return classesConfig;
    }

    @NotNull
    public YamlConfiguration getItemsConfig() {
        return itemsConfig;
    }
}
