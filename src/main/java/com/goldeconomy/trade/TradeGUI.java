package com.goldeconomy.trade;

import com.goldeconomy.GoldAPI;
import com.goldeconomy.GoldEconomy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class TradeGUI {

    public static final String TITLE_PREFIX = "§8§l✦ §r§eTrade with: §r";

    private TradeGUI() {}

    public static void open(GoldEconomy plugin, TradeSession session, UUID viewerUuid) {
        Player viewer = plugin.getServer().getPlayer(viewerUuid);
        if (viewer == null) return;

        Player other     = plugin.getServer().getPlayer(session.getOther(viewerUuid));
        String otherName = other != null ? other.getName() : "Unknown";

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + otherName);
        session.setInventory(viewerUuid, inv);

        fillStaticElements(inv);
        updateGoldDisplays(inv, session, viewerUuid);
        updateReadyButtons(inv, session, viewerUuid);

        viewer.openInventory(inv);
    }

    // ── Static layout ─────────────────────────────────────────────────────────

    static void fillStaticElements(Inventory inv) {
        // Divider column (4, 13, 22, 31, 40, 49)
        ItemStack div = pane(Material.GRAY_STAINED_GLASS_PANE, "§7──────");
        for (int s : new int[]{4,13,22,31,40,49}) inv.setItem(s, div);

        // Black filler for unused bottom corners
        ItemStack black = pane(Material.BLACK_STAINED_GLASS_PANE, "§r");
        for (int s : new int[]{28,29,30,32,33,34,37,38,39,41,42,43,46,47,48,50,51,52,53})
            inv.setItem(s, black);
    }

    // ── Gold display ──────────────────────────────────────────────────────────

    public static void updateGoldDisplays(Inventory inv, TradeSession session, UUID viewerUuid) {
        double yourGold  = session.getGold(viewerUuid);
        double theirGold = session.getGold(session.getOther(viewerUuid));

        // Slot 27: your gold (clickable)
        ItemStack yourItem = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta  ym       = yourItem.getItemMeta();
        ym.setDisplayName("§6Your Gold Offer: §e" + String.format("%.0f", yourGold));
        ym.setLore(List.of(
            "§eLeft click:   §7+1 gold",
            "§eRight click:  §7-1 gold",
            "§eShift + Left: §7+10 gold",
            "§eShift + Right:§7-10 gold"
        ));
        yourItem.setItemMeta(ym);
        inv.setItem(TradeSession.SLOT_YOUR_GOLD, yourItem);

        // Slot 35: their gold (display only)
        ItemStack theirItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta  tm        = theirItem.getItemMeta();
        tm.setDisplayName("§6Their Gold Offer: §e" + String.format("%.0f", theirGold));
        tm.setLore(List.of("§7Read-only."));
        theirItem.setItemMeta(tm);
        inv.setItem(TradeSession.SLOT_THEIR_GOLD, theirItem);
    }

    // ── Ready buttons ─────────────────────────────────────────────────────────

    public static void updateReadyButtons(Inventory inv, TradeSession session, UUID viewerUuid) {
        boolean yourReady  = session.isReady(viewerUuid);
        boolean theirReady = session.isReady(session.getOther(viewerUuid));

        // Slot 36: your ready
        ItemStack yr = new ItemStack(yourReady ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta  yrm = yr.getItemMeta();
        yrm.setDisplayName(yourReady ? "§a§l✔ YOU ARE READY" : "§c§l✗ NOT READY");
        yrm.setLore(List.of(yourReady ? "§7Click to un-ready." : "§7Click when happy with the trade."));
        yr.setItemMeta(yrm);
        inv.setItem(TradeSession.SLOT_YOUR_READY, yr);

        // Slot 44: their ready (display only)
        ItemStack tr  = new ItemStack(theirReady ? Material.LIME_DYE : Material.ORANGE_DYE);
        ItemMeta  trm = tr.getItemMeta();
        trm.setDisplayName(theirReady ? "§a§l✔ THEY ARE READY" : "§e§l⏳ WAITING FOR THEM");
        trm.setLore(List.of("§7Read-only."));
        tr.setItemMeta(trm);
        inv.setItem(TradeSession.SLOT_THEIR_READY, tr);

        // Slot 45: cancel
        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta  cm     = cancel.getItemMeta();
        cm.setDisplayName("§c§lCancel Trade");
        cm.setLore(List.of("§7Cancels the trade.", "§7Your items will be returned."));
        cancel.setItemMeta(cm);
        inv.setItem(TradeSession.SLOT_CANCEL, cancel);
    }

    // ── Sync offer of one player to the other's GUI ───────────────────────────

    public static void syncOfferAcross(TradeSession session, UUID actor) {
        Inventory actorInv = session.getInventory(actor);
        Inventory otherInv = session.getInventory(session.getOther(actor));
        if (actorInv == null || otherInv == null) return;

        for (int i = 0; i < TradeSession.OFFER_SLOTS_LEFT.length; i++) {
            ItemStack item = actorInv.getItem(TradeSession.OFFER_SLOTS_LEFT[i]);
            otherInv.setItem(TradeSession.OFFER_SLOTS_RIGHT[i], item);
        }
    }

    // ── Refresh both GUIs ─────────────────────────────────────────────────────

    public static void refreshBoth(TradeSession session) {
        refreshFor(session, session.getUuidA());
        refreshFor(session, session.getUuidB());
    }

    private static void refreshFor(TradeSession session, UUID uuid) {
        Inventory inv = session.getInventory(uuid);
        if (inv == null) return;
        updateGoldDisplays(inv, session, uuid);
        updateReadyButtons(inv, session, uuid);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static ItemStack pane(Material mat, String name) {
        ItemStack g = new ItemStack(mat);
        ItemMeta  m = g.getItemMeta();
        m.setDisplayName(name);
        g.setItemMeta(m);
        return g;
    }
}
