package me.gimme.gimmetag.roleclass;

import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.gui.*;
import me.gimme.gimmetag.item.ItemManager;
import me.gimme.gimmetag.sfx.SoundEffects;
import me.gimme.gimmetag.tag.InventorySupplier;
import me.gimme.gimmetag.tag.Role;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ClassSelectionManager {

    private static final RoleClass EMPTY_RUNNER_CLASS = new RoleClass("-", null, null, null, new HashMap<>());
    private static final RoleClass EMPTY_HUNTER_CLASS = new RoleClass("-", null, null, null, new HashMap<>());

    private static final Material DEFAULT_RUNNER_ICON = Material.PLAYER_HEAD;
    private static final Material DEFAULT_HUNTER_ICON = Material.WITHER_SKELETON_SKULL;

    private final GUIView mainMenuView;

    private final InventoryGUI gui;
    private final InventorySupplier inventorySupplier;


    private final Map<Role, List<RoleClass>> classes = new HashMap<>();
    private final Map<Role, @Nullable RoleClass> defaultClassByRole = new HashMap<>();
    private final Map<UUID, Map<Role, RoleClass>> playerSelectedClasses = new HashMap<>();

    public ClassSelectionManager(@NotNull Plugin plugin, @NotNull ItemManager itemManager) {
        this.gui = new InventoryGUI(plugin);
        this.inventorySupplier = new InventorySupplier(itemManager);


        Map<String, RoleClass> roleClasses = Config.CLASSES.getValue().stream().collect(Collectors.toMap(RoleClass::getName, r -> r));

        int runnerHexColor = Config.RUNNER_DEFAULT_OUTFIT_COLOR.getValue();
        Color defaultRunnerOutfitColor = runnerHexColor < 0 ? null : Color.fromRGB(runnerHexColor);

        for (String roleClassName : Config.RUNNER_CLASSES.getValue()) {
            RoleClass roleClass = roleClasses.get(roleClassName.toLowerCase());
            if (roleClass == null) continue;
            if (roleClass.getIcon() == null) roleClass.setIcon(DEFAULT_RUNNER_ICON);
            if (roleClass.getColor() == null) roleClass.setColor(defaultRunnerOutfitColor);

            classes.computeIfAbsent(Role.RUNNER, k -> new ArrayList<>()).add(roleClass);
        }

        int hunterHexColor = Config.HUNTER_DEFAULT_OUTFIT_COLOR.getValue();
        Color defaultHunterOutfitColor = hunterHexColor < 0 ? null : Color.fromRGB(hunterHexColor);

        for (String roleClassName : Config.HUNTER_CLASSES.getValue()) {
            RoleClass roleClass = roleClasses.get(roleClassName);
            if (roleClass == null) continue;
            if (roleClass.getIcon() == null) roleClass.setIcon(DEFAULT_HUNTER_ICON);
            if (roleClass.getColor() == null) roleClass.setColor(defaultHunterOutfitColor);

            classes.computeIfAbsent(Role.HUNTER, k -> new ArrayList<>()).add(roleClass);
        }


        String defaultRunnerClassName = Config.DEFAULT_RUNNER_CLASS.getValue();
        Optional<RoleClass> defaultRunnerClass = classes.get(Role.RUNNER).stream()
                .filter(c -> c.getName().equalsIgnoreCase(defaultRunnerClassName))
                .findFirst();
        String defaultHunterClassName = Config.DEFAULT_HUNTER_CLASS.getValue();
        Optional<RoleClass> defaultHunterClass = classes.get(Role.HUNTER).stream()
                .filter(c -> c.getName().equalsIgnoreCase(defaultHunterClassName))
                .findFirst();

        defaultClassByRole.put(Role.RUNNER, defaultRunnerClass.orElse(EMPTY_RUNNER_CLASS));
        defaultClassByRole.put(Role.HUNTER, defaultHunterClass.orElse(EMPTY_HUNTER_CLASS));

        mainMenuView = new GUIViewBuilder()
                .setTitle("Class Selection")
                .addButtons(
                        new Button(Role.RUNNER.getDisplayName(), DEFAULT_RUNNER_ICON, Collections.singletonList(Role.RUNNER.getDisplayName() + " classes"),
                                player -> gui.open(createClassesView(Role.RUNNER, player), player)),
                        new Button(Role.HUNTER.getDisplayName(), DEFAULT_HUNTER_ICON, Collections.singletonList(Role.HUNTER.getDisplayName() + " classes"),
                                player -> gui.open(createClassesView(Role.HUNTER, player), player))
                ).build();
    }

    public void openClassSelectionMenu(@NotNull Player player) {
        gui.clearNavigationHistory(player);
        gui.open(mainMenuView, player);
    }

    @NotNull
    public RoleClass getRoleClassOrDefault(@NotNull Player player, @NotNull Role role) {
        RoleClass roleClass = getRoleClass(player, role);
        if (roleClass == null) roleClass = getDefaultRoleClass(role);
        return roleClass;
    }

    // Returns null if the player has not yet selected a role.
    @Nullable
    public RoleClass getRoleClass(@NotNull Player player, @NotNull Role role) {
        Map<Role, RoleClass> classByRole = playerSelectedClasses.get(player.getUniqueId());
        if (classByRole == null) return null;

        return classByRole.get(role);
    }

    @NotNull
    public RoleClass getDefaultRoleClass(@NotNull Role role) {
        return Objects.requireNonNull(defaultClassByRole.get(role));
    }

    private void selectClass(@NotNull Player player, @NotNull Role role, @NotNull RoleClass roleClass) {
        playerSelectedClasses.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(role, roleClass);

        String format = "" + ChatColor.GRAY + ChatColor.ITALIC;
        player.sendMessage(format + "Selected " + role.getDisplayName() + format + " class: " + roleClass.getDisplayName());
    }


    private GUIView createClassesView(@NotNull Role role, @NotNull Player player) {
        RoleClass selectedClass = getRoleClass(player, role);

        return new GUIViewBuilder()
                .setTitle(role.getDisplayName() + ": " + (selectedClass != null
                        ? selectedClass.getDisplayName()
                        : "Select a Class"))
                .addButton(gui.getBackButton())
                .addButton(ItemView.EMPTY)
                .addButtons(
                        classes.get(role).stream()
                                .map(c -> {
                                    assert c.getIcon() != null;
                                    return new Button(c.getDisplayName(), c.getIcon(),
                                            null, p -> gui.open(createRoleClassView(c, role, c != selectedClass), p));
                                })
                                .toArray(Button[]::new)
                )
                .build();
    }

    private GUIView createRoleClassView(@NotNull RoleClass roleClass, @NotNull Role role, boolean selectable) {
        String chooseButtonTitle = "Choose";
        Button chooseButton =
                selectable ?
                        new Button(chooseButtonTitle, Material.LIME_WOOL, null, player -> {
                            selectClass(player, role, roleClass);
                            openClassSelectionMenu(player);
                        }, SoundEffects.CLICK_ACCEPT)
                        : new Button(ChatColor.GRAY + chooseButtonTitle, Material.LIGHT_GRAY_WOOL, null, null);
        return new GUIViewBuilder()
                .setTitle(roleClass.getDisplayName() + ": Items")
                .addButtons(
                        chooseButton,
                        gui.getBackButton()
                )
                .setItemViews(inventorySupplier.getItems(roleClass.getItemMap()).toArray(new ItemStack[0]))
                .build();
    }
}
