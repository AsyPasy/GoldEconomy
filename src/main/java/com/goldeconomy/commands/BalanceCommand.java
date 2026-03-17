package com.goldeconomy.commands;

import com.goldeconomy.GoldAPI;
import com.goldeconomy.GoldEconomy;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final GoldEconomy plugin;

    public BalanceCommand(GoldEconomy plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cSpecify a player: /balance <player>"); return true;
            }
            double bal = GoldAPI.getBalance(player.getUniqueId());
            player.sendMessage("§6━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§e  Your Gold Balance");
            player.sendMessage("§6  " + String.format("%.2f", bal) + " 🪙 Gold Coins");
            player.sendMessage("§6━━━━━━━━━━━━━━━━━━━━");
        } else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) { sender.sendMessage("§cPlayer not found."); return true; }
            double bal = GoldAPI.getBalance(target.getUniqueId());
            sender.sendMessage("§e" + target.getName() + "§7's balance: §6"
                + String.format("%.2f", bal) + " 🪙 gold coins");
        }
        return true;
    }
}
