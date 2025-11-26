package examples;

import model.Blockchain;
import model.Token;
import model.TokenUtils;

/**
 * Demostración rápida del Sistema de Tokens
 * Ejecuta este archivo para ver el sistema en acción
 */
public class QuickTokenDemo {

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("         DEMOSTRACIÓN RÁPIDA DE TOKENS");
        System.out.println("=".repeat(60) + "\n");

        // Crear blockchain
        Blockchain blockchain = new Blockchain();

        // ==================== CREAR TOKENS ====================
        System.out.println("📌 PASO 1: Creando tokens...\n");

        Token dogecoin = blockchain.tokenRegistry.createToken(
            "Dogecoin",
            "DOGE",
            1000000,
            "MINER_1"
        );
        System.out.println("✓ Creado: " + dogecoin.getName() + " (" + dogecoin.getSymbol() + ")");

        Token ethereum = blockchain.tokenRegistry.createToken(
            "Ethereum",
            "ETH",
            1000,
            "MINER_1"
        );
        System.out.println("✓ Creado: " + ethereum.getName() + " (" + ethereum.getSymbol() + ")\n");

        // ==================== TRANSFERENCIAS ====================
        System.out.println("📌 PASO 2: Transfiriendo tokens...\n");

        blockchain.tokenRegistry.transferToken(dogecoin.getId(), "MINER_1", "USUARIO_A", 500000);
        System.out.println("✓ Transferidos 500,000 DOGE a USUARIO_A");

        blockchain.tokenRegistry.transferToken(dogecoin.getId(), "USUARIO_A", "USUARIO_B", 200000);
        System.out.println("✓ Transferidos 200,000 DOGE a USUARIO_B");

        blockchain.tokenRegistry.transferToken(ethereum.getId(), "MINER_1", "USUARIO_A", 500);
        System.out.println("✓ Transferidos 500 ETH a USUARIO_A\n");

        // ==================== MOSTRAR BALANCES ====================
        System.out.println("📌 PASO 3: Estado actual de balances...\n");

        System.out.println("Token: DOGE");
        System.out.println("  MINER_1:   " + blockchain.tokenRegistry.getBalance(dogecoin.getId(), "MINER_1") + " DOGE");
        System.out.println("  USUARIO_A: " + blockchain.tokenRegistry.getBalance(dogecoin.getId(), "USUARIO_A") + " DOGE");
        System.out.println("  USUARIO_B: " + blockchain.tokenRegistry.getBalance(dogecoin.getId(), "USUARIO_B") + " DOGE");

        System.out.println("\nToken: ETH");
        System.out.println("  MINER_1:   " + blockchain.tokenRegistry.getBalance(ethereum.getId(), "MINER_1") + " ETH");
        System.out.println("  USUARIO_A: " + blockchain.tokenRegistry.getBalance(ethereum.getId(), "USUARIO_A") + " ETH\n");

        // ==================== ACUÑACIÓN ====================
        System.out.println("📌 PASO 4: Acuñando nuevos tokens...\n");

        blockchain.tokenRegistry.mintToken(dogecoin.getId(), "USUARIO_A", 100000, "MINER_1");
        System.out.println("✓ Acuñados 100,000 DOGE más para USUARIO_A");
        System.out.println("  Nuevo saldo USUARIO_A: " + blockchain.tokenRegistry.getBalance(dogecoin.getId(), "USUARIO_A") + " DOGE\n");

        // ==================== QUEMA ====================
        System.out.println("📌 PASO 5: Quemando tokens...\n");

        blockchain.tokenRegistry.burnToken(dogecoin.getId(), "USUARIO_B", 50000);
        System.out.println("✓ Se quemaron 50,000 DOGE de USUARIO_B");
        System.out.println("  Nuevo saldo USUARIO_B: " + blockchain.tokenRegistry.getBalance(dogecoin.getId(), "USUARIO_B") + " DOGE");
        System.out.println("  Nuevo suministro total: " + dogecoin.getTotalSupply() + " DOGE\n");

        // ==================== ESTADÍSTICAS ====================
        System.out.println("📌 PASO 6: Estadísticas del token DOGE...\n");

        System.out.println("Holders: " + TokenUtils.getHolderCount(dogecoin));
        System.out.println("Concentración: " + String.format("%.4f", TokenUtils.getConcentrationIndex(dogecoin)));
        System.out.println("Saldo promedio: " + String.format("%.0f", TokenUtils.getAverageBalance(dogecoin)) + " DOGE");
        System.out.println("Max holder: " + TokenUtils.getMaxHolder(dogecoin));
        System.out.println("Integridad: " + (TokenUtils.isBalanced(dogecoin) ? "✓ Verificada" : "✗ Error") + "\n");

        // ==================== TOP HOLDERS ====================
        System.out.println("📌 PASO 7: Top 3 holders de DOGE...\n");

        var topHolders = TokenUtils.getTopHolders(dogecoin, 3);
        int rank = 1;
        for (var holder : topHolders) {
            double pct = (double) holder.getValue() / dogecoin.getTotalSupply() * 100;
            System.out.printf("%d. %s: %d DOGE (%.2f%%)%n", rank++, holder.getKey(), holder.getValue(), pct);
        }
        System.out.println();

        // ==================== LISTAR TODOS LOS TOKENS ====================
        System.out.println("📌 PASO 8: Tokens registrados en la blockchain...\n");

        for (Token token : blockchain.tokenRegistry.getAllTokens()) {
            System.out.println("  • " + token.getName() + " (" + token.getSymbol() + ") - ID: " + token.getId().substring(0, 8) + "...");
        }
        System.out.println();

        // ==================== REPORTE COMPLETO ====================
        System.out.println("📌 PASO 9: Reporte del token DOGE\n");
        System.out.println(TokenUtils.generateReport(dogecoin));

        // ==================== FIN ====================
        System.out.println("=".repeat(60));
        System.out.println("✓ DEMOSTRACIÓN COMPLETADA CON ÉXITO");
        System.out.println("=".repeat(60) + "\n");

        System.out.println("💡 Para usar en GUI:");
        System.out.println("   1. Ejecuta: mvn clean compile");
        System.out.println("   2. Ejecuta: mvn exec:java -Dexec.mainClass=\"GUI.MainWindow\"");
        System.out.println("   3. Ve a la pestaña 'Tokens'");
        System.out.println("   4. Crea nuevos tokens rellenando el formulario\n");
    }
}

