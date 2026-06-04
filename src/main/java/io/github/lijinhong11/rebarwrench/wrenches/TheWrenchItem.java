package io.github.lijinhong11.rebarwrench.wrenches;

import io.github.lijinhong11.rebarwrench.RebarWrench;
import io.github.lijinhong11.rebarwrench.api.Wrenchable;
import io.github.lijinhong11.rebarwrench.api.properties.Property;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarInteractor;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

public class TheWrenchItem extends RebarItem implements RebarInteractor {
    private boolean enableFastBreaking = getSettings(RebarWrench.WRENCH_KEY).get("enableFastBreaking", ConfigAdapter.BOOLEAN, true);

    public TheWrenchItem(@NonNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onUsedToClick(@NonNull PlayerInteractEvent event, @NonNull EventPriority priority) {
        Player p = event.getPlayer();

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        RebarBlock rb = BlockStorage.get(clickedBlock);
        if (rb == null) return;

        if (enableFastBreaking && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            BlockStorage.breakBlock(clickedBlock, new BlockBreakContext.PluginBreak(clickedBlock, true, true));
            return;
        }
        Wrenchable wrenchable = RebarWrench.getWrenchable(rb.getClass());
        if (wrenchable == null) {
            p.sendMessage(Component.translatable("rebarwrench.message.not_usable"));
            return;
        }

        Map<String, Property<?>> props = wrenchable.properties().getProperties();
        if (props.isEmpty()) return;

        if (!event.getAction().isRightClick()) return;

        List<String> keys = List.copyOf(props.keySet());
        String currentKey = readActiveKey(clickedBlock, keys);

        if (event.getPlayer().isSneaking()) {
            int idx = keys.indexOf(currentKey);
            String nextKey = keys.get((idx + 1) % keys.size());
            writeActiveKey(clickedBlock, nextKey);
            p.sendMessage(Component.translatable("rebarwrench.message.switch_key", RebarArgument.of("key", nextKey)));
        } else {
            Property<?> property = props.get(currentKey);
            int currentIdx = readIndex(clickedBlock, currentKey, property.defaultIndex());
            int nextIdx = (currentIdx + 1) % property.possibleValues().size();
            Object oldValue = property.possibleValues().get(currentIdx);
            Object newValue = property.possibleValues().get(nextIdx);
            writeIndex(clickedBlock, currentKey, nextIdx);
            property.triggerOnChange(rb, oldValue, newValue);
            Component c = Component.translatable("rebarwrench.message.switch_value", RebarArgument.of("key", currentKey), RebarArgument.of("value", String.valueOf(newValue)));
            p.sendMessage(c);
        }
    }

    private static NamespacedKey blockKey(Block block, String sub) {
        return new NamespacedKey("rebarwrench", block.getX() + "_" + block.getY() + "_" + block.getZ() + "_" + sub);
    }

    private static String readActiveKey(Block block, List<String> keys) {
        String key = block.getChunk().getPersistentDataContainer().get(blockKey(block, "active"), PersistentDataType.STRING);
        if (key == null || !keys.contains(key)) {
            return keys.getFirst();
        }
        return key;
    }

    private static void writeActiveKey(Block block, String key) {
        block.getChunk().getPersistentDataContainer().set(blockKey(block, "active"), PersistentDataType.STRING, key);
    }

    private static int readIndex(Block block, String propertyKey, int defaultIndex) {
        NamespacedKey k = blockKey(block, "idx_" + propertyKey);
        return block.getChunk().getPersistentDataContainer().getOrDefault(k, PersistentDataType.INTEGER, defaultIndex);
    }

    private static void writeIndex(Block block, String propertyKey, int index) {
        NamespacedKey k = blockKey(block, "idx_" + propertyKey);
        block.getChunk().getPersistentDataContainer().set(k, PersistentDataType.INTEGER, index);
    }

    public static final class onBreak implements Listener{
        @EventHandler
        public void onBlockBreak(BlockBreakEvent event){
            Block block = event.getBlock();
            if (!(BlockStorage.get(block) instanceof RebarBlock rb)) return;
            Wrenchable wrenchable = RebarWrench.getWrenchable(rb.getClass());
            if (wrenchable == null) return;
            PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
            pdc.remove(blockKey(block, "active"));
            for (String propertyKey : wrenchable.properties().getProperties().keySet()) {
                pdc.remove(blockKey(block, "idx_" + propertyKey));
            }
        }
    }
}
