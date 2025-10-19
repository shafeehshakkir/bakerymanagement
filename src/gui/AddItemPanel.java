// AddItemPanel.java
package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import db.DBConnection;

public class AddItemPanel extends JPanel {

    private JTextField itemNameField;
    private JTextField quantityField;
    private JTextField priceField;
    private JButton addButton;
    private JLabel messageLabel;

    public AddItemPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel titleLabel = new JLabel("Add New Stock Item");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        itemNameField = new JTextField(20);
        quantityField = new JTextField(10);
        priceField = new JTextField(10);
        addButton = new JButton("Add Item");
        messageLabel = new JLabel(" ");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 20, 10);
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy++;
        add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1;
        add(itemNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        add(quantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        add(priceField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(addButton, gbc);

        gbc.gridy++;
        add(messageLabel, gbc);

        addButton.addActionListener(e -> addItem());
    }

    public void resetFields() {
        itemNameField.setText("");
        quantityField.setText("");
        priceField.setText("");
        messageLabel.setText(" ");
    }

    private void addItem() {
        String itemName = itemNameField.getText().trim();
        String qtyText = quantityField.getText().trim();
        String priceText = priceField.getText().trim();

        if (itemName.isEmpty() || qtyText.isEmpty() || priceText.isEmpty()) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("Please fill all fields.");
            return;
        }

        int quantity;
        double price;
        try {
            quantity = Integer.parseInt(qtyText);
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException ex) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("Quantity must be integer and Price must be a number.");
            return;
        }

        if (quantity < 0 || price < 0) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("Quantity and Price must be non-negative.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Check if item exists
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM stock WHERE item_name = ?")) {
                checkStmt.setString(1, itemName);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt("count") > 0) {
                    // Update existing item
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE stock SET quantity = quantity + ?, price = ? WHERE item_name = ?")) {
                        updateStmt.setInt(1, quantity);
                        updateStmt.setDouble(2, price);
                        updateStmt.setString(3, itemName);
                        updateStmt.executeUpdate();
                    }
                    messageLabel.setForeground(new Color(34, 139, 34));
                    messageLabel.setText("Existing item updated.");
                    return;
                }
            }

            // Insert new item
            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO stock (item_name, quantity, price) VALUES (?, ?, ?)")) {
                insertStmt.setString(1, itemName);
                insertStmt.setInt(2, quantity);
                insertStmt.setDouble(3, price);
                insertStmt.executeUpdate();
            }

            messageLabel.setForeground(new Color(34, 139, 34));
            messageLabel.setText("New item added.");
            resetFields();

        } catch (SQLException e) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("DB error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}