package org.wonemy.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.wonemy.Wonemy;

public class BuyLevelCommand implements CommandExecutor {
    private final Wonemy wonemy = Wonemy.getInstance();

    @Override
    public boolean onCommand(CommandSender source, Command command, String label, String[] strings) {
        if (source instanceof Player) {
            Player player = (Player) source;
            if (label.equalsIgnoreCase("buylvl")) {
                if (player.hasPermission("wonemy.buylvl")) {
                    double cost = 10.0; // Default cost value if nothing else is specified on the sign
                    int levels = 1; // Default level value if nothing else is specified on the sign

                    if (player.getTargetBlock(null, 5).getState() instanceof Sign) {
                        Sign sign = (Sign) player.getTargetBlock(null, 5).getState();
                        if (sign.getPersistentDataContainer().has(new NamespacedKey(wonemy, "wonemy-uneditable"), PersistentDataType.BYTE)) {
                            player.sendMessage(Component.text("This sign is not editable!").color(NamedTextColor.RED));
                            return true;
                        }

                        if (sign.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "[LevelUp]")) {
                            try {
                                levels = Integer.parseInt(ChatColor.stripColor(sign.getLine(1)).replace("Levels: ", ""));
                                cost = Double.parseDouble(ChatColor.stripColor(sign.getLine(2)).replace("Cost: $", ""));
                            } catch (NumberFormatException e) {
                                player.sendMessage(Component.text("Invalid sign format! Please try again.").color(NamedTextColor.RED));
                                return true;
                            }
                        }
                    }

                    if (wonemy.getEconomy().getBalance(player) >= cost) {
                        EconomyResponse response = wonemy.getEconomy().withdrawPlayer(player, cost);
                        if (response.transactionSuccess()) {
                            player.sendMessage(Component.text("You have purchased " + levels + " levels of experience.").color(NamedTextColor.GREEN));
                            player.giveExpLevels(levels);
                        } else {
                            player.sendMessage(Component.text("Transaction failed! Please contact an admin.").color(NamedTextColor.RED));
                        }
                    } else {
                        player.sendMessage(Component.text("You don't have enough money to complete this purchase!").color(NamedTextColor.RED));
                    }
                } else {
                    player.sendMessage(Component.text("You don't have permission to use this command!").color(NamedTextColor.RED));
                }
            }
        } else {
            source.sendMessage(Component.text("This command can only be ran as a player!").color(NamedTextColor.RED));
        }
        return false;
    }
}
