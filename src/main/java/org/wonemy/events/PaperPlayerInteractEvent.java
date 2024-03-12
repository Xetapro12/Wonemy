package org.wonemy.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.wonemy.Wonemy;

public class PaperPlayerInteractEvent implements Listener {
    private final Wonemy wonemy = Wonemy.getInstance();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && (clickedBlock.getType().toString().endsWith("_SIGN"))) {
                Sign sign = (Sign) clickedBlock.getState();
                if (sign.getPersistentDataContainer().has(new NamespacedKey(wonemy, "wonemy_uneditable"), PersistentDataType.BYTE)) {
                    event.setCancelled(true);
                    if (event.getPlayer().hasPermission("wonemy.buylvl")) {
                        this.handlePurchase(event.getPlayer(), sign);
                    } else {
                        player.sendMessage(Component.text("You don't have permission to buy from this sign!").color(NamedTextColor.RED));
                    }
                }
            }
        }
    }

    /**
     * Handles a purchase when a sign is interacted with
     * @param player The player interacting with this sign
     * @param sign The sign this player is interacting with
     */
    private void handlePurchase(Player player, Sign sign) {
        String[] lines = sign.getLines();
        int levels = Integer.parseInt(ChatColor.stripColor(lines[1]).replace("Levels: ", ""));
        double cost = Double.parseDouble(ChatColor.stripColor(lines[2]).replace("Cost: $", ""));

        if (wonemy.getEconomy().getBalance(player) >= cost) {
            EconomyResponse response = wonemy.getEconomy().withdrawPlayer(player, cost); // Deduct the cost from their account
            if (response.transactionSuccess()) { // Give them the purchased levels
                player.sendMessage(Component.text("You have purchased " + levels + " levels of experience!").color(NamedTextColor.GREEN));
                player.giveExpLevels(levels);
            } else {
                player.sendMessage(Component.text("Transaction failed! Please contact an admin.").color(NamedTextColor.RED));
            }
        } else {
            player.sendMessage(Component.text("You don't enough money in your account to complete this purchase!").color(NamedTextColor.RED));
        }
    }
}