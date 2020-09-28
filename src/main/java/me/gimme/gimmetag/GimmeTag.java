package me.gimme.gimmetag;

import me.gimme.gimmecore.command.CommandManager;
import me.gimme.gimmetag.command.commands.*;
import me.gimme.gimmetag.config.AbilityItemConfig;
import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.gamerule.DisableHunger;
import me.gimme.gimmetag.gamerule.EnableProjectileKnockback;
import me.gimme.gimmetag.item.ItemManager;
import me.gimme.gimmetag.item.items.*;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class GimmeTag extends JavaPlugin {

    public static final String PERMISSIONS_PATH = "gimmetag";
    public static final String TAG_COMMAND = "tag";

    private CommandManager commandManager;
    private ItemManager itemManager;
    private TagManager tagManager;

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
        commandManager.registerBasicHelpCommand(TAG_COMMAND);
        registerCommand(new StartCommand(tagManager));
        registerCommand(new StopCommand(tagManager));
        registerCommand(new HunterCommand(tagManager));
        registerCommand(new RunnerCommand(tagManager));
        registerCommand(new SuicideCommand());
        registerCommand(new NoteCommand());
    }

    private void registerEvents() {
        registerEvents(tagManager);
        if (Config.DISABLE_HUNGER.getValue()) registerEvents(new DisableHunger(() -> tagManager.isActiveRound()));
        registerEvents(new EnableProjectileKnockback(() -> tagManager.isActiveRound()));
    }

    private void registerCustomItems() {
        ConfigurationSection speedBoostSection = Config.SPEED_BOOSTS.getValue();
        for (String speedBoostId : speedBoostSection.getKeys(false)) {
            AbilityItemConfig itemConfig = new AbilityItemConfig(speedBoostSection, speedBoostId);
            itemManager.registerItem(new SpeedBoost(
                    speedBoostId,
                    itemConfig.getCooldown().doubleValue(),
                    itemConfig.isConsumable(),
                    itemConfig.getDuration().doubleValue(),
                    itemConfig.getLevel()
            ));
        }
        itemManager.registerItem(new HunterCompass("hunter_compass", this));
        itemManager.registerItem(new SwapperBall(
                Config.SWAPPER_BALL.getCooldown().doubleValue(),
                Config.SWAPPER_BALL.isConsumable(),
                Config.SWAPPER_ALLOW_HUNTER_SWAP.getValue(),
                this,
                tagManager
        ));
        itemManager.registerItem(new HunterBow());
        itemManager.registerItem(new InvisPotion(Config.INVIS_POTION_DURATION.getValue().doubleValue()));
        itemManager.registerItem(new BalloonGrenade(
                Config.BALLOON_GRENADE.getCooldown().doubleValue(),
                Config.BALLOON_GRENADE.isConsumable(),
                Config.BALLOON_GRENADE.getDuration().doubleValue(),
                Config.BALLOON_GRENADE.getLevel()
        ));
    }

    private void registerCommand(me.gimme.gimmecore.command.BaseCommand command) {
        commandManager.register(command);
    }

    private void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }


    private static GimmeTag instance;

    public static GimmeTag getPlugin() {
        return instance;
    }
}
