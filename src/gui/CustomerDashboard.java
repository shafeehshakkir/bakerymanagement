// CustomerDashboard.java
package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import db.DBConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerDashboard extends JFrame {

    private final String customerName;
    private final String username;

    private JLabel welcomeLabel;
    private JTable stockTable;
    private DefaultTableModel stockTableModel;
    private JTextField quantityField;
    private JLabel totalLabel;
    private JButton placeOrderButton;
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;

    public CustomerDashboard(String customerName, String username) {
        this.customerName = customerName;
        this.username = username;

        setTitle("Customer Dashboard");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        welcomeLabel = new JLabel("Welcome, " + customerName, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(welcomeLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Place Order Tab
        JPanel placeOrderPanel = new JPanel(new BorderLayout(10, 10));
        placeOrderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        stockTableModel = new DefaultTableModel(new Object[]{"Item", "Price", "Available Qty"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stockTable = new JTable(stockTableModel);
        JScrollPane stockScroll = new JScrollPane(stockTable);
        placeOrderPanel.add(stockScroll, BorderLayout.CENTER);

        JPanel orderControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        orderControlPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField(5);
        orderControlPanel.add(quantityField);

        totalLabel = new JLabel("Total: $0.00");
        orderControlPanel.add(totalLabel);

        placeOrderButton = new JButton("Place Order");
        orderControlPanel.add(placeOrderButton);

        placeOrderPanel.add(orderControlPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Place Order", placeOrderPanel);

        // Order History Tab
        JPanel orderHistoryPanel = new JPanel(new BorderLayout());
        ordersTableModel = new DefaultTableModel(new Object[]{"Order ID", "Item", "Quantity", "Price", "Date"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(ordersTableModel);
        JScrollPane ordersScroll = new JScrollPane(ordersTable);
        orderHistoryPanel.add(ordersScroll, BorderLayout.CENTER);

        tabbedPane.addTab("Order History", orderHistoryPanel);

        // Logout Button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new SweetCornerLogin().setVisible(true);
        });

        add(tabbedPane, BorderLayout.CENTER);
        add(logoutBtn, BorderLayout.SOUTH);

        loadStock();
        loadOrders();

        stockTable.getSelectionModel().addListSelectionListener(e -> updateTotal());
        quantityField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
        });

        placeOrderButton.addActionListener(e -> placeOrder());
    }

    private void loadStock() {
        stockTableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT item_name, price, quantity FROM stock WHERE quantity > 0")) {
            while (rs.next()) {
                stockTableModel.addRow(new Object[]{
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading stock: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTotal() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            totalLabel.setText("Total: $0.00");
            return;
        }
        String qtyText = quantityField.getText().trim();
        if (qtyText.isEmpty()) {
            totalLabel.setText("Total: $0.00");
            return;
        }
        try {
            int qty = Integer.parseInt(qtyText);
            if (qty < 0) {
                totalLabel.setText("Total: $0.00");
                return;
            }
            double price = (double) stockTableModel.getValueAt(selectedRow, 1);
            totalLabel.setText(String.format("Total: $%.2f", qty * price));
        } catch (NumberFormatException ex) {
            totalLabel.setText("Total: $0.00");
        }
    }

    private void placeOrder() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to order.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String qtyText = quantityField.getText().trim();
        if (qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter quantity.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid integer.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int availableQty = (int) stockTableModel.getValueAt(selectedRow, 2);
        if (qty > availableQty) {
            JOptionPane.showMessageDialog(this, "Quantity exceeds available stock.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String item = (String) stockTableModel.getValueAt(selectedRow, 0);
        double price = (double) stockTableModel.getValueAt(selectedRow, 1);
        double totalPrice = price * qty;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement insertOrder = conn.prepareStatement(
                    "INSERT INTO orders (customer_name, item, quantity, price, date) VALUES (?, ?, ?, ?, ?)")) {
                insertOrder.setString(1, customerName);
                insertOrder.setString(2, item);
                insertOrder.setInt(3, qty);
                insertOrder.setDouble(4, totalPrice);
                insertOrder.setString(5, date);
                insertOrder.executeUpdate();
            }

            try (PreparedStatement updateStock = conn.prepareStatement(
                    "UPDATE stock SET quantity = quantity - ? WHERE item_name = ?")) {
                updateStock.setInt(1, qty);
                updateStock.setString(2, item);
                updateStock.executeUpdate();
            }

            conn.commit();

            JOptionPane.showMessageDialog(this, "Order placed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            quantityField.setText("");
            totalLabel.setText("Total: $0.00");
            loadStock();
            loadOrders();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error placing order: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrders() {
        ordersTableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, item, quantity, price, date FROM orders WHERE customer_name = ? ORDER BY date DESC")) {
            ps.setString(1, customerName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ordersTableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("item"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"),
                            rs.getString("date")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        DBConnection.initialize();
        SwingUtilities.invokeLater(() -> new CustomerDashboard("Customer User", "customer").setVisible(true));
    }
}