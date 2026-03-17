package com.goldeconomy.commands;

import com.goldeconomy.GoldAPI;
import com.goldeconomy.GoldEconomy;
import com.goldeconomy.items.GoldCoinItem;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GoldCommand implements CommandExecutor {

    private final GoldEconomy plugin;

    public GoldCommand(GoldEconomy plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("goldeconomy.admin")) {
            sender.sendMessage("§cNo permission."); return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§eUsage: /gold <give|take|set|balance|withdraw> <player> [amount]");
            return true;
        }

        String subCmd = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found: " + args[1]); return true; }

        switch (subCmd) {
            case "give" -> {
                double amt = parsePositive(sender, args, 2); if (amt < 0) return true;
                GoldAPI.addGold(target.getUniqueId(), amt);
                sender.sendMessage("§aGave §e" + amt + " §agold to §e" + target.getName() + "§a.");
                target.sendMessage("§aYou received §e" + amt + " §6🪙 gold coins§a!");
            }
            case "take" -> {
                double amt = parsePositive(sender, args, 2); if (amt < 0) return true;
                if (!GoldAPI.removeGold(target.getUniqueId(), amt)) {
                    sender.sendMessage("§c" + target.getName() + " doesn't have enough gold.");
                } else {
                    sender.sendMessage("§aTook §e" + amt + " §agold from §e" + target.getName() + "§a.");
                    target.sendMessage("§c" + amt + " gold coins were removed from your balance.");
                }
            }
            case "set" -> {
                double amt = parseNonNeg(sender, args, 2); if (amt < 0) return true;
                GoldAPI.setGold(target.getUniqueId(), amt);
                sender.sendMessage("§aSet §e" + target.getName() + "§a's balance to §e" + amt + "§a.");
                target.sendMessage("§aYour gold balance was set to §e" + amt + " §6🪙§a.");
            }
            case "balance" -> {
                double bal = GoldAPI.getBalance(target.getUniqueId());
                sender.sendMessage("§e" + target.getName() + "§a's balance: §6"
                    + String.format("%.2f", bal) + " 🪙 gold coins");
            }
            case "withdraw" -> {
                double amt = parsePositive(sender, args, 2); if (amt < 0) return true;
                int coins = (int) amt;
                if (!GoldAPI.removeGold(target.getUniqueId(), coins)) {
                    sender.sendMessage("§c" + target.getName() + " doesn't have enough gold.");
                } else {
                    target.getInventory().addItem(GoldCoinItem.create(coins));
                    sender.sendMessage("§aWithdrew §e" + coins + " §agold coins for §e" + target.getName() + "§a.");
                    target.sendMessage("§aWithdrew §e" + coins + " §6🪙 Gold Coins §afrom your balance!");
                }
            }
            default -> sender.sendMessage("§eUsage: /gold <give|take|set|balance|withdraw> <player> [amount]");
        }
        return true;
    }

    private double parsePositive(CommandSender s, String[] args, int idx) {
        if (args.length <= idx) { s.sendMessage("§cMissing amount."); return -1; }
        try {
            double v = Double.parseDouble(args[idx]);
            if (v <= 0) { s.sendMessage("§cAmount must be positive."); return -1; }
            return v;
        } catch (NumberFormatException e) { s.sendMessage("§cInvalid number."); return -1; }
    }

    private double parseNonNeg(CommandSender s, String[] args, int idx) {
        if (args.length <= idx) { s.sendMessage("§cMissing amount."); return -1; }
        try {
            double v = Double.parseDouble(args[idx]);
            if (v < 0) { s.sendMessage("§cAmount cannot be negative."); return -1; }
            return v;
        } catch (NumberFormatException e) { s.sendMessage("§cInvalid number."); return -1; }
    }
}
