package org.wonemy.untitled;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Wonemy extends JavaPlugin implements Listener {

    private Economy economy;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!setupEconomy()) {
            getLogger().severe("Vault or an economy plugin is not installed!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Wonemy]", ChatColor.DARK_GREEN + "Custom Plugin Loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Wonemy]", ChatColor.DARK_GREEN + "Wonemy Custom Plugin Unloaded");
    }

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

                event.setLine(0, ChatColor.DARK_AQUA+ "[LevelUp]");
                event.setLine(1, ChatColor.AQUA + "Levels: " + levels);
                event.setLine(2, ChatColor.AQUA + "Cost: $" + cost);

                // Set the sign as uneditable
                Sign sign = (Sign) event.getBlock().getState();
                sign.getPersistentDataContainer().set(new NamespacedKey(this, "wonemy_uneditable"), PersistentDataType.BYTE, (byte) 1);
                sign.update();

            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.DARK_RED + "[Wonemy]", ChatColor.RED + "Invalid format on lines 2 or 3.", ChatColor.GREEN + " Use numbers of levels on line 2 and cost on line 3.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock != null && (clickedBlock.getType().toString().endsWith("_SIGN"))) {
                Sign sign = (Sign) clickedBlock.getState();

                // Check if the sign has the "wonemy_uneditable" metadata
                if (sign.getPersistentDataContainer().has(new NamespacedKey(this, "wonemy_uneditable"), PersistentDataType.BYTE)) {
                    event.setCancelled(true);

                    // Check if the player has permission to buy from the sign
                    if (event.getPlayer().hasPermission("wonemy.buylvl")) {
                        handleSignPurchase(event.getPlayer(), sign);
                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to buy from this sign.");
                    }
                }
            }
        }
    }

    private void handleSignPurchase(Player player, Sign sign) {
        // Extract levels and cost from the sign text
        String[] lines = sign.getLines();
        int levels = Integer.parseInt(ChatColor.stripColor(lines[1]).replace("Levels: ", ""));
        double cost = Double.parseDouble(ChatColor.stripColor(lines[2]).replace("Cost: $", ""));

        // Check if the player has enough money
        if (economy.getBalance(player) >= cost) {
            // Deduct the cost from the player's account
            EconomyResponse response = economy.withdrawPlayer(player, cost);

            if (response.transactionSuccess()) {
                // Give the player the purchased levels
                player.sendMessage(ChatColor.GREEN + "You have purchased " + levels + " levels of experience!");
                player.giveExpLevels(levels);
            } else {
                player.sendMessage(ChatColor.RED + "Transaction failed. Please contact an admin.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough money to purchase!");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Check if the broken block is a sign
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();

            // Check if the sign has the "wonemy_uneditable" metadata
            if (sign.getPersistentDataContainer().has(new NamespacedKey(this, "wonemy_uneditable"), PersistentDataType.BYTE)) {
                // Check if the player has permission to break the sign
                if (event.getPlayer().hasPermission("wonemy.breaksign")) {
                    return;
                }

                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "This sign is not editable.");
            }
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("buylvl") && sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("wonemy.buylvl")) {
                // Check if the player has enough money
                double cost = 10.0; // Default cost if not specified on the sign
                int levels = 1;    // Default levels if not specified on the sign

                // Check if the player is looking at a [LevelUp] sign
                if (player.getTargetBlock(null, 5).getState() instanceof org.bukkit.block.Sign) {
                    org.bukkit.block.Sign sign = (org.bukkit.block.Sign) player.getTargetBlock(null, 5).getState();

                    // Check if the sign is editable
                    if (sign.getPersistentDataContainer().has(new NamespacedKey(this, "wonemy_uneditable"), PersistentDataType.BYTE)) {
                        player.sendMessage(ChatColor.RED + "This sign is not editable.");
                        return true;
                    }

                    if (sign.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "[LevelUp]")) {
                        try {
                            levels = Integer.parseInt(ChatColor.stripColor(sign.getLine(1)).replace("Levels: ", ""));
                            cost = Double.parseDouble(ChatColor.stripColor(sign.getLine(2)).replace("Cost: $", ""));
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Invalid sign format. Please check the sign.");
                            return true;
                        }
                    }
                }

                if (economy.getBalance(player) >= cost) {
                    EconomyResponse response = economy.withdrawPlayer(player, cost);
                    if (response.transactionSuccess()) {
                        player.sendMessage(ChatColor.GREEN + "You have purchased " + levels + " levels of experience!");
                        player.giveExpLevels(levels);
                    } else {
                        player.sendMessage(ChatColor.RED + "Transaction failed. Please contact an admin.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have enough money to purchase!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            }

            return true;
        }
        return false;
    }
}