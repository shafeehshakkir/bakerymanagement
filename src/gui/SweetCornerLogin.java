// SweetCornerLogin.java
package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import db.DBConnection;

public class SweetCornerLogin extends JFrame {

    public SweetCornerLogin() {
        setTitle("Sweet-Corner Login");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Color.pink);

        ImageIcon logo = new ImageIcon("resources/icon.png");
        Image scaledLogo = logo.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(Box.createVerticalStrut(20));
        topPanel.add(logoLabel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(Box.createVerticalStrut(20));

        add(topPanel, BorderLayout.NORTH);

        JPanel loginPanel = new JPanel(new GridLayout(7, 1, 10, 10));
        loginPanel.setPreferredSize(new Dimension(300, 270));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Login"));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Signup");
        JLabel messageLabel = new JLabel("", SwingConstants.CENTER);

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);
        loginPanel.add(signupButton);
        loginPanel.add(messageLabel);

        add(loginPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Please enter username and password.");
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT name, username, role FROM users WHERE username = ? AND password = ? LIMIT 1")) {

                ps.setString(1, user);
                ps.setString(2, pass);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("role");
                        String name = rs.getString("name");
                        messageLabel.setForeground(new Color(34, 139, 34));
                        messageLabel.setText("Login Successful!");

                        SwingUtilities.invokeLater(() -> {
                            dispose();
                            if ("admin".equalsIgnoreCase(role)) {
                                new AdminDashboard(user).setVisible(true);
                            } else {
                                new CustomerDashboard(name, user).setVisible(true);
                            }
                        });
                    } else {
                        messageLabel.setForeground(Color.RED);
                        messageLabel.setText("Invalid credentials.");
                    }
                }
            } catch (SQLException ex) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("DB error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        signupButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                dispose();
                new SignupFrame().setVisible(true);
            });
        });
    }

    public static void main(String[] args) {
        DBConnection.initialize();
        SwingUtilities.invokeLater(() -> new SweetCornerLogin().setVisible(true));
    }
}