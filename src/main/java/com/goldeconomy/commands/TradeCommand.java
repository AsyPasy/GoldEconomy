package com.goldeconomy.commands;

import com.goldeconomy.GoldEconomy;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TradeCommand implements CommandExecutor {

    private final GoldEconomy plugin;

    public TradeCommand(GoldEconomy plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can trade."); return true;
        }
        if (args.length == 0) { player.sendMessage("§eUsage: /trade <player>"); return true; }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || target.equals(player)) {
            player.sendMessage("§cPlayer not found or invalid."); return true;
        }
        plugin.getTradeManager().initiateOrAccept(player, target);
        return true;
    }
}
