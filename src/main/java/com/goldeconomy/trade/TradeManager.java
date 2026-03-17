package com.goldeconomy.trade;

import com.goldeconomy.GoldAPI;
import com.goldeconomy.GoldEconomy;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TradeManager {

    private final GoldEconomy plugin;

    /** playerId → who they sent a pending request to */
    private final Map<UUID, UUID>         pendingRequests = new HashMap<>();
    /** playerId → active TradeSession */
    private final Map<UUID, TradeSession> activeSessions  = new HashMap<>();

    public TradeManager(GoldEconomy plugin) { this.plugin = plugin; }

    // ── Initiate or accept ────────────────────────────────────────────────────

    public void initiateOrAccept(Player sender, Player target) {
        UUID sId = sender.getUniqueId();
        UUID tId = target.getUniqueId();

        if (isInTrade(sId)) { sender.sendMessage("§cYou are already in a trade!"); return; }
        if (isInTrade(tId)) { sender.sendMessage("§c" + target.getName() + " is already in a trade!"); return; }

        // Check if target already sent sender a request → accept
        if (Objects.equals(pendingRequests.get(tId), sId)) {
            pendingRequests.remove(tId);
            openTrade(sender, target);
            return;
        }

        // Send new request
        pendingRequests.put(sId, tId);
        sender.sendMessage("§aTrade request sent to §e" + target.getName() + "§a.");

        TextComponent msg    = new TextComponent("§e" + sender.getName() + " §awants to trade. ");
        TextComponent accept = new TextComponent("§a§l[ACCEPT]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade " + sender.getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder("§eClick to accept the trade request").create()));
        msg.addExtra(accept);
        target.spigot().sendMessage(msg);

        long timeout = plugin.getConfig().getLong("trade-request-timeout", 60) * 20L;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (Objects.equals(pendingRequests.get(sId), tId)) {
                pendingRequests.remove(sId);
                if (sender.isOnline()) sender.sendMessage("§cTrade request to §e" + target.getName() + " §cexpired.");
                if (target.isOnline()) target.sendMessage("§cTrade request from §e" + sender.getName() + " §cexpired.");
            }
        }, timeout);
    }

    private void openTrade(Player p1, Player p2) {
        TradeSession session = new TradeSession(p1.getUniqueId(), p2.getUniqueId());
        activeSessions.put(p1.getUniqueId(), session);
        activeSessions.put(p2.getUniqueId(), session);

        TradeGUI.open(plugin, session, p1.getUniqueId());
        TradeGUI.open(plugin, session, p2.getUniqueId());

        p1.sendMessage("§aTrade opened with §e" + p2.getName() + "§a!");
        p2.sendMessage("§aTrade opened with §e" + p1.getName() + "§a!");
    }

    // ── Session access ────────────────────────────────────────────────────────

    public TradeSession getSession(UUID uuid) { return activeSessions.get(uuid); }
    public boolean isInTrade(UUID uuid)       { return activeSessions.containsKey(uuid); }

    // ── Cancel ────────────────────────────────────────────────────────────────

    public void cancelTrade(UUID uuid, String message) {
        TradeSession session = activeSessions.get(uuid);
        if (session == null) return;

        returnItems(session, session.getUuidA());
        returnItems(session, session.getUuidB());

        activeSessions.remove(session.getUuidA());
        activeSessions.remove(session.getUuidB());

        closeAndMessage(plugin.getServer().getPlayer(session.getUuidA()), message);
        closeAndMessage(plugin.getServer().getPlayer(session.getUuidB()), message);
    }

    // ── Complete ──────────────────────────────────────────────────────────────

    public void completeTrade(TradeSession session) {
        Player pA = plugin.getServer().getPlayer(session.getUuidA());
        Player pB = plugin.getServer().getPlayer(session.getUuidB());

        if (pA == null || pB == null) {
            cancelTrade(session.getUuidA(), "§cTrade cancelled: player disconnected."); return;
        }

        double goldA = session.getGold(session.getUuidA());
        double goldB = session.getGold(session.getUuidB());

        if (!GoldAPI.hasGold(session.getUuidA(), goldA)) {
            pA.sendMessage("§cTrade failed: insufficient gold.");
            cancelTrade(session.getUuidA(), "§cTrade cancelled."); return;
        }
        if (!GoldAPI.hasGold(session.getUuidB(), goldB)) {
            pB.sendMessage("§cTrade failed: insufficient gold.");
            cancelTrade(session.getUuidA(), "§cTrade cancelled."); return;
        }

        ItemStack[] itemsA = session.getOfferItems(session.getUuidA());
        ItemStack[] itemsB = session.getOfferItems(session.getUuidB());

        // Swap gold
        GoldAPI.removeGold(session.getUuidA(), goldA);
        GoldAPI.removeGold(session.getUuidB(), goldB);
        GoldAPI.addGold(session.getUuidA(), goldB);
        GoldAPI.addGold(session.getUuidB(), goldA);

        // Swap items
        for (ItemStack item : itemsA)
            if (item != null && item.getType() != Material.AIR) pB.getInventory().addItem(item);
        for (ItemStack item : itemsB)
            if (item != null && item.getType() != Material.AIR) pA.getInventory().addItem(item);

        activeSessions.remove(session.getUuidA());
        activeSessions.remove(session.getUuidB());

        pA.closeInventory();
        pB.closeInventory();
        pA.sendMessage("§a✔ Trade complete with §e" + pB.getName() + "§a!");
        pB.sendMessage("§a✔ Trade complete with §e" + pA.getName() + "§a!");
    }

    public void cancelAllTrades() {
        new HashSet<>(activeSessions.keySet())
            .forEach(u -> cancelTrade(u, "§cTrade cancelled: server shutting down."));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void returnItems(TradeSession session, UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return;
        for (ItemStack item : session.getOfferItems(uuid))
            if (item != null && item.getType() != Material.AIR)
                player.getInventory().addItem(item);
    }

    private void closeAndMessage(Player player, String msg) {
        if (player == null) return;
        player.closeInventory();
        player.sendMessage(msg);
    }
}
