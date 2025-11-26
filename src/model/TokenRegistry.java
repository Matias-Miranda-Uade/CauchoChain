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
}

