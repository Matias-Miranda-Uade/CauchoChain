package GUI;

import model.Block;
import model.Blockchain;
import model.Token;
import model.Transaction;
import wallet.Wallet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class WalletPanel extends JPanel {
    private final Blockchain blockchain;
    private final List<Wallet> wallets;
    private final DefaultTableModel walletsTableModel;
    private final DefaultTableModel transactionsTableModel;
    private final DefaultComboBoxModel<String> fromWalletModel;
    private final DefaultComboBoxModel<String> toWalletModel;
    private final DefaultComboBoxModel<String> tokenModel;
    private final JComboBox<String> fromWalletCombo;
    private final JComboBox<String> toWalletCombo;
    private final JComboBox<String> tokenCombo;
    private final JTextField amountField;
    private final JLabel tokenBalanceLabel;

    public WalletPanel(Blockchain blockchain) {
        super(new BorderLayout());
        this.blockchain = blockchain;
        this.wallets = new ArrayList<>();

        // Inicializar modelos de tabla primero
        String[] walletColumns = {"Dirección", "Alias", "Saldo"};
        String[] txColumns = {"De", "Para", "Monto", "Estado"};
        this.walletsTableModel = new DefaultTableModel(walletColumns, 0);
        this.transactionsTableModel = new DefaultTableModel(txColumns, 0);

        // Inicializar modelos de ComboBox
        this.fromWalletModel = new DefaultComboBoxModel<>();
        this.toWalletModel = new DefaultComboBoxModel<>();
        this.tokenModel = new DefaultComboBoxModel<>();
        this.fromWalletCombo = new JComboBox<>(fromWalletModel);
        this.toWalletCombo = new JComboBox<>(toWalletModel);
        this.tokenCombo = new JComboBox<>(tokenModel);
        this.amountField = new JTextField();
        this.tokenBalanceLabel = new JLabel("Balance: -");

        // Panel izquierdo: Lista de wallets y creación
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Wallets"));

        // Tabla de wallets
        JTable walletsTable = new JTable(walletsTableModel);
        leftPanel.add(new JScrollPane(walletsTable), BorderLayout.CENTER);

        // Botón crear wallet
        JButton createWalletButton = new JButton("Crear Nueva Wallet");
        createWalletButton.addActionListener(e -> createNewWallet());
        leftPanel.add(createWalletButton, BorderLayout.SOUTH);

        // Panel derecho: Transacciones
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Transacciones"));

        // Panel de envío
        JPanel sendPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        sendPanel.add(new JLabel("Desde:"));
        sendPanel.add(fromWalletCombo);
        sendPanel.add(new JLabel("Para:"));
        sendPanel.add(toWalletCombo);
        sendPanel.add(new JLabel("Token:"));
        sendPanel.add(tokenCombo);
        sendPanel.add(new JLabel("Monto:"));
        sendPanel.add(amountField);
        sendPanel.add(tokenBalanceLabel);

        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> sendTransaction());
        sendPanel.add(new JLabel(""));
        sendPanel.add(sendButton);

        // Listeners para actualizar balance
        fromWalletCombo.addActionListener(e -> updateTokenBalance());
        tokenCombo.addActionListener(e -> updateTokenBalance());

        // Historial de transacciones
        JTable txTable = new JTable(transactionsTableModel);

        rightPanel.add(sendPanel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(txTable), BorderLayout.CENTER);

        // Layout principal
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftPanel,
            rightPanel
        );
        splitPane.setDividerLocation(400);
        add(splitPane, BorderLayout.CENTER);

        // Crear algunas wallets de ejemplo y actualizar la UI inicialmente
        SwingUtilities.invokeLater(() -> {
            // Crear wallet SYSTEM para distribuir tokens
            Wallet systemWallet = new Wallet("system");
            wallets.add(systemWallet);

            // Crear primera wallet sin fondos iniciales
            Wallet firstWallet = new Wallet("Principal");
            wallets.add(firstWallet);


            // Crear segunda wallet
            Wallet secondWallet = new Wallet("Secundaria");
            wallets.add(secondWallet);

            // Actualizar la UI
            updateWalletsList();
            updateTransactionsList();
            // Sincronizar la lista de wallets con la blockchain
            blockchain.setWallets(wallets);
        });

        // Timer para actualización periódica
        Timer timer = new Timer(5000, e -> SwingUtilities.invokeLater(() -> {
            updateWalletsList();
            updateTransactionsList();
        }));
        timer.start();
    }

    private void createNewWallet() {
        // Mostrar diálogo para ingresar el alias
        String alias = JOptionPane.showInputDialog(
            this,
            "Ingrese un alias para la nueva wallet:",
            "Crear Nueva Wallet",
            JOptionPane.PLAIN_MESSAGE
        );

        // Verificar si el usuario canceló o ingresó un alias vacío
        if (alias == null || alias.trim().isEmpty()) {
            return;
        }

        // Verificar si el alias ya existe
        for (Wallet w : wallets) {
            if (w.getAlias().equals(alias.trim())) {
                JOptionPane.showMessageDialog(
                    this,
                    "El alias ya está en uso. Por favor elija otro.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        Wallet wallet = new Wallet(alias.trim());
        wallets.add(wallet);
        updateWalletsList();
        updateTransactionsList();
        // Sincronizar la lista de wallets con la blockchain
        blockchain.setWallets(wallets);

        JOptionPane.showMessageDialog(
            this,
            "Wallet creada exitosamente con alias: " + alias,
            "Wallet Creada",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void updateWalletsList() {
        blockchain.tokenRegistry.syncBalancesFromBlockchain(blockchain);

        // Guardar selección actual de los ComboBox
        String selectedFromWallet = (String) fromWalletCombo.getSelectedItem();
        String selectedToWallet = (String) toWalletCombo.getSelectedItem();
        String selectedToken = (String) tokenCombo.getSelectedItem();

        // Obtener todos los tokens existentes
        List<Token> tokens = blockchain.tokenRegistry.getAllTokens();
        Vector<String> columns = new Vector<>();
        columns.add("Alias");
        columns.add("Dirección");
        for (Token token : tokens) {
            columns.add(token.getSymbol());
        }
        walletsTableModel.setColumnIdentifiers(columns);
        walletsTableModel.setRowCount(0);
        fromWalletModel.removeAllElements();
        toWalletModel.removeAllElements();

        for (Wallet wallet : wallets) {
            Vector<Object> row = new Vector<>();
            row.add(wallet.getAlias());
            row.add(wallet.getAddress());
            for (Token token : tokens) {
                long balance = blockchain.tokenRegistry.getBalance(token.getId(), wallet.getAddress());
                row.add(balance);
            }
            walletsTableModel.addRow(row);
            fromWalletModel.addElement(wallet.getAlias());
            toWalletModel.addElement(wallet.getAlias());
        }
        updateTokenList();

        // Restaurar selección de los ComboBox si sigue existiendo
        if (selectedFromWallet != null) {
            fromWalletCombo.setSelectedItem(selectedFromWallet);
        }
        if (selectedToWallet != null) {
            toWalletCombo.setSelectedItem(selectedToWallet);
        }
        if (selectedToken != null) {
            tokenCombo.setSelectedItem(selectedToken);
        }
    }

    private void updateTokenList() {
        tokenModel.removeAllElements();
        List<Token> tokens = blockchain.tokenRegistry.getAllTokens();

        if (tokens.isEmpty()) {
            tokenModel.addElement("(Sin tokens)");
        } else {
            for (Token token : tokens) {
                tokenModel.addElement(token.getSymbol() + " - " + token.getName());
            }
        }

        updateTokenBalance();
    }

    private void updateTokenBalance() {
        try {
            if (blockchain.tokenRegistry.getAllTokens().isEmpty()) {
                tokenBalanceLabel.setText("Balance: - (sin tokens)");
                return;
            }

            int fromIndex = fromWalletCombo.getSelectedIndex();
            int tokenIndex = tokenCombo.getSelectedIndex();

            if (fromIndex < 0 || tokenIndex < 0 || fromIndex >= wallets.size()) {
                tokenBalanceLabel.setText("Balance: -");
                return;
            }

            Wallet fromWallet = wallets.get(fromIndex);
            List<Token> tokens = blockchain.tokenRegistry.getAllTokens();

            if (tokenIndex >= tokens.size()) {
                tokenBalanceLabel.setText("Balance: -");
                return;
            }

            Token selectedToken = tokens.get(tokenIndex);
            long balance = blockchain.tokenRegistry.getBalance(selectedToken.getId(), fromWallet.getAddress());
            tokenBalanceLabel.setText("Balance: " + balance + " " + selectedToken.getSymbol());
        } catch (Exception e) {
            tokenBalanceLabel.setText("Balance: error");
        }
    }

    private void sendTransaction() {
        try {
            // Obtener los índices seleccionados
            int fromIndex = fromWalletCombo.getSelectedIndex();
            int toIndex = toWalletCombo.getSelectedIndex();
            int tokenIndex = tokenCombo.getSelectedIndex();

            if (fromIndex < 0 || toIndex < 0 || tokenIndex < 0 ||
                fromIndex >= wallets.size() || toIndex >= wallets.size()) {
                JOptionPane.showMessageDialog(this,
                    "Por favor selecciona carteras y token válidos",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Token> tokens = blockchain.tokenRegistry.getAllTokens();
            if (tokens.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No hay tokens disponibles en la blockchain",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tokenIndex >= tokens.size()) {
                JOptionPane.showMessageDialog(this,
                    "Token inválido",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Obtener las wallets
            Wallet fromWallet = wallets.get(fromIndex);
            Wallet toWallet = wallets.get(toIndex);
            Token selectedToken = tokens.get(tokenIndex);

            if (fromWallet == toWallet) {
                JOptionPane.showMessageDialog(this,
                    "No puedes enviar a la misma wallet",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            long amount = Long.parseLong(amountField.getText());

            if (amount <= 0) {
                JOptionPane.showMessageDialog(this,
                    "El monto debe ser mayor a 0",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verificar balance
            long balance = blockchain.tokenRegistry.getBalance(selectedToken.getId(), fromWallet.getAddress());
            if (balance < amount) {
                JOptionPane.showMessageDialog(this,
                    "Balance insuficiente. Balance actual: " + balance + " " + selectedToken.getSymbol(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Realizar la transferencia Y registrar en la blockchain
            boolean success = false;
            try {
                // Crear transacción manualmente y firmarla
                Transaction tx = new Transaction(fromWallet.getAddress(), toWallet.getAddress(), amount);
                tx.setDescription("TOKEN:" + selectedToken.getSymbol() + ":" + amount);
                tx.signTransaction(fromWallet.getKeyPair());
                blockchain.createTransaction(tx);
                // Transferir en el token registry
                success = blockchain.tokenRegistry.transferToken(selectedToken.getId(), fromWallet.getAddress(), toWallet.getAddress(), amount);
            } catch (Exception ex) {
                blockchain.getLogger().error("Error creando transacción: " + ex.getMessage());
                success = false;
            }

            if (success) {
                blockchain.getLogger().info("Transferencia de " + amount + " " + selectedToken.getSymbol() +
                    " de " + fromWallet.getAlias() + " a " + toWallet.getAlias());
                JOptionPane.showMessageDialog(this,
                    "Transferencia de " + amount + " " + selectedToken.getSymbol() + " exitosa\n" +
                    "De: " + fromWallet.getAlias() + "\n" +
                    "Para: " + toWallet.getAlias() + "\n\n" +
                    "La transacción está pendiente de minería",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                amountField.setText("");
                updateWalletsList();
                updateTransactionsList();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al realizar la transferencia",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Por favor ingresa un monto válido (número entero)",
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al enviar: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTransactionsList() {
        transactionsTableModel.setRowCount(0);

        // Transacciones confirmadas (primero las mostramos porque son más importantes)
        for (Block block : blockchain.getChain()) {
            for (Transaction tx : block.getTransactions()) {
                addTransactionToTable(tx, "Confirmada");
            }
        }

        // Transacciones pendientes
        for (Transaction tx : blockchain.pendingTransactions) {
            addTransactionToTable(tx, "Pendiente");
        }
    }

    private String getAliasForAddress(String address) {
        if (address == null) {
            return null;
        }

        for (Wallet wallet : wallets) {
            if (wallet.getAddress().equals(address)) {
                return wallet.getAlias();
            }
        }

        return null;
    }

    private void addTransactionToTable(Transaction tx, String status) {
        String fromAlias = getAliasForAddress(tx.fromAddress);
        String toAlias = getAliasForAddress(tx.toAddress);

        transactionsTableModel.addRow(new Object[]{
            fromAlias != null ? fromAlias : tx.fromAddress,
            toAlias != null ? toAlias : tx.toAddress,
            tx.amount,
            status
        });
    }

    /**
     * Asigna tokens a la wallet system cuando se crea un nuevo token
     * Este método es llamado desde TokenPanel
     */
    public void assignTokensToSystem(String tokenId, long amount) {
        Wallet systemWallet = getWalletByAlias("system");
        if (systemWallet != null && amount > 0) {
            Token token = blockchain.tokenRegistry.getToken(tokenId);
            if (token != null) {
                long currentBalance = token.getBalance(systemWallet.getAddress());
                token.updateBalance(systemWallet.getAddress(), currentBalance + amount);
            }
        }
    }

    /**
     * Busca una wallet por su alias (ignora mayúsculas/minúsculas)
     */
    public Wallet getWalletByAlias(String alias) {
        if (alias == null) return null;
        for (Wallet w : wallets) {
            if (w.getAlias().equalsIgnoreCase(alias)) {
                return w;
            }
        }
        return null;
    }
}
