package org.wonemy.untitled;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
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
        getLogger().info(ChatColor.GREEN + "Wonemy Custom Plugin Loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info(ChatColor.GREEN + "Wonemy Custom Plugin UnLoaded");
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

                event.setLine(0, ChatColor.GREEN + "[LevelUp]");
                event.setLine(1, ChatColor.YELLOW + "Levels: " + levels);
                event.setLine(2, ChatColor.YELLOW + "Cost: $" + cost);

            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid format on lines 2 or 3. Use numbers for levels and cost.");
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