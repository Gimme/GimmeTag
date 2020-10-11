package me.gimme.gimmetag.roleclass;

import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.gui.*;
import me.gimme.gimmetag.item.ItemManager;
import me.gimme.gimmetag.tag.InventorySupplier;
import me.gimme.gimmetag.tag.Role;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClassSelectionManager {

    @NotNull
    public static final RoleClass EMPTY_HUNTER_CLASS = new RoleClass("", null, new HashMap<>(), null);

    private static final Material DEFAULT_RUNNER_ICON = Material.PLAYER_HEAD;
    private static final Material DEFAULT_HUNTER_ICON = Material.WITHER_SKELETON_SKULL;

    private final GUIView mainMenuView;
    private final GUIView runnerClassesView;
    private final GUIView hunterClassesView;

    private final InventoryGUI gui;
    private final InventorySupplier inventorySupplier;


    private final Map<@NotNull Role, @Nullable RoleClass> defaultClassByRole = new HashMap<>();
    private final Map<@NotNull UUID, Map<@NotNull Role, @NotNull RoleClass>> selectedClasses = new HashMap<>();

    public ClassSelectionManager(@NotNull Plugin plugin, @NotNull ItemManager itemManager) {
        this.gui = new InventoryGUI(plugin);
        this.inventorySupplier = new InventorySupplier(itemManager);

        String defaultRunnerClassName = Config.DEFAULT_RUNNER_CLASS.getValue();
        Optional<RoleClass> defaultRunnerClass = Config.RUNNER_CLASSES.getValue().stream()
                .filter(c -> ChatColor.stripColor(c.getName()).equalsIgnoreCase(ChatColor.stripColor(defaultRunnerClassName)))
                .findFirst();
        String defaultHunterClassName = Config.DEFAULT_HUNTER_CLASS.getValue();
        Optional<RoleClass> defaultHunterClass = Config.HUNTER_CLASSES.getValue().stream()
                .filter(c -> ChatColor.stripColor(c.getName()).equalsIgnoreCase(ChatColor.stripColor(defaultHunterClassName)))
                .findFirst();

        defaultClassByRole.put(Role.RUNNER, defaultRunnerClass.orElse(null));
        defaultClassByRole.put(Role.HUNTER, defaultHunterClass.orElse(EMPTY_HUNTER_CLASS));

        runnerClassesView = new GUIViewBuilder()
                .setTitle("Select a Runner Class")
                .addButton(gui.BACK_BUTTON)
                .addButton(ItemView.EMPTY)
                .addButtons(
                        Config.RUNNER_CLASSES.getValue().stream()
                                .map(c -> new Button(c.getName(), c.getIcon() != null ? c.getIcon() : DEFAULT_RUNNER_ICON,
                                        null, player -> gui.open(createRoleClassView(c, Role.RUNNER), player)))
                                .toArray(Button[]::new)
                )
                .build();

        hunterClassesView = new GUIViewBuilder()
                .setTitle("Select a Hunter Class")
                .addButton(gui.BACK_BUTTON)
                .addButton(ItemView.EMPTY)
                .addButtons(
                        Config.HUNTER_CLASSES.getValue().stream()
                                .map(c -> new Button(c.getName(), c.getIcon() != null ? c.getIcon() : DEFAULT_HUNTER_ICON,
                                        null, player -> gui.open(createRoleClassView(c, Role.HUNTER), player)))
                                .toArray(Button[]::new)
                )
                .build();

        mainMenuView = new GUIViewBuilder()
                .setTitle("Class Selection")
                .addButtons(
                        new Button("Runner", DEFAULT_RUNNER_ICON, Collections.singletonList("Go to runner class selection"),
                                player -> gui.open(runnerClassesView, player)),
                        new Button("Hunter", DEFAULT_HUNTER_ICON, Collections.singletonList("Go to hunter class selection"),
                                player -> gui.open(hunterClassesView, player))
                ).build();
    }

    public void openClassSelectionMenu(@NotNull Player player) {
        gui.clearNavigationHistory(player);
        gui.open(mainMenuView, player);
    }

    // Returns null only if no default runner role specified.
    @Nullable
    public RoleClass getRoleClass(@NotNull Player player, @NotNull Role role) {
        RoleClass roleClass = defaultClassByRole.get(role);

        Map<Role, RoleClass> classByRole = selectedClasses.get(player.getUniqueId());
        if (classByRole != null) {
            RoleClass selectedRoleClass = classByRole.get(role);
            if (selectedRoleClass != null) roleClass = selectedRoleClass;
        }

        return roleClass;
    }

    private void selectClass(@NotNull Player player, @NotNull Role role, @NotNull RoleClass roleClass) {
        selectedClasses.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(role, roleClass);
    }

    private GUIView createRoleClassView(@NotNull RoleClass roleClass, @NotNull Role role) {
        return new GUIViewBuilder()
                .setTitle("Inventory")
                .addButtons(
                        new Button("Choose", Material.LIME_WOOL, null, player -> {
                            selectClass(player, role, roleClass);
                            openClassSelectionMenu(player);
                        }),
                        gui.BACK_BUTTON
                )
                .setItemViews(inventorySupplier.getItems(roleClass.getItemMap()).toArray(new ItemStack[0]))
                .build();
    }
}
