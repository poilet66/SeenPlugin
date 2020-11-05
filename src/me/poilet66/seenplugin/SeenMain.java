package me.poilet66.seenplugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class SeenMain extends JavaPlugin {

    private static Economy econ = null;

    @Override
    public void onEnable(){
        if (!setupEconomy() || !setupTowny()) {
            getLogger().severe("[" + getDescription().getName() + "] - Disabled due to no Vault or Towny dependency found.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        loadConfig();
        this.getCommand("seen").setExecutor(new CommandClass(this));
    }

    @Override
    public void onDisable(){

    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupTowny() {
        if(getServer().getPluginManager().getPlugin("Towny") == null) {
            return false;
        }
        return true;
    }

    public static Economy getEcon() {
        return econ;
    }

    public void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}
