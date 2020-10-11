package me.gimme.gimmetag.utils.outline;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An outline effect based on a collection of targets to display the outline on.
 */
public class CollectionOutlineEffect extends OutlineEffect {

    @Nullable
    private Collection<? extends Entity> targets;
    private final Map<@NotNull Integer, @NotNull Entity> activeOutlines = new HashMap<>(); // Current outlines since last refresh

    /**
     * Creates an outline effect based on the contents of a collection with targets.
     * <p>
     * After calling {@link this#refresh()}, each entity in the target collection will have an outline effect displayed
     * for the given pov entity. Both {@link this#show()} and {@link this#hide()} will execute a refresh automatically.
     * <p>
     * The collection of targets can be updated from outside or {@link this#setTargets(Collection)} can be used to
     * update it manually.
     *
     * @param plugin    the plugin to register the effect with
     * @param povEntity the entity that will be the only one to see this outline effect
     * @param targets   the target entities to display outlines of, or null for no targets
     */
    public CollectionOutlineEffect(@NotNull Plugin plugin, @NotNull Entity povEntity, @Nullable Collection<? extends Entity> targets) {
        super(plugin, null);

        setOutlineCondition((p, entityId) -> povEntity.getUniqueId().equals(p.getUniqueId()) && activeOutlines.containsKey(entityId));
        setTargets(targets);
    }

    /**
     * Refreshes the outline status of the target entities. This needs to be called every time the contents of the
     * target collection changes for it to take effect.
     */
    public void refresh() {
        List<Entity> newTargets = new ArrayList<>();
        Map<@NotNull Integer, @NotNull Entity> removedTargets = new HashMap<>(activeOutlines);

        if (targets != null) {
            for (Entity target : targets) {
                int targetId = target.getEntityId();

                if (!activeOutlines.containsKey(targetId)) {
                    newTargets.add(target);
                    activeOutlines.put(targetId, target);
                } else {
                    removedTargets.remove(targetId);
                }
            }
        }
        activeOutlines.keySet().removeAll(removedTargets.keySet());

        OutlineEffect.refresh(newTargets);
        OutlineEffect.refresh(removedTargets.values());
    }

    /**
     * Sets the target entities to show an outline of, or null for no targets.
     *
     * @param targets the target entities to show an outline of, or null for no targets
     */
    public void setTargets(@Nullable Collection<? extends Entity> targets) {
        this.targets = targets;
    }

    @Override
    public boolean show() {
        boolean b = super.show();
        refresh();
        return b;
    }

    @Override
    public boolean hide() {
        boolean b = super.hide();
        refresh(activeOutlines.values());
        return b;
    }
}
