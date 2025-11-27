package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registro centralizado de tokens en la blockchain.
 * Permite crear, gestionar y acceder a los tokens.
 */
public class TokenRegistry {
    private Map<String, Token> tokens; // ID del token -> Token
    private List<String> tokenIds; // Lista ordenada de IDs de tokens

    public TokenRegistry() {
        this.tokens = new HashMap<>();
        this.tokenIds = new ArrayList<>();
    }

    /**
     * Crea un nuevo token o agrega suministro a uno existente
     * Si el token con el mismo símbolo ya existe, agrega el suministro al existente
     */
    public Token createToken(String name, String symbol, long totalSupply, String creator) {
        if (name == null || name.isEmpty() || symbol == null || symbol.isEmpty()) {
            throw new IllegalArgumentException("Nombre y símbolo del token no pueden estar vacíos");
        }

        // Verificar si ya existe un token con este símbolo
        Token existingToken = getTokenBySymbol(symbol);

        if (existingToken != null) {
            // Token ya existe: agregar suministro al existente
            // Aumentar el suministro total
            long newTotalSupply = existingToken.getTotalSupply() + totalSupply;
            existingToken.addToSupply(totalSupply, creator);
            return existingToken;
        }

        // Token no existe: crear uno nuevo
        Token newToken = new Token(name, symbol, totalSupply, creator);
        tokens.put(newToken.getId(), newToken);
        tokenIds.add(newToken.getId());

        return newToken;
    }

    /**
     * Obtiene un token por su ID
     */
    public Token getToken(String tokenId) {
        return tokens.get(tokenId);
    }

    /**
     * Obtiene todos los tokens
     */
    public List<Token> getAllTokens() {
        List<Token> result = new ArrayList<>();
        for (String id : tokenIds) {
            result.add(tokens.get(id));
        }
        return result;
    }

    /**
     * Obtiene un token por su símbolo
     */
    public Token getTokenBySymbol(String symbol) {
        for (Token token : getAllTokens()) {
            if (token.getSymbol().equals(symbol)) {
                return token;
            }
        }
        return null;
    }

    /**
     * Verifica si un token existe
     */
    public boolean tokenExists(String tokenId) {
        return tokens.containsKey(tokenId);
    }

    /**
     * Obtiene la cantidad de tokens registrados
     */
    public int getTokenCount() {
        return tokens.size();
    }

    /**
     * Obtiene el balance de una dirección en un token específico
     */
    public long getBalance(String tokenId, String address) {
        Token token = getToken(tokenId);
        if (token == null) {
            return 0;
        }
        return token.getBalance(address);
    }

    /**
     * Transfiere tokens entre direcciones
     */
    public boolean transferToken(String tokenId, String from, String to, long amount) {
        Token token = getToken(tokenId);
        if (token == null) {
            return false;
        }
        return token.transfer(from, to, amount);
    }

    /**
     * Transfiere tokens y crea una transacción en la blockchain
     */
    public boolean transferTokenWithTransaction(String tokenId, String from, String to, long amount, Blockchain blockchain) {
        Token token = getToken(tokenId);
        if (token == null) {
            return false;
        }

        // Primero validar que el saldo sea suficiente
        long fromBalance = token.getBalance(from);
        if (fromBalance < amount) {
            return false;
        }

        // Hacer la transferencia en el token
        if (!token.transfer(from, to, amount)) {
            return false;
        }

        // Crear una transacción en la blockchain para registrar el movimiento
        // Usamos una transacción con el formato: token_id:from:to:amount
        String description = "TOKEN:" + token.getSymbol() + ":" + amount;
        Transaction tx = new Transaction(from, to, amount);
        tx.setDescription(description);

        try {
            blockchain.createTransaction(tx);
            return true;
        } catch (Exception e) {
            // Si falla la creación de la transacción, revertir la transferencia de tokens
            token.transfer(to, from, amount);
            return false;
        }
    }

    /**
     * Acuña nuevos tokens (solo el creador)
     */
    public boolean mintToken(String tokenId, String to, long amount, String caller) {
        Token token = getToken(tokenId);
        if (token == null) {
            return false;
        }
        return token.mint(to, amount, caller);
    }

    /**
     * Quema tokens
     */
    public boolean burnToken(String tokenId, String from, long amount) {
        Token token = getToken(tokenId);
        if (token == null) {
            return false;
        }
        return token.burn(from, amount);
    }

    /**
     * Sincroniza los balances de tokens basándose en las transacciones confirmadas
     * en la blockchain. Esto recalcula los balances considerando todas las
     * transacciones minadas en bloques.
     */
    public void syncBalancesFromBlockchain(Blockchain blockchain) {
        if (blockchain == null) {
            return;
        }

        // Para cada token, recalcular sus balances basándose en transacciones confirmadas
        for (Token token : getAllTokens()) {
            // Limpiar balances y recalcular
            Map<String, Long> newBalances = new HashMap<>();

            // Procesar todas las transacciones en la blockchain
            for (Block block : blockchain.getChain()) {
                for (Transaction tx : block.getTransactions()) {
                    // Solo procesar si es una transacción de este token
                    if (tx.getDescription() != null && tx.getDescription().startsWith("TOKEN:" + token.getSymbol())) {
                        // Restar del remitente
                        if (tx.fromAddress != null) {
                            long currentBalance = newBalances.getOrDefault(tx.fromAddress, 0L);
                            newBalances.put(tx.fromAddress, currentBalance - (long) tx.amount);
                        }
                        // Sumar al destinatario
                        if (tx.toAddress != null && !tx.toAddress.equals("BURN")) {
                            long currentBalance = newBalances.getOrDefault(tx.toAddress, 0L);
                            newBalances.put(tx.toAddress, currentBalance + (long) tx.amount);
                        }
                    }
                }
            }

            // Actualizar los balances del token
            for (Map.Entry<String, Long> entry : newBalances.entrySet()) {
                if (entry.getValue() >= 0) {
                    token.updateBalance(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}

