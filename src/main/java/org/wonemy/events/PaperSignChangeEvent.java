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

        // Check if the player has permission to create the sign
        if (!player.hasPermission("wonemy.create")) {
            return;
        }

        if (event.getLine(0).equalsIgnoreCase("[LevelUp]")) {
            try {
                int levels = Integer.parseInt(event.getLine(1));
                double cost = Double.parseDouble(event.getLine(2));

                event.setLine(0, ChatColor.valueOf(getConfig().getString("sign.color", "GREEN")) + "[LevelUp]");
                event.setLine(1, ChatColor.valueOf(getConfig().getString("sign.levelsColor", "YELLOW")) + "Levels: " + levels);
                event.setLine(2, ChatColor.valueOf(getConfig().getString("sign.costColor", "YELLOW")) + "Cost: $" + cost);

                // Set the sign as uneditable
                Sign sign = (Sign) event.getBlock().getState();
                sign.getPersistentDataContainer().set(new NamespacedKey(this, "wonemy_uneditable"), PersistentDataType.BYTE, (byte) 1);
                sign.update();

            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.DARK_RED + "[Wonemy]", ChatColor.RED + "Invalid format on lines 2 or 3.", ChatColor.GREEN + " Use numbers of levels on line 2 and cost on line 3.");
            }
        }
    }
}
