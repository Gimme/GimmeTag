package me.gimme.gimmetag.item;

import me.gimme.gimmecore.util.RomanNumerals;
import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.config.type.AbilityItemConfig;
import me.gimme.gimmetag.item.task.RechargeTask;
import me.gimme.gimmetag.sfx.SoundEffect;
import me.gimme.gimmetag.sfx.SoundEffects;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a custom item that can be used to create item stacks with abilities tied to them.
 * <p>
 * The ability of an item stack is used by right clicking with it.
 */
public abstract class AbilityItem extends CustomItem {

    private static final String USE_RESPONSE_MESSAGE_FORMAT = "" + ChatColor.RESET + ChatColor.YELLOW;

    private boolean consumable;
    private int cooldownTicks;
    private int rechargeTimeTicks;
    private int durationTicks;
    private int level;

    @NotNull
    private UseEvent useEvent = UseEvent.INTERACT;
    @Nullable
    private String useResponseMessage;
    @Nullable
    private SoundEffect useSound;
    private boolean showCooldown;
    private boolean showRechargeTime;
    private boolean showDuration;
    private boolean showLevel;

    private final Map<@NotNull UUID, @NotNull RechargeTask> rechargeTasks = new HashMap<>();

    /**
     * Creates a new ability item with the specified name, item type and configuration.
     *
     * @param name   a unique name and the display name of this item. The name can contain ChatColors and spaces, which
     *               will be stripped from the name and only be used for the display name.
     * @param type   the item type of the generated item stacks
     * @param config a config containing values to be applied to this ability item's variables
     */
    public AbilityItem(@NotNull String name, @NotNull Material type, @NotNull AbilityItemConfig config) {
        super(name, type);

        init(config);
    }

    /**
     * Creates a new ability item with the specified name, display name, item type and configuration.
     *
     * @param id          a unique name for this item
     * @param displayName the display name of this item
     * @param type        the item type of the generated item stacks
     * @param config      a config containing values to be applied to this ability item's variables
     */
    public AbilityItem(@NotNull String id, @NotNull String displayName, @NotNull Material type,
                       @NotNull AbilityItemConfig config) {
        super(id, displayName, type);

        init(config);
    }

    /**
     * Initializes this item's variables based on the given configuration.
     *
     * @param config the configuration to get the values from
     */
    private void init(@NotNull AbilityItemConfig config) {
        this.consumable = config.isConsumable();
        setCooldown(config.getCooldown());
        setRechargeTime(config.getRechargeTime());
        this.durationTicks = Ticks.secondsToTicks(config.getDuration());
        this.level = config.getLevel();

        this.useSound = SoundEffects.USE_EFFECT;
        showCooldown(cooldownTicks > 0);
        showRechargeTime(rechargeTimeTicks > 0);
        showDuration(durationTicks > 0);
        showLevel(level > 0);
    }

    @Override
    @NotNull
    public ItemStack createItemStack(int amount) {
        ItemStack itemStack = super.createItemStack(amount);
        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

        if (showLevel) setLevelInfo(itemMeta, level);
        if (showDuration) setDurationInfo(itemMeta, durationTicks);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Updates the lore of the given item stack with the specified header, the item info and the specified footer.
     * <p>
     * This item's cooldown is displayed at the top.
     *
     * @param itemStack the item stack to modify the lore of
     * @param header    the header to be placed at the top of the lore
     * @param footer    the footer to be placed at the bottom of the lore
     */
    @Override
    protected void updateLore(@NotNull ItemStack itemStack, @NotNull List<String> header, @NotNull List<String> footer) {
        if (showRechargeTime) header.add(0, ChatColor.GRAY + formatSeconds(rechargeTimeTicks) + " Recharge");
        if (showCooldown) header.add(0, ChatColor.GRAY + formatSeconds(cooldownTicks) + " Cooldown");
        super.updateLore(itemStack, header, footer);
    }

    /**
     * Attempts to use this item's ability and returns if it was successful or not.
     * <p>
     * A use can be unsuccessful if the item is on cooldown or the implemented item's ability failed internally.
     *
     * @param itemStack the item stack that was used to activate its ability
     * @param user      the player that used the item stack
     * @return if the use was successful
     */
    boolean use(@NotNull ItemStack itemStack, @NotNull Player user) {
        if (user.hasCooldown(itemStack.getType())) return false;
        if (!onUse(itemStack, user)) return false;

        applyCooldown(itemStack, user);
        if (consumable) itemStack.setAmount(itemStack.getAmount() - 1);

        if (useResponseMessage != null && !useResponseMessage.isEmpty())
            user.sendMessage(USE_RESPONSE_MESSAGE_FORMAT + useResponseMessage);
        if (useSound != null) useSound.playAt(user);

        return true;
    }

    /**
     * Performs the ability of this item and returns if it was successful or not.
     * <p>
     * A use can be unsuccessful for any reason defined in the specific implementation.
     *
     * @param itemStack the item stack that was used to activate the ability
     * @param user      the player that used the item stack
     * @return if the use was successful
     */
    protected abstract boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user);

    /**
     * Applies cooldown, or recharge time, to the given item after being used by the specified player.
     *
     * @param itemStack the used item stack
     * @param user      the user of the item
     */
    private void applyCooldown(@NotNull ItemStack itemStack, @NotNull Player user) {
        UUID uuid = user.getUniqueId();

        RechargeTask rechargeTask = rechargeTasks.get(uuid);
        if (isRechargeable()) {
            if (rechargeTask != null && !rechargeTask.isCancelled()) {
                rechargeTask.incrementRecharges();
            } else {
                rechargeTask = new RechargeTask(GimmeTag.getInstance(), itemStack, getRechargeTime()).start();
                rechargeTasks.put(uuid, rechargeTask);
            }

            int amount = itemStack.getAmount();
            if (amount == 1) {
                // Keep the final charge in inventory to indicate that it is recharging
                itemStack.setAmount(amount + 1);
                rechargeTask.setFinalCharge();
            }
        }

        int cd = cooldownTicks;
        if (rechargeTask != null && rechargeTask.isFinalCharge())
            cd = Math.max(cd, rechargeTask.getTicksUntilRecharge());
        if (cd > 0) user.setCooldown(itemStack.getType(), cd);
    }

    void setCooldown(double seconds) {
        this.cooldownTicks = Ticks.secondsToTicks(seconds);
    }

    protected double getCooldown() {
        return Ticks.ticksToSeconds(cooldownTicks);
    }

    protected int getCooldownTicks() {
        return cooldownTicks;
    }

    private void setRechargeTime(double seconds) {
        this.rechargeTimeTicks = Ticks.secondsToTicks(seconds);
    }

    private double getRechargeTime() {
        return Ticks.ticksToSeconds(rechargeTimeTicks);
    }

    protected int getRechargeTimeTicks() {
        return rechargeTimeTicks;
    }

    protected double getDuration() {
        return Ticks.ticksToSeconds(durationTicks);
    }

    protected int getDurationTicks() {
        return durationTicks;
    }

    /**
     * Returns an amplifier based on this item's level, which is useful when applying potion effects.
     * <p>
     * The amplifier is always 1 less than the level. For example, the potion effect "Speed I" has amplifier 0, "Speed
     * II" has amplifier 1, and so on.
     *
     * @return the amplifier, used for potion effects
     */
    protected int getAmplifier() {
        return level - 1;
    }

    /**
     * Sets the event type that should trigger the use of this ability item. {@link UseEvent#SHOOT_BOW} can only be used
     * if the item type is a bow.
     *
     * @param useEvent the event type to trigger this item's use
     */
    protected void setUseEvent(@NotNull UseEvent useEvent) {
        this.useEvent = useEvent;
    }

    /**
     * Returns the event type that will trigger the use of this ability item.
     *
     * @return the event type that will trigger this item's use
     */
    @NotNull
    UseEvent getUseEvent() {
        return useEvent;
    }

    /**
     * Sets the message that is sent to the player after using this item's ability. The message can be null to not send
     * any message.
     *
     * @param message the message to send to the player after each use, or null for no message
     */
    protected void setUseResponseMessage(@Nullable String message) {
        this.useResponseMessage = message;
    }

    /**
     * Sets the sound that is played at the player's location after using this item's ability. The sound can be null to
     * not play any sound.
     *
     * @param useSound the sound to play at the player's location after each use, or null for no sound
     */
    protected void setUseSound(@Nullable SoundEffect useSound) {
        this.useSound = useSound;
    }

    /**
     * Mutes the default sound effect that is played after using this item's ability.
     */
    protected void mute() {
        setUseSound(null);
    }

    /**
     * Sets if the cooldown of this item should be displayed in the lore.
     *
     * @param showCooldown if the cooldown should be displayed in the lore
     */
    void showCooldown(boolean showCooldown) {
        this.showCooldown = showCooldown;
    }

    /**
     * Sets if the recharge cooldown of this item should be displayed in the lore.
     *
     * @param showRechargeTime if the recharge cooldown should be displayed in the lore
     */
    private void showRechargeTime(boolean showRechargeTime) {
        this.showRechargeTime = showRechargeTime;
    }

    /**
     * Sets if the duration of this item should be displayed next to the display name.
     *
     * @param showDuration if the duration should be displayed next to the display name
     */
    void showDuration(boolean showDuration) {
        this.showDuration = showDuration;
    }

    /**
     * Sets if the level of this item should be displayed next to the display name.
     *
     * @param showLevel if the level should be displayed next to the display name
     */
    protected void showLevel(boolean showLevel) {
        this.showLevel = showLevel;
    }

    /**
     * Returns if this item is consumable and will recharge consumptions after a delay.
     *
     * @return if this item is consumable and will recharge consumptions after a delay
     */
    private boolean isRechargeable() {
        return consumable && rechargeTimeTicks > 0;
    }

    /**
     * Cleans up all active tasks for this item.
     */
    public void onDisable() {
        rechargeTasks.values().forEach(RechargeTask::rechargeAll);
    }


    /**
     * Appends the specified duration information to the display name of the given item meta.
     *
     * @param itemMeta      the item meta whose display name to edit
     * @param durationTicks the duration in ticks
     */
    private static void setDurationInfo(@NotNull ItemMeta itemMeta, int durationTicks) {
        itemMeta.setDisplayName(itemMeta.getDisplayName() + getFormattedDuration(durationTicks));
    }

    /**
     * Appends the specified level to the display name of the given item meta.
     *
     * @param itemMeta the item meta whose display name to edit
     * @param level    the level to append to the display name
     */
    private static void setLevelInfo(@NotNull ItemMeta itemMeta, int level) {
        itemMeta.setDisplayName(itemMeta.getDisplayName() + " " + RomanNumerals.toRoman(level));
    }

    /**
     * Converts the specified ticks into seconds and returns it in a string formatted according to the standard way to
     * display durations.
     *
     * @param durationTicks the duration in ticks to be converted into seconds
     * @return a formatted string showing the specified duration
     */
    public static String getFormattedDuration(int durationTicks) {
        return "" + ChatColor.RESET + ChatColor.GRAY + " (" + formatSeconds(durationTicks) + ")" + ChatColor.RESET;
    }


    protected enum UseEvent {
        INTERACT,
        SHOOT_BOW
    }
}
