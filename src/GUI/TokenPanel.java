package GUI;

import model.Blockchain;
import model.Token;
import model.Transaction;
import wallet.Wallet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TokenPanel extends JPanel {
    private final Blockchain blockchain;
    private final WalletPanel walletPanel;
    private final JTable tokenTable;
    private final DefaultTableModel tableModel;
    private final JTextArea detailsArea;
    private JComboBox<String> tokenCombo; // Variable de instancia para acceso global

    public TokenPanel(Blockchain blockchain, WalletPanel walletPanel) {
        this.blockchain = blockchain;
        this.walletPanel = walletPanel;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior: Crear nuevo token
        JPanel createTokenPanel = createTokenCreationPanel();
        add(createTokenPanel, BorderLayout.NORTH);

        // Panel central: Tabla de tokens
        tableModel = new DefaultTableModel(
            new String[]{"Nombre", "Símbolo", "Suministro Total", "Creador"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tokenTable = new JTable(tableModel);
        tokenTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tokenTable.getSelectionModel().addListSelectionListener(e -> showTokenDetails());

        JScrollPane tableScrollPane = new JScrollPane(tokenTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Tokens Registrados"));

        // Panel derecho: Detalles del token
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane detailsScrollPane = new JScrollPane(detailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Detalles del Token"));
        detailsScrollPane.setPreferredSize(new Dimension(300, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, detailsScrollPane);
        splitPane.setDividerLocation(0.65);

        // Panel central solo con la tabla y detalles
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(splitPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Panel inferior: Botón de actualización
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("Actualizar");
        refreshButton.addActionListener(e -> refreshTokenList());
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Timer para actualización automática
        Timer timer = new Timer(3000, e -> refreshTokenList());
        timer.start();

        refreshTokenList();
    }

    private JPanel createTokenCreationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Crear Nuevo Token"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Campo: Nombre
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Nombre:"), gbc);

        JTextField nameField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // Campo: Símbolo
        gbc.gridx = 2;
        panel.add(new JLabel("Símbolo:"), gbc);

        JTextField symbolField = new JTextField(10);
        gbc.gridx = 3;
        panel.add(symbolField, gbc);

        // Campo: Suministro Total
        gbc.gridx = 4;
        panel.add(new JLabel("Suministro:"), gbc);

        JSpinner supplySpinner = new JSpinner(new SpinnerNumberModel(1000L, 1L, Long.MAX_VALUE, 100L));
        gbc.gridx = 5;
        panel.add(supplySpinner, gbc);

        // Campo: Dirección del Creador
        gbc.gridx = 6;
        panel.add(new JLabel("Creador:"), gbc);

        JTextField creatorField = new JTextField(15);
        creatorField.setText("SYSTEM");
        creatorField.setToolTipText("Dirección del creador del token");
        gbc.gridx = 7;
        panel.add(creatorField, gbc);

        // Botón: Crear Token
        JButton createButton = new JButton("Crear Token");
        createButton.addActionListener(e -> createNewToken(
            nameField.getText(),
            symbolField.getText(),
            ((Number) supplySpinner.getValue()).longValue(),
            creatorField.getText()
        ));
        gbc.gridx = 8;
        gbc.gridy = 0;
        panel.add(createButton, gbc);

        return panel;
    }

    private void createNewToken(String name, String symbol, long totalSupply, String creator) {
        if (name.isEmpty() || symbol.isEmpty() || creator.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor completa todos los campos",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Verificar si el token ya existe
            Token existingToken = blockchain.tokenRegistry.getTokenBySymbol(symbol);
            boolean isNewToken = existingToken == null;

            Token resultToken = blockchain.tokenRegistry.createToken(name, symbol, totalSupply, creator);

            if (isNewToken) {
                // Es un token nuevo - asignar suministro a wallet system
                if (walletPanel != null) {
                    walletPanel.assignTokensToSystem(resultToken.getId(), totalSupply);
                }

                blockchain.getLogger().info("Token creado: " + resultToken.getName() + " (" + resultToken.getSymbol() + ")");
                JOptionPane.showMessageDialog(this,
                    "Token creado exitosamente!\n" +
                    "Nombre: " + resultToken.getName() + "\n" +
                    "Símbolo: " + resultToken.getSymbol() + "\n" +
                    "Suministro: " + resultToken.getTotalSupply() + "\n" +
                    "Suministro asignado a wallet: system\n" +
                    "ID: " + resultToken.getId().substring(0, 8) + "...",
                    "Token Creado",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Se agregó suministro a un token existente - asignar a wallet system
                if (walletPanel != null) {
                    walletPanel.assignTokensToSystem(existingToken.getId(), totalSupply);
                }

                blockchain.getLogger().info("Suministro agregado a token existente: " + resultToken.getSymbol() + " (+" + totalSupply + ")");
                JOptionPane.showMessageDialog(this,
                    "Token ya existía. Se agregó suministro exitosamente!\n" +
                    "Símbolo: " + resultToken.getSymbol() + "\n" +
                    "Suministro total ahora: " + resultToken.getTotalSupply() + "\n" +
                    "Suministro agregado: " + totalSupply + "\n" +
                    "Asignado a wallet: system",
                    "Suministro Agregado",
                    JOptionPane.INFORMATION_MESSAGE);
            }

            refreshTokenList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al crear token: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTokenDetails() {
        int selectedRow = tokenTable.getSelectedRow();
        if (selectedRow == -1) {
            detailsArea.setText("");
            return;
        }

        Token token = blockchain.tokenRegistry.getTokenBySymbol((String) tableModel.getValueAt(selectedRow, 1));

        if (token != null) {
            StringBuilder details = new StringBuilder();
            details.append("=== DETALLES DEL TOKEN ===\n\n");
            details.append("ID: ").append(token.getId()).append("\n");
            details.append("Nombre: ").append(token.getName()).append("\n");
            details.append("Símbolo: ").append(token.getSymbol()).append("\n");
            details.append("Suministro Total: ").append(token.getTotalSupply()).append("\n");
            details.append("Creador: ").append(token.getCreator()).append("\n");
            details.append("Creado: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(token.getCreatedAt()))).append("\n\n");

            details.append("=== SALDOS ===\n");
            token.getBalances().forEach((address, balance) ->
                details.append(address).append(": ").append(balance).append("\n")
            );

            detailsArea.setText(details.toString());
        }
    }

    private void refreshTokenList() {
        tableModel.setRowCount(0);
        for (Token token : blockchain.tokenRegistry.getAllTokens()) {
            tableModel.addRow(new Object[]{
                token.getName(),
                token.getSymbol(),
                token.getTotalSupply(),
                token.getCreator().substring(0, Math.min(15, token.getCreator().length())) + "..."
            });
        }

        // Actualizar ComboBox de burn tokens si existe
        if (tokenCombo != null) {
            String selectedItem = (String) tokenCombo.getSelectedItem();
            tokenCombo.removeAllItems();

            // Cargar todos los tokens de la blockchain
            for (Token token : blockchain.tokenRegistry.getAllTokens()) {
                tokenCombo.addItem(token.getSymbol());
            }

            // Intentar mantener la selección anterior
            if (selectedItem != null && tokenCombo.getItemCount() > 0) {
                boolean found = false;
                for (int i = 0; i < tokenCombo.getItemCount(); i++) {
                    if (tokenCombo.getItemAt(i).equals(selectedItem)) {
                        tokenCombo.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }
                // Si no encuentra el item anterior, selecciona el primero
                if (!found && tokenCombo.getItemCount() > 0) {
                    tokenCombo.setSelectedIndex(0);
                }
            }
        }
    }
}
