// AdminDashboard.java
package gui;

import javax.swing.*;
import java.awt.*;
import db.DBConnection;

public class AdminDashboard extends JFrame {

    public AdminDashboard(String adminName) {
        setTitle("Admin Dashboard");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, Admin: " + adminName, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 15, 15));
        JButton manageStockBtn = new JButton("Manage Stock");
        JButton viewOrdersBtn = new JButton("View All Orders");
        JButton addItemBtn = new JButton("Add New Item");
        JButton manageUsersBtn = new JButton("Manage Users");
        JButton logoutBtn = new JButton("Logout");

        buttonPanel.add(manageStockBtn);
        buttonPanel.add(viewOrdersBtn);
        buttonPanel.add(addItemBtn);
        buttonPanel.add(manageUsersBtn);
        buttonPanel.add(logoutBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new CardLayout());
        add(centerPanel, BorderLayout.CENTER);

        ManageStockPanel manageStockPanel = new ManageStockPanel();
        ViewOrdersPanel viewOrdersPanel = new ViewOrdersPanel();
        AddItemPanel addItemPanel = new AddItemPanel();
        ManageUsersPanel manageUsersPanel = new ManageUsersPanel();

        centerPanel.add(manageStockPanel, "ManageStock");
        centerPanel.add(viewOrdersPanel, "ViewOrders");
        centerPanel.add(addItemPanel, "AddItem");
        centerPanel.add(manageUsersPanel, "ManageUsers");

        CardLayout cl = (CardLayout) centerPanel.getLayout();

        manageStockBtn.addActionListener(e -> {
            manageStockPanel.loadStock();
            cl.show(centerPanel, "ManageStock");
        });

        viewOrdersBtn.addActionListener(e -> {
            viewOrdersPanel.loadOrders();
            cl.show(centerPanel, "ViewOrders");
        });

        addItemBtn.addActionListener(e -> {
            addItemPanel.resetFields();
            cl.show(centerPanel, "AddItem");
        });

        manageUsersBtn.addActionListener(e -> {
            manageUsersPanel.loadUsers();
            cl.show(centerPanel, "ManageUsers");
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            new SweetCornerLogin().setVisible(true);
        });
    }

    public static void main(String[] args) {
        DBConnection.initialize();
        SwingUtilities.invokeLater(() -> new AdminDashboard("Admin").setVisible(true));
    }
}