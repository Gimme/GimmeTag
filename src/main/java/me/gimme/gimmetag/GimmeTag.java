package me.gimme.gimmetag;

import me.gimme.gimmecore.command.CommandManager;
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
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class GimmeTag extends JavaPlugin {

    public static final String PERMISSIONS_PATH = "gimmetag";
    public static final String TAG_COMMAND = "tag";

    private static final String PROTOCOL_LIB_NAME = "ProtocolLib";

    private CommandManager commandManager;
    private ItemManager itemManager;
    private TagManager tagManager;

    @NotNull
    public TagManager getTagManager() {
        return tagManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        commandManager = new CommandManager(this);
        itemManager = new ItemManager(this);
        tagManager = new TagManager(this, itemManager);

        registerCommands();
        registerEvents();
        registerCustomItems();
    }

    @Override
    public void onDisable() {
        tagManager.onDisable();
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
        registerCommand(new NoteCommand());
        registerCommand(new TestCommand());
    }

    private void registerEvents() {
        registerEvents(tagManager);
        if (Config.DISABLE_HUNGER.getValue()) registerEvents(new DisableHunger(() -> tagManager.isActiveRound()));
        if (Config.DISABLE_ARROW_DAMAGE.getValue()) registerEvents(new DisableArrowDamage(() -> tagManager.isActiveRound()));
        registerEvents(new EnableProjectileKnockback(() -> tagManager.isActiveRound()));
        registerEvents(new SleepProgressbar(this, tagManager));
        registerEvents(new EventEffects(getServer()));
        registerEvents(new ResultsDisplay(getServer()));
        if (getServer().getPluginManager().getPlugin(PROTOCOL_LIB_NAME) != null)
            registerEvents(new TeamOutline(this, tagManager));
        else getLogger().warning(PROTOCOL_LIB_NAME + " is needed to show team outlines.");
        registerEvents(new SoulboundItems());
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
        itemManager.registerItem(new SmokeGrenade(
                Config.SMOKE_GRENADE,
                Config.SMOKE_GRENADE_COLOR.getValue(),
                Config.SMOKE_GRENADE_USE_TEAM_COLOR.getValue(),
                this
        ));
        itemManager.registerItem(new ImpulseGrenade(Config.IMPULSE_GRENADE, this));
    }

    private void registerCommand(me.gimme.gimmecore.command.BaseCommand command) {
        commandManager.register(command);
    }

    private void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }


    private static GimmeTag instance;
    public static GimmeTag getPlugin() {
        if (instance == null) throw new IllegalStateException("Plugin has not been initialized yet");
        return instance;
    }
}
