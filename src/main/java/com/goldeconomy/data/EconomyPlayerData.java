package com.goldeconomy.data;

import java.util.UUID;

public class EconomyPlayerData {

    private final UUID   uuid;
    private double       balance;

    public EconomyPlayerData(UUID uuid, double startingBalance) {
        this.uuid    = uuid;
        this.balance = startingBalance;
    }

    public UUID   getUuid()    { return uuid; }
    public double getBalance() { return balance; }

    public void setBalance(double amount) { this.balance = Math.max(0, amount); }
    public void addBalance(double amount) { this.balance += amount; }

    public boolean removeBalance(double amount) {
        if (balance < amount) return false;
        balance -= amount;
        return true;
    }
}
