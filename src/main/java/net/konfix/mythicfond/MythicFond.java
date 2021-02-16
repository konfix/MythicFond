package net.konfix.mythicfond;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.konfix.mythicfond.controllers.FundController;
import net.konfix.mythicfond.listeners.FundListeners;
import net.konfix.mythicfond.utils.Chat;
import net.konfix.mythicfond.utils.FileManager;
import net.konfix.mythicfond.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class MythicFond extends JavaPlugin {

    @Getter private static MythicFond instance;
    @Getter private static Economy econ;

    @Getter private FileManager data;
    @Getter private FileManager lang;

    @Getter private FundController fundController;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        data = new FileManager(this, "data", getDataFolder().getAbsolutePath());
        lang = new FileManager(this, "lang", getDataFolder().getAbsolutePath());

        if (!setupEconomy() ) {
            Chat.log(String.format("[%s] - Plugin iskljucen, Vault nije pronadjen.", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PaperCommandManager paperCommandManager = new PaperCommandManager(this);
        paperCommandManager.registerCommand(new MythicFondCMD(this));

        getServer().getPluginManager().registerEvents(new FundListeners(this), this);

        loadMessages();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            fundController = new FundController(this);
            fundController.load();
        }, 20L);
    }

    @Override
    public void onDisable(){
        fundController.save();
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

    private void loadMessages() {
        for (Message message : Message.values()) {
            if (!this.lang.getConfiguration().contains(message.getPath())) {
                this.lang.getConfiguration().set(message.getPath(), message.getDefault());
            }
        }

        lang.save();
        Message.setFile(this.lang.getConfiguration());
    }
}
