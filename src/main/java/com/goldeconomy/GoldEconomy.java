package com.goldeconomy;

import com.goldeconomy.commands.*;
import com.goldeconomy.data.EconomyDataManager;
import com.goldeconomy.items.GoldCoinItem;
import com.goldeconomy.listeners.*;
import com.goldeconomy.trade.*;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class GoldEconomy extends JavaPlugin {

    private static GoldEconomy instance;

    private EconomyDataManager dataManager;
    private TradeManager       tradeManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        GoldCoinItem.init(new NamespacedKey(this, "gold_coin"));

        this.dataManager  = new EconomyDataManager(this);
        this.tradeManager = new TradeManager(this);

        GoldAPI.init(this);

        getCommand("gold")   .setExecutor(new GoldCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("trade")  .setExecutor(new TradeCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this),    this);
        getServer().getPluginManager().registerEvents(new GoldCoinListener(this),  this);
        getServer().getPluginManager().registerEvents(new TradeGUIListener(this),  this);

        getLogger().info("GoldEconomy v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (tradeManager != null) tradeManager.cancelAllTrades();
        if (dataManager  != null) dataManager.saveAll();
        getLogger().info("GoldEconomy disabled.");
    }

    public static GoldEconomy getInstance()       { return instance; }
    public EconomyDataManager getDataManager()    { return dataManager; }
    public TradeManager       getTradeManager()   { return tradeManager; }
}
