package io.github.lijinhong11.rebarwrench;

import io.github.lijinhong11.rebarwrench.api.Wrenchable;
import io.github.lijinhong11.rebarwrench.listeners.BrushListener;
import io.github.lijinhong11.rebarwrench.wrenches.TheWrenchItem;
import io.github.pylonmc.pylon.PylonPages;
import io.github.pylonmc.rebar.addon.RebarAddon;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RebarWrench extends JavaPlugin implements RebarAddon {
    public static RebarWrench INSTANCE;
    public static NamespacedKey WRENCH_KEY;

    private static final Map<Class<? extends RebarBlock>, Wrenchable> WRENCHABLE_MAP = new HashMap<>();

    @Override
    public void onLoad() {
        INSTANCE = this;

        WRENCH_KEY = new NamespacedKey(INSTANCE, "wrench");
    }

    @Override
    public void onEnable() {
        registerWithRebar();

        ItemStack wrench = ItemStackBuilder.rebar(Material.CLAY_BALL, WRENCH_KEY)
                .set(DataComponentTypes.ITEM_MODEL, Material.BRUSH.getKey())
                .build();
        RebarItem.register(TheWrenchItem.class, wrench);
        PylonPages.TOOLS.addItem(wrench);

        Bukkit.getPluginManager().registerEvents(new BrushListener(), this);

        getLogger().info("RebarWrench is enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RebarWrench is disabled!");
    }

    @Override
    public @NonNull JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public @NonNull Set<Locale> getLanguages() {


        return Set.of(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE,
                Locale.of("pt", "BR"), Locale.of("pt", "PT"),
                Locale.of("fr", "FR"), Locale.of("es", "ES"));
    }

    @Override
    public @NonNull Material getMaterial() {
        return Material.BRUSH;
    }

    public static void registerWrenchable(@NonNull Class<? extends RebarBlock> clazz, @NonNull Wrenchable wrenchable) {
        WRENCHABLE_MAP.put(clazz, wrenchable);
    }

    public static Wrenchable getWrenchable(@NonNull Class<? extends RebarBlock> clazz) {
        return WRENCHABLE_MAP.get(clazz);
    }
}
