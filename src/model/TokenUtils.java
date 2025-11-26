package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase auxiliar con funciones avanzadas para trabajar con tokens
 * Incluye métodos de análisis, distribución y estadísticas
 */
public class TokenUtils {

    /**
     * Distribuir tokens desde un creador a múltiples direcciones
     * Útil para airdrops o distribuciones iniciales
     */
    public static void distributeTokens(
            TokenRegistry registry,
            String tokenId,
            String from,
            Map<String, Long> distribution) {

        Token token = registry.getToken(tokenId);
        if (token == null) return;

        for (Map.Entry<String, Long> entry : distribution.entrySet()) {
            registry.transferToken(tokenId, from, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Obtener la lista de holders (poseedores) de un token, ordenados por cantidad
     */
    public static List<Map.Entry<String, Long>> getTopHolders(Token token, int limit) {
        return token.getBalances().entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(limit)
            .toList();
    }

    /**
     * Calcular la concentración de tokens (índice de Herfindahl)
     * Valores cercanos a 1 = muy concentrado
     * Valores cercanos a 0 = distribuido
     */
    public static double getConcentrationIndex(Token token) {
        long totalSupply = token.getTotalSupply();
        if (totalSupply == 0) return 0;

        double hhi = 0;
        for (long balance : token.getBalances().values()) {
            double marketShare = (double) balance / totalSupply;
            hhi += marketShare * marketShare;
        }
        return hhi;
    }

    /**
     * Obtener el número total de holders únicos
     */
    public static int getHolderCount(Token token) {
        return token.getBalances().size();
    }

    /**
     * Calcular el saldo total de tokens (verificación de integridad)
     */
    public static long getTotalBalances(Token token) {
        return token.getBalances().values().stream()
            .mapToLong(Long::longValue)
            .sum();
    }

    /**
     * Verificar si el suministro total coincide con la suma de balances
     */
    public static boolean isBalanced(Token token) {
        return getTotalBalances(token) == token.getTotalSupply();
    }

    /**
     * Obtener estadísticas del token
     */
    public static Map<String, Object> getTokenStats(Token token) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("name", token.getName());
        stats.put("symbol", token.getSymbol());
        stats.put("totalSupply", token.getTotalSupply());
        stats.put("holderCount", getHolderCount(token));
        stats.put("concentrationIndex", getConcentrationIndex(token));
        stats.put("isBalanced", isBalanced(token));
        stats.put("creator", token.getCreator());
        stats.put("createdAt", token.getCreatedAt());

        if (!token.getBalances().isEmpty()) {
            long maxBalance = token.getBalances().values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
            stats.put("maxBalance", maxBalance);

            long avgBalance = token.getTotalSupply() / token.getBalances().size();
            stats.put("avgBalance", avgBalance);
        }

        return stats;
    }

    /**
     * Generar un reporte en texto del token
     */
    public static String generateReport(Token token) {
        StringBuilder report = new StringBuilder();

        report.append("═════════════════════════════════════════\n");
        report.append("        REPORTE DEL TOKEN\n");
        report.append("═════════════════════════════════════════\n\n");

        report.append("INFORMACIÓN GENERAL:\n");
        report.append("  Nombre: ").append(token.getName()).append("\n");
        report.append("  Símbolo: ").append(token.getSymbol()).append("\n");
        report.append("  ID: ").append(token.getId()).append("\n");
        report.append("  Creador: ").append(token.getCreator()).append("\n");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        report.append("  Creado: ").append(sdf.format(new java.util.Date(token.getCreatedAt()))).append("\n\n");

        report.append("MÉTRICAS:\n");
        report.append("  Suministro Total: ").append(token.getTotalSupply()).append("\n");
        report.append("  Holders (poseedores): ").append(getHolderCount(token)).append("\n");
        report.append("  Índice de Concentración: ").append(String.format("%.4f", getConcentrationIndex(token))).append("\n");
        report.append("  Balanza Verificada: ").append(isBalanced(token) ? "✓ Sí" : "✗ No").append("\n\n");

        report.append("DISTRIBUCIÓN PRINCIPAL:\n");
        List<Map.Entry<String, Long>> topHolders = getTopHolders(token, 10);
        int rank = 1;
        for (Map.Entry<String, Long> holder : topHolders) {
            double percentage = (double) holder.getValue() / token.getTotalSupply() * 100;
            report.append(String.format("  %d. %s: %d (%.2f%%)\n",
                rank++, holder.getKey(), holder.getValue(), percentage));
        }

        report.append("\n═════════════════════════════════════════\n");

        return report.toString();
    }

    /**
     * Validar integridad del token
     */
    public static boolean validateTokenIntegrity(Token token) {
        // Verificar que el suministro total = suma de balances
        if (!isBalanced(token)) {
            return false;
        }

        // Verificar que no hay balances negativos
        for (long balance : token.getBalances().values()) {
            if (balance < 0) {
                return false;
            }
        }

        // Verificar que el suministro es positivo
        if (token.getTotalSupply() <= 0) {
            return false;
        }

        return true;
    }

    /**
     * Crear una copia del token (snapshot)
     * Útil para análisis histórico
     */
    public static Map<String, Long> createSnapshot(Token token) {
        return new HashMap<>(token.getBalances());
    }

    /**
     * Comparar dos snapshots y obtener cambios
     */
    public static Map<String, Long> getChanges(Map<String, Long> snapshot1, Map<String, Long> snapshot2) {
        Map<String, Long> changes = new HashMap<>();

        // Obtener todas las direcciones
        java.util.Set<String> allAddresses = new java.util.HashSet<>();
        allAddresses.addAll(snapshot1.keySet());
        allAddresses.addAll(snapshot2.keySet());

        // Calcular cambios
        for (String address : allAddresses) {
            long val1 = snapshot1.getOrDefault(address, 0L);
            long val2 = snapshot2.getOrDefault(address, 0L);
            long change = val2 - val1;
            if (change != 0) {
                changes.put(address, change);
            }
        }

        return changes;
    }

    /**
     * Obtener dirección con mayor balance
     */
    public static String getMaxHolder(Token token) {
        return token.getBalances().entrySet().stream()
            .max((a, b) -> Long.compare(a.getValue(), b.getValue()))
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Obtener saldo promedio por holder
     */
    public static double getAverageBalance(Token token) {
        int holderCount = getHolderCount(token);
        if (holderCount == 0) return 0;
        return (double) token.getTotalSupply() / holderCount;
    }
}

