package org.wonemy;

import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.wonemy.commands.BuyLevelCommand;
import org.wonemy.events.PaperOnBlockBreakEvent;
import org.wonemy.events.PaperPlayerInteractEvent;
import org.wonemy.events.PaperSignChangeEvent;
import org.wonemy.managers.EconomyManager;

import java.util.logging.Logger;

public final class Wonemy extends JavaPlugin {

    @Getter
    private Logger logger = Bukkit.getLogger();

    @Getter @Setter
    private Economy economy;

    private EconomyManager economyManager;

    private static Wonemy plugin;

    @Override
    public void onEnable() {
        plugin = this;

        this.logger.info("Wonemy has started!");

        this.getCommand("buylvl").setExecutor(new BuyLevelCommand());

        this.getServer().getPluginManager().registerEvents(new PaperOnBlockBreakEvent(), this);
        this.getServer().getPluginManager().registerEvents(new PaperPlayerInteractEvent(), this);
        this.getServer().getPluginManager().registerEvents(new PaperSignChangeEvent(), this);

        this.economyManager = new EconomyManager();
    }

    @Override
    public void onDisable() {
        this.logger.info("Wonemy has stopped!");
    }

    public static Wonemy getInstance() {
        return plugin;
    }
}