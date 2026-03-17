package com.goldeconomy;

import java.util.UUID;

/**
 * Static API used by GoldEconomy internally and by external plugins (e.g. EnhancedJobSystem)
 * via reflection so they don't need a compile-time dependency.
 */
public class GoldAPI {

    private static GoldEconomy plugin;

    static void init(GoldEconomy p) { plugin = p; }

    public static double getBalance(UUID uuid) {
        return plugin.getDataManager().getPlayerData(uuid).getBalance();
    }

    public static void addGold(UUID uuid, double amount) {
        plugin.getDataManager().getPlayerData(uuid).addBalance(amount);
    }

    public static boolean removeGold(UUID uuid, double amount) {
        return plugin.getDataManager().getPlayerData(uuid).removeBalance(amount);
    }

    public static void setGold(UUID uuid, double amount) {
        plugin.getDataManager().getPlayerData(uuid).setBalance(amount);
    }

    public static boolean hasGold(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }
}
