package com.goldeconomy.data;

import com.goldeconomy.GoldEconomy;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;

public class EconomyDataManager {

    private final GoldEconomy           plugin;
    private final File                  dataDir;
    private final Map<UUID, EconomyPlayerData> cache = new HashMap<>();

    public EconomyDataManager(GoldEconomy plugin) {
        this.plugin  = plugin;
        this.dataDir = new File(plugin.getDataFolder(), "players");
        if (!dataDir.exists()) dataDir.mkdirs();
    }

    public EconomyPlayerData getPlayerData(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::load);
    }

    public void saveAll() { cache.keySet().forEach(this::save); }

    public void unload(UUID uuid) { save(uuid); cache.remove(uuid); }

    private EconomyPlayerData load(UUID uuid) {
        File file = new File(dataDir, uuid + ".yml");
        double startBal = plugin.getConfig().getDouble("starting-balance", 0.0);
        EconomyPlayerData data = new EconomyPlayerData(uuid, startBal);
        if (!file.exists()) return data;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        data.setBalance(cfg.getDouble("balance", startBal));
        return data;
    }

    private void save(UUID uuid) {
        EconomyPlayerData data = cache.get(uuid);
        if (data == null) return;

        File file = new File(dataDir, uuid + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("balance", data.getBalance());
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
