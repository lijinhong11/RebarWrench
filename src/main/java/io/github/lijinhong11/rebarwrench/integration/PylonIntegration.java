package io.github.lijinhong11.rebarwrench.integration;

import io.github.pylonmc.pylon.PylonPages;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

/**
 * Keeps every direct reference to Pylon's API in one place.
 *
 * <p>Pylon is only a soft dependency, so this class must never be loaded unless Pylon is actually
 * installed and enabled. {@link io.github.lijinhong11.rebarwrench.RebarWrench} guards all of its
 * calls with {@code Bukkit.getPluginManager().isPluginEnabled("Pylon")}, which stops the JVM from
 * resolving (and failing on) the {@code io.github.pylonmc.pylon.*} classes when Pylon is absent.
 */
public final class PylonIntegration {

    private PylonIntegration() {
    }

    /**
     * Adds the wrench to Pylon's guide tools page. Only call this when Pylon is enabled.
     */
    public static void addToToolsPage(@NonNull ItemStack item) {
        PylonPages.TOOLS.addItem(item);
    }
}
