package model;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa un token (moneda) en la blockchain.
 * Cada token tiene un ID único, un nombre, símbolo y un suministro total.
 */
public class Token {
    private String id;
    private String name;
    private String symbol;
    private long totalSupply;
    private String creator;
    private long createdAt;
    private Map<String, Long> balances; // Dirección -> Saldo

    public Token(String name, String symbol, long totalSupply, String creator) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.symbol = symbol;
        this.totalSupply = totalSupply;
        this.creator = creator;
        this.createdAt = System.currentTimeMillis();
        this.balances = new HashMap<>();

        // No asignar automáticamente al creador
        // Los tokens se asignan manualmente desde TokenPanel a la wallet system
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getTotalSupply() {
        return totalSupply;
    }

    public String getCreator() {
        return creator;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Map<String, Long> getBalances() {
        return new HashMap<>(balances);
    }

    /**
     * Obtiene el balance de una dirección para este token
     */
    public long getBalance(String address) {
        return balances.getOrDefault(address, 0L);
    }

    /**
     * Actualiza directamente el balance de una dirección
     * Se usa internamente para asignar suministro inicial a la wallet system
     */
    public void updateBalance(String address, long newBalance) {
        if (newBalance < 0) {
            return;
        }
        balances.put(address, newBalance);
    }

    /**
     * Transfiere tokens de una dirección a otra
     */
    public boolean transfer(String from, String to, long amount) {
        if (from == null || to == null || amount <= 0) {
            return false;
        }

        long fromBalance = balances.getOrDefault(from, 0L);
        if (fromBalance < amount) {
            return false; // Saldo insuficiente
        }

        balances.put(from, fromBalance - amount);
        balances.put(to, balances.getOrDefault(to, 0L) + amount);
        return true;
    }

    /**
     * Acuña nuevos tokens (solo el creador puede hacerlo)
     */
    public boolean mint(String to, long amount, String caller) {
        if (!caller.equals(creator) || amount <= 0) {
            return false;
        }

        balances.put(to, balances.getOrDefault(to, 0L) + amount);
        totalSupply += amount;
        return true;
    }

    /**
     * Quema tokens (reduce el suministro total)
     */
    public boolean burn(String from, long amount) {
        if (amount <= 0) {
            return false;
        }

        long fromBalance = balances.getOrDefault(from, 0L);
        if (fromBalance < amount) {
            return false;
        }

        balances.put(from, fromBalance - amount);
        totalSupply -= amount;
        return true;
    }

    /**
     * Agrega más suministro a un token existente
     * Los nuevos tokens se asignan manualmente desde la GUI
     */
    public void addToSupply(long amount, String caller) {
        if (amount <= 0) {
            return;
        }

        // Incrementar el suministro total
        this.totalSupply += amount;

        // No asignar automáticamente - se hace desde TokenPanel
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Total: %d - Creador: %s", name, symbol, totalSupply, creator.substring(0, 8) + "...");
    }
}
