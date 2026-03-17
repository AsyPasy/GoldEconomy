package com.goldeconomy.trade;

import com.goldeconomy.GoldAPI;
import com.goldeconomy.GoldEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;

import java.util.*;

public class TradeGUIListener implements Listener {

    private final GoldEconomy plugin;

    public TradeGUIListener(GoldEconomy plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.startsWith(TradeGUI.TITLE_PREFIX)) return;

        event.setCancelled(true);

        TradeSession session = plugin.getTradeManager().getSession(player.getUniqueId());
        if (session == null) { player.closeInventory(); return; }

        int slot    = event.getRawSlot();
        int invSize = event.getInventory().getSize();
        if (slot >= invSize) return; // clicked in player's own bottom inventory

        UUID uuid = player.getUniqueId();

        // ── Your offer slots (allow item movement) ────────────────────────────
        Set<Integer> offerSet = new HashSet<>();
        for (int s : TradeSession.OFFER_SLOTS_LEFT) offerSet.add(s);

        if (offerSet.contains(slot)) {
            event.setCancelled(false);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                session.unreadyBoth();
                TradeGUI.syncOfferAcross(session, uuid);
                TradeGUI.refreshBoth(session);
            });
            return;
        }

        // ── Your gold offer button ────────────────────────────────────────────
        if (slot == TradeSession.SLOT_YOUR_GOLD) {
            double balance = GoldAPI.getBalance(uuid);
            double current = session.getGold(uuid);
            double delta;

            if (event.isShiftClick()) delta = event.isLeftClick() ? 10 : -10;
            else                      delta = event.isLeftClick() ? 1  : -1;

            double newAmount = Math.max(0, Math.min(current + delta, balance));
            session.setGold(uuid, newAmount);
            session.unreadyBoth();
            TradeGUI.syncOfferAcross(session, uuid); // keep their gold display in sync
            TradeGUI.refreshBoth(session);
            return;
        }

        // ── Ready / un-ready ──────────────────────────────────────────────────
        if (slot == TradeSession.SLOT_YOUR_READY) {
            session.setReady(uuid, !session.isReady(uuid));
            TradeGUI.refreshBoth(session);

            if (session.bothReady()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    plugin.getTradeManager().completeTrade(session), 1L);
            }
            return;
        }

        // ── Cancel ────────────────────────────────────────────────────────────
        if (slot == TradeSession.SLOT_CANCEL) {
            plugin.getTradeManager().cancelTrade(uuid, "§cTrade cancelled.");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().startsWith(TradeGUI.TITLE_PREFIX)) return;

        int invSize = event.getInventory().getSize();
        Set<Integer> offerSet = new HashSet<>();
        for (int s : TradeSession.OFFER_SLOTS_LEFT) offerSet.add(s);

        for (int slot : event.getRawSlots()) {
            if (slot < invSize && !offerSet.contains(slot)) {
                event.setCancelled(true);
                return;
            }
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            TradeSession session = plugin.getTradeManager().getSession(player.getUniqueId());
            if (session == null) return;
            session.unreadyBoth();
            TradeGUI.syncOfferAcross(session, player.getUniqueId());
            TradeGUI.refreshBoth(session);
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!event.getView().getTitle().startsWith(TradeGUI.TITLE_PREFIX)) return;

        UUID uuid = player.getUniqueId();
        if (!plugin.getTradeManager().isInTrade(uuid)) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getTradeManager().isInTrade(uuid))
                plugin.getTradeManager().cancelTrade(uuid, "§cTrade cancelled: GUI closed.");
        }, 1L);
    }
}
