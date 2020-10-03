package me.gimme.gimmetag.item;

import me.gimme.gimmecore.util.RomanNumerals;
import me.gimme.gimmetag.config.AbilityItemConfig;
import me.gimme.gimmetag.sfx.SoundEffect;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbilityItem extends CustomItem {

    private static final String USE_RESPONSE_MESSAGE_FORMAT = "" + ChatColor.RESET + ChatColor.YELLOW;

    private boolean consumable;
    private int cooldownTicks;
    private int durationTicks;
    private int level;

    @Nullable
    private String useResponseMessage = null;
    private boolean muted = false;
    private boolean showCooldown = false;
    private boolean showDuration = false;
    private boolean showLevel = false;

    public AbilityItem(@NotNull String name, @NotNull Material type, @NotNull AbilityItemConfig config) {
        super(name, type);

        init(config);
    }

    public AbilityItem(@NotNull String id, @NotNull String displayName, @NotNull Material type,
                       @NotNull AbilityItemConfig config) {
        super(id, displayName, type);

        init(config);
    }

    private void init(@NotNull AbilityItemConfig config) {
        this.consumable = config.isConsumable();
        setCooldown(config.getCooldown());
        setDuration(config.getDuration());
        this.level = config.getLevel();

        showCooldown(cooldownTicks > 0);
        showDuration(durationTicks > 0);
        showLevel(level > 0);
    }

    @Override
    @NotNull
    public ItemStack createItemStack(int amount) {
        ItemStack itemStack = super.createItemStack(amount);
        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

        if (showCooldown) {
            List<String> lore = itemMeta.getLore() != null ? itemMeta.getLore() : new ArrayList<>();
            lore.add(0, ChatColor.GRAY + getCooldownString() + " Cooldown");
            itemMeta.setLore(lore);
        }
        if (showLevel) setLevelInfo(itemMeta, level);
        if (showDuration) setDurationInfo(itemMeta, durationTicks);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public boolean use(@NotNull ItemStack itemStack, @NotNull Player user) {
        if (user.hasCooldown(itemStack.getType())) return false;
        if (!onUse(itemStack, user)) return false;

        if (consumable) itemStack.setAmount(itemStack.getAmount() - 1);
        if (cooldownTicks > 0) user.setCooldown(itemStack.getType(), cooldownTicks);
        if (useResponseMessage != null && !useResponseMessage.isEmpty())
            user.sendMessage(USE_RESPONSE_MESSAGE_FORMAT + useResponseMessage);
        if (!muted) SoundEffect.USE_EFFECT.play(user);

        return true;
    }

    protected abstract boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user);

    protected void setCooldown(double seconds) {
        this.cooldownTicks = Ticks.secondsToTicks(seconds);
    }

    protected double getCooldown() {
        return Ticks.ticksToSeconds(cooldownTicks);
    }

    protected int getCooldownTicks() {
        return cooldownTicks;
    }

    protected void setDuration(double seconds) {
        this.durationTicks = Ticks.secondsToTicks(seconds);
    }

    protected double getDuration() {
        return Ticks.ticksToSeconds(durationTicks);
    }

    protected int getDurationTicks() {
        return durationTicks;
    }

    protected int getLevel() {
        return level;
    }

    protected int getAmplifier() {
        return getLevel() - 1;
    }

    protected void setUseResponseMessage(@Nullable String message) {
        this.useResponseMessage = message;
    }

    protected void mute() {
        muted = true;
    }

    protected void showCooldown(boolean showCooldown) {
        this.showCooldown = showCooldown;
    }

    protected void showDuration(boolean showDuration) {
        this.showDuration = showDuration;
    }

    protected void showLevel(boolean showLevel) {
        this.showLevel = showLevel;
    }

    private void setLevelInfo(@NotNull ItemMeta itemMeta, int level) {
        itemMeta.setDisplayName(itemMeta.getDisplayName() + " " + RomanNumerals.toRoman(level));
    }

    private void setDurationInfo(@NotNull ItemMeta itemMeta, int durationTicks) {
        itemMeta.setDisplayName(itemMeta.getDisplayName() + getFormattedDuration(durationTicks));
    }

    private String getCooldownString() {
        return formatSeconds(cooldownTicks);
    }


    public static String getFormattedDuration(int durationTicks) {
        return "" + ChatColor.RESET + ChatColor.GRAY + " (" + formatSeconds(durationTicks) + ")" + ChatColor.RESET;
    }
}
