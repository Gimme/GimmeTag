package me.gimme.gimmetag.item;

import me.gimme.gimmetag.sfx.SoundEffect;
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

    private int cooldownTicks;
    private boolean consumable;
    @Nullable
    private String useResponseMessage = null;
    private boolean muted = false;
    private boolean hideCooldown = false;
    private boolean showDuration = false;
    private int durationTicks = 0;

    public AbilityItem(@NotNull String name, @NotNull Material type, double cooldown, boolean consumable) {
        super(name, type);

        setCooldown(cooldown);
        this.consumable = consumable;
    }

    public AbilityItem(@NotNull String id, @NotNull String displayName, @NotNull Material type, double cooldown,
                       boolean consumable) {
        super(id, displayName, type);

        setCooldown(cooldown);
        this.consumable = consumable;
    }

    @Override
    @NotNull
    public ItemStack createItemStack(int amount) {
        ItemStack itemStack = super.createItemStack(amount);
        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

        if (cooldownTicks > 0 && !hideCooldown) {
            List<String> lore = itemMeta.getLore() != null ? itemMeta.getLore() : new ArrayList<>();
            lore.add(0, ChatColor.GRAY + getCooldownString() + " Cooldown");
            itemMeta.setLore(lore);
        }

        if (showDuration) setDurationInfo(itemMeta, durationTicks);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public void use(@NotNull ItemStack itemStack, @NotNull Player user) {
        if (user.hasCooldown(itemStack.getType())) return;
        if (!onUse(itemStack, user)) return;

        if (consumable) itemStack.setAmount(itemStack.getAmount() - 1);
        if (cooldownTicks > 0) user.setCooldown(itemStack.getType(), cooldownTicks);
        if (useResponseMessage != null && !useResponseMessage.isEmpty())
            user.sendMessage(USE_RESPONSE_MESSAGE_FORMAT + useResponseMessage);
        if (!muted) SoundEffect.USE_EFFECT.play(user);
    }

    protected abstract boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user);

    protected void setCooldown(double seconds) {
        this.cooldownTicks = (int) Math.round(seconds * 20);
    }

    protected void setUseResponseMessage(@Nullable String message) {
        this.useResponseMessage = message;
    }

    protected void mute() {
        muted = true;
    }

    protected void hideCooldown() {
        hideCooldown = true;
    }

    protected void showDuration(int durationTicks) {
        this.showDuration = true;
        this.durationTicks = durationTicks;
    }

    private void setDurationInfo(@NotNull ItemMeta itemMeta, int durationTicks) {
        itemMeta.setDisplayName(itemMeta.getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " (" + formatSeconds(durationTicks) + ")");
    }

    private String getCooldownString() {
        return formatSeconds(cooldownTicks);
    }
}
