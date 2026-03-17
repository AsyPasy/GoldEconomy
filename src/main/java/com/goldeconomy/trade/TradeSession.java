package com.goldeconomy.trade;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class TradeSession {

    // ── Slot constants ────────────────────────────────────────────────────────
    public static final int[] OFFER_SLOTS_LEFT  = {0,1,2,3, 9,10,11,12, 18,19,20,21};
    public static final int[] OFFER_SLOTS_RIGHT = {5,6,7,8, 14,15,16,17, 23,24,25,26};
    public static final int   SLOT_YOUR_GOLD    = 27;
    public static final int   SLOT_THEIR_GOLD   = 35;
    public static final int   SLOT_YOUR_READY   = 36;
    public static final int   SLOT_THEIR_READY  = 44;
    public static final int   SLOT_CANCEL       = 45;

    private final UUID uuidA;
    private final UUID uuidB;

    private Inventory invA;
    private Inventory invB;

    private double  goldA    = 0;
    private double  goldB    = 0;
    private boolean readyA   = false;
    private boolean readyB   = false;

    public TradeSession(UUID a, UUID b) { this.uuidA = a; this.uuidB = b; }

    public UUID getUuidA() { return uuidA; }
    public UUID getUuidB() { return uuidB; }

    public UUID getOther(UUID uuid) { return uuid.equals(uuidA) ? uuidB : uuidA; }

    public boolean isPartner(UUID uuid) { return uuid.equals(uuidA) || uuid.equals(uuidB); }

    public Inventory getInventory(UUID uuid) { return uuid.equals(uuidA) ? invA : invB; }
    public void setInventory(UUID uuid, Inventory inv) {
        if (uuid.equals(uuidA)) invA = inv; else invB = inv;
    }

    public double getGold(UUID uuid)            { return uuid.equals(uuidA) ? goldA : goldB; }
    public void   setGold(UUID uuid, double v)  {
        if (uuid.equals(uuidA)) goldA = Math.max(0, v); else goldB = Math.max(0, v);
    }

    public boolean isReady(UUID uuid)           { return uuid.equals(uuidA) ? readyA : readyB; }
    public void    setReady(UUID uuid, boolean r) {
        if (uuid.equals(uuidA)) readyA = r; else readyB = r;
    }

    public boolean bothReady()   { return readyA && readyB; }
    public void    unreadyBoth() { readyA = false; readyB = false; }

    /** Returns items currently placed in the left (offer) slots of a player's GUI. */
    public ItemStack[] getOfferItems(UUID uuid) {
        Inventory inv = getInventory(uuid);
        if (inv == null) return new ItemStack[0];
        ItemStack[] result = new ItemStack[OFFER_SLOTS_LEFT.length];
        for (int i = 0; i < OFFER_SLOTS_LEFT.length; i++)
            result[i] = inv.getItem(OFFER_SLOTS_LEFT[i]);
        return result;
    }
}
