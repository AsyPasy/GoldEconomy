package com.goldeconomy.listeners;

import com.goldeconomy.GoldAPI;
import com.goldeconomy.GoldEconomy;
import com.goldeconomy.items.GoldCoinItem;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class GoldCoinListener implements Listener {

    private final GoldEconomy plugin;

    public GoldCoinListener(GoldEconomy plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack item = event.getItem().getItemStack();
        if (!GoldCoinItem.isGoldCoin(item)) return;

        event.setCancelled(true);
        event.getItem().remove();

        int coins = item.getAmount();
        GoldAPI.addGold(player.getUniqueId(), coins);
        player.sendMessage("§6🪙 +" + coins + " gold coin(s) auto-deposited! §eBalance: "
            + String.format("%.2f", GoldAPI.getBalance(player.getUniqueId())));
    }
}
