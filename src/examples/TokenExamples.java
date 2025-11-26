package examples;

import model.Blockchain;
import model.Token;

/**
 * Ejemplo de uso del sistema de Tokens en PirgaChain
 * Este archivo demuestra cómo usar programáticamente los tokens.
 */
public class TokenExamples {

    public static void main(String[] args) {
        // Crear una blockchain
        Blockchain blockchain = new Blockchain();

        // ==================== EJEMPLO 1: Crear tokens ====================
        System.out.println("=== CREANDO TOKENS ===\n");

        Token dogecoin = blockchain.tokenRegistry.createToken(
            "Dogecoin",
            "DOGE",
            1000000,
            "MINER_001"
        );
        System.out.println("✓ Token creado: " + dogecoin);
        System.out.println("  ID: " + dogecoin.getId());
        System.out.println("  Saldo del creador: " + dogecoin.getBalance("MINER_001") + "\n");

        Token bitcoin = blockchain.tokenRegistry.createToken(
            "Bitcoin",
            "BTC",
            21000000,
            "MINER_001"
        );
        System.out.println("✓ Token creado: " + bitcoin);
        System.out.println("  Saldo del creador: " + bitcoin.getBalance("MINER_001") + "\n");

        // ==================== EJEMPLO 2: Transferir tokens ====================
        System.out.println("=== TRANSFIRIENDO TOKENS ===\n");

        // Transferir DOGE de MINER_001 a USUARIO_A
        boolean transferred = blockchain.tokenRegistry.transferToken(
            dogecoin.getId(),
            "MINER_001",
            "USUARIO_A",
            100000
        );

        if (transferred) {
            System.out.println("✓ Transferencia exitosa");
            System.out.println("  MINER_001 saldo DOGE: " + dogecoin.getBalance("MINER_001"));
            System.out.println("  USUARIO_A saldo DOGE: " + dogecoin.getBalance("USUARIO_A") + "\n");
        }

        // Transferencia de USUARIO_A a USUARIO_B
        blockchain.tokenRegistry.transferToken(
            dogecoin.getId(),
            "USUARIO_A",
            "USUARIO_B",
            50000
        );
        System.out.println("✓ USUARIO_A -> USUARIO_B: 50000 DOGE");
        System.out.println("  USUARIO_A saldo: " + dogecoin.getBalance("USUARIO_A"));
        System.out.println("  USUARIO_B saldo: " + dogecoin.getBalance("USUARIO_B") + "\n");

        // ==================== EJEMPLO 3: Acuñación (Minting) ====================
        System.out.println("=== ACUÑANDO NUEVOS TOKENS ===\n");

        // Solo el creador puede acuñar
        boolean minted = blockchain.tokenRegistry.mintToken(
            dogecoin.getId(),
            "USUARIO_A",
            500000,
            "MINER_001"  // solo el creador
        );

        if (minted) {
            System.out.println("✓ Se acuñaron 500000 DOGE para USUARIO_A");
            System.out.println("  Nuevo saldo: " + dogecoin.getBalance("USUARIO_A"));
            System.out.println("  Nuevo suministro total: " + dogecoin.getTotalSupply() + "\n");
        }

        // Intento fallido: usuario no creador intenta acuñar
        boolean failedMint = blockchain.tokenRegistry.mintToken(
            dogecoin.getId(),
            "USUARIO_A",
            1000000,
            "USUARIO_B"  // no es el creador
        );
        System.out.println("✗ Usuario no creador intentó acuñar: " + failedMint + "\n");

        // ==================== EJEMPLO 4: Quema de tokens ====================
        System.out.println("=== QUEMANDO TOKENS ===\n");

        long balanceAntes = dogecoin.getBalance("USUARIO_A");
        boolean burned = blockchain.tokenRegistry.burnToken(
            dogecoin.getId(),
            "USUARIO_A",
            100000
        );

        if (burned) {
            System.out.println("✓ Se quemaron 100000 DOGE de USUARIO_A");
            System.out.println("  Saldo antes: " + balanceAntes);
            System.out.println("  Saldo después: " + dogecoin.getBalance("USUARIO_A"));
            System.out.println("  Nuevo suministro total: " + dogecoin.getTotalSupply() + "\n");
        }

        // ==================== EJEMPLO 5: Consultar tokens ====================
        System.out.println("=== CONSULTANDO TOKENS ===\n");

        System.out.println("Cantidad de tokens registrados: " + blockchain.tokenRegistry.getTokenCount());
        System.out.println("\nTodos los tokens:");
        for (Token token : blockchain.tokenRegistry.getAllTokens()) {
            System.out.println("  - " + token.getName() + " (" + token.getSymbol() + ")");
        }

        // Buscar por símbolo
        Token buscado = blockchain.tokenRegistry.getTokenBySymbol("BTC");
        System.out.println("\nToken buscado por símbolo 'BTC': " + (buscado != null ? buscado.getName() : "No encontrado"));

        // ==================== EJEMPLO 6: Ver balances ====================
        System.out.println("\n=== BALANCES FINALES ===\n");

        System.out.println("Token: " + dogecoin.getName() + " (" + dogecoin.getSymbol() + ")");
        System.out.println("Suministro total: " + dogecoin.getTotalSupply());
        System.out.println("Distribución:");
        dogecoin.getBalances().forEach((address, balance) ->
            System.out.println("  " + address + ": " + balance + " " + dogecoin.getSymbol())
        );

        // ==================== EJEMPLO 7: Operación fallida (saldo insuficiente) ====================
        System.out.println("\n=== INTENTOS FALLIDOS ===\n");

        boolean failedTransfer = blockchain.tokenRegistry.transferToken(
            dogecoin.getId(),
            "USUARIO_B",
            "USUARIO_A",
            999999999  // USUARIO_B no tiene tanto
        );
        System.out.println("Transferencia con saldo insuficiente: " + failedTransfer);
    }
}

