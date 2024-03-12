package org.wonemy.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.wonemy.Wonemy;

public class EconomyManager {
    private final Wonemy wonemy = Wonemy.getInstance();

    public EconomyManager() {
        if (wonemy.getServer().getPluginManager().getPlugin("Vault") == null) {
            wonemy.getServer().shutdownMessage().append(Component.text("Vault could not be found! Shutting down.").color(NamedTextColor.RED));
        }

        RegisteredServiceProvider<Economy> provider = wonemy.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            wonemy.getServer().shutdownMessage().append(Component.text("The economy service provider could not be found! Shutting down.").color(NamedTextColor.RED));
        }

        wonemy.setEconomy(provider.getProvider());
    }
}
