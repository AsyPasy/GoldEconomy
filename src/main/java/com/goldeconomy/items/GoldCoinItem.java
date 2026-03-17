package com.goldeconomy.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class GoldCoinItem {

    private static NamespacedKey KEY;

    private GoldCoinItem() {}

    public static void init(NamespacedKey key) { KEY = key; }

    public static ItemStack create(int amount) {
        int stacks = Math.min(amount, 64);
        ItemStack item = new ItemStack(Material.GOLD_NUGGET, stacks);
        ItemMeta  meta = item.getItemMeta();
        meta.setDisplayName("§6🪙 Gold Coin");
        meta.setLore(List.of(
            "§7The main currency.",
            "§7Value: §e1 coin per piece.",
            "§8Pick up to auto-deposit to balance."
        ));
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isGoldCoin(ItemStack item) {
        return item != null && item.hasItemMeta()
            && item.getItemMeta().getPersistentDataContainer()
                   .has(KEY, PersistentDataType.BYTE);
    }
}
