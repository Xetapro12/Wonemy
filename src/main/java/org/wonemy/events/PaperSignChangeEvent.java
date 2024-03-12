package org.wonemy.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.wonemy.Wonemy;

public class PaperSignChangeEvent implements Listener {
    private final Wonemy wonemy = Wonemy.getInstance();

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("wonemy.create")) return;

        if (event.getLine(0).equalsIgnoreCase("[LevelUp]")) {
            try {
                int levels = Integer.parseInt(event.getLine(1));
                double cost = Double.parseDouble(event.getLine(2));

                event.setLine(0, ChatColor.DARK_AQUA+ "[LevelUp]");
                event.setLine(1, ChatColor.AQUA + "Levels: " + levels);
                event.setLine(2, ChatColor.AQUA + "Cost: $" + cost);

                Sign sign = (Sign) event.getBlock().getState();
                sign.getPersistentDataContainer().set(new NamespacedKey(wonemy, "wonemy.uneditable"), PersistentDataType.BYTE, (byte) 1);
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid format on lines 2 or 3! Use numbers of levels on line 2, and cost on line 3.").color(NamedTextColor.RED));
            }
        }
    }
}
