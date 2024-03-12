package org.wonemy.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataType;
import org.wonemy.Wonemy;

public class PaperOnBlockBreakEvent implements Listener {
    private final Wonemy wonemy = Wonemy.getInstance();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (sign.getPersistentDataContainer().has(new NamespacedKey(wonemy, "wonemy_uneditable"), PersistentDataType.BYTE)) {
                if (player.hasPermission("wonemy.breaksign")) {
                    return;
                }

                event.setCancelled(true);
                player.sendMessage(Component.text("This sign is not editable!").color(NamedTextColor.RED));

            }
        }
    }
}