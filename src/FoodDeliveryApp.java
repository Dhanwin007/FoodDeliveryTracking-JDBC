import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// ======================================================
// 1. DATA MODEL
// ======================================================

class FoodOrder {

    private String orderId;
    private String customerName;
    private String restaurant;
    private double amount;
    private String status;

    public FoodOrder(
            String orderId,
            String customerName,
            String restaurant,
            double amount,
            String status
    ) {

        this.orderId = orderId;
        this.customerName = customerName;
        this.restaurant = restaurant;
        this.amount = amount;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {

        return String.format(
                "Order ID: %-5s | Customer: %-10s | Restaurant: %-15s | Amount: ₹%-7.2f | Status: %s",
                orderId,
                customerName,
                restaurant,
                amount,
                status
        );
    }
}

// ======================================================
// 2. BUSINESS LOGIC + DATABASE
// ======================================================

class OrderTrackerLogic {

    // ==================================================
    // SUPABASE DATABASE CONFIGURATION
    // ==================================================

    private final String URL =
            "jdbc:postgresql://db.objvizdcqlqmhznznvsw.supabase.co:5432/postgres?user=postgres&password=1lvqf7aVGuUykYZC";

    private final String USER = "postgres";

    private final String PASSWORD = "1lvqf7aVGuUykYZC";

    // ==================================================
    // DATABASE CONNECTION
    // ==================================================

private Connection connect() {

    try {

        Connection con =
            DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
            );

        System.out.println(
            "Connected Successfully!"
        );

        return con;

    } catch(Exception e) {

        System.out.println(
            "DATABASE CONNECTION FAILED"
        );

        e.printStackTrace();

        return null;
    }
}

    // ==================================================
    // ADD ORDER INTO DATABASE
    // ==================================================

    public void addOrder(FoodOrder order) {

        String query =
                "INSERT INTO food_orders VALUES (?, ?, ?, ?, ?)";

        try (
                Connection con = connect();

                PreparedStatement ps =
                        con.prepareStatement(query)
        ) {

            ps.setString(1, order.getOrderId());
            ps.setString(2, order.getCustomerName());
            ps.setString(3, order.getRestaurant());
            ps.setDouble(4, order.getAmount());
            ps.setString(5, order.getStatus());

            ps.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ==================================================
    // GET ALL ORDERS FROM DATABASE
    // ==================================================

    public List<FoodOrder> getAllOrders() {

        List<FoodOrder> orders =
                new ArrayList<>();

        String query =
                "SELECT * FROM food_orders";

        try (

                Connection con = connect();

                Statement st =
                        con.createStatement();

                ResultSet rs =
                        st.executeQuery(query)

        ) {

            while (rs.next()) {

                FoodOrder order =
                        new FoodOrder(

                                rs.getString("order_id"),

                                rs.getString("customer_name"),

                                rs.getString("restaurant"),

                                rs.getDouble("amount"),

                                rs.getString("status")
                        );

                orders.add(order);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return orders;
    }

    // ==================================================
    // SORT ORDERS BY AMOUNT
    // ==================================================

   public List<FoodOrder> sortByAmount() {

    List<FoodOrder> orders =
            new ArrayList<>();

    String query =
            "SELECT * FROM food_orders ORDER BY amount ASC";

    try (

            Connection con = connect();

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(query)

    ) {

        while(rs.next()) {

            FoodOrder order =
                    new FoodOrder(

                            rs.getString("order_id"),

                            rs.getString("customer_name"),

                            rs.getString("restaurant"),

                            rs.getDouble("amount"),

                            rs.getString("status")
                    );

            orders.add(order);
        }

    } catch(Exception e) {

        e.printStackTrace();
    }

    return orders;
}
    // ==================================================
    // PREMIUM ORDERS
    // ==================================================

    public List<FoodOrder> getPremiumOrders() {

        List<FoodOrder> premium =
                new ArrayList<>();

        for (FoodOrder order : getAllOrders()) {

            if (order.getAmount() > 1000) {

                premium.add(order);
            }
        }

        return premium;
    }

    // ==================================================
    // TOTAL REVENUE
    // ==================================================

    public double calculateTotalRevenue() {

        double total = 0;

        for (FoodOrder order : getAllOrders()) {

            total += order.getAmount();
        }

        return total;
    }

    // ==================================================
    // RECEIPT MESSAGE
    // ==================================================

    public String generateReceiptMessage(
            FoodOrder order
    ) {

        StringBuffer sb =
                new StringBuffer();

        sb.append("====================================\n");
        sb.append("          ORDER RECEIPT             \n");
        sb.append("====================================\n");
        sb.append("Order #      : ")
                .append(order.getOrderId())
                .append("\n");

        sb.append("Customer     : ")
                .append(order.getCustomerName())
                .append("\n");

        sb.append("Restaurant   : ")
                .append(order.getRestaurant())
                .append("\n");

        sb.append("Order Status : ")
                .append(order.getStatus())
                .append("\n");

        sb.append("------------------------------------\n");

        sb.append("TOTAL AMOUNT : ₹")
                .append(order.getAmount())
                .append("\n");

        sb.append("====================================\n");

        return sb.toString();
    }
}

// ======================================================
// 3. SWING USER INTERFACE
// ======================================================

public class FoodDeliveryApp {

    private JFrame frame;

    private JTextField txtOrderId;
    private JTextField txtName;
    private JTextField txtRestaurant;
    private JTextField txtAmount;
    private JTextField txtStatus;

    private JTextArea displayArea;

    private JLabel lblRevenue;

    private OrderTrackerLogic trackerLogic;

    // ==================================================
    // UI STYLING
    // ==================================================

    private Font mainFont =
            new Font("SansSerif", Font.PLAIN, 14);

    private Font boldFont =
            new Font("SansSerif", Font.BOLD, 14);

    private Font receiptFont =
            new Font("Monospaced", Font.PLAIN, 13);

    private Color primaryColor =
            new Color(41, 128, 185);

    private Color bgColor =
            new Color(245, 246, 250);

    // ==================================================
    // CONSTRUCTOR
    // ==================================================

    public FoodDeliveryApp() {

        trackerLogic =
                new OrderTrackerLogic();

        initializeGUI();
    }

    // ==================================================
    // GUI INITIALIZATION
    // ==================================================

    private void initializeGUI() {

        frame =
                new JFrame(
                        "🍔 Premium Food Delivery Tracker"
                );

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        frame.setSize(750, 600);

        frame.setLayout(
                new BorderLayout(15, 15)
        );

        frame.getContentPane()
                .setBackground(bgColor);

        // ==================================================
        // HEADER
        // ==================================================

        JLabel headerLabel =
                new JLabel(
                        "Online Food Delivery Order Management",
                        JLabel.CENTER
                );

        headerLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        22
                )
        );

        headerLabel.setForeground(primaryColor);

        headerLabel.setBorder(
                new EmptyBorder(
                        15,
                        10,
                        5,
                        10
                )
        );

        frame.add(
                headerLabel,
                BorderLayout.NORTH
        );

        // ==================================================
        // CENTER PANEL
        // ==================================================

        JPanel centerPanel =
                new JPanel(
                        new BorderLayout(10, 10)
                );

        centerPanel.setBackground(bgColor);

        centerPanel.setBorder(
                new EmptyBorder(0, 15, 0, 15)
        );

        // ==================================================
        // INPUT PANEL
        // ==================================================

        JPanel inputPanel =
                new JPanel(
                        new GridLayout(5, 2, 10, 15)
                );

        inputPanel.setBackground(Color.WHITE);

        TitledBorder inputBorder =
                BorderFactory.createTitledBorder(
                        "📝 Enter New Order Details"
                );

        inputBorder.setTitleFont(boldFont);

        inputPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        inputBorder,
                        new EmptyBorder(10, 10, 10, 10)
                )
        );

        inputPanel.add(
                createStyledLabel("Order ID:")
        );

        txtOrderId =
                createStyledTextField();

        inputPanel.add(txtOrderId);

        inputPanel.add(
                createStyledLabel("Customer Name:")
        );

        txtName =
                createStyledTextField();

        inputPanel.add(txtName);

        inputPanel.add(
                createStyledLabel("Restaurant:")
        );

        txtRestaurant =
                createStyledTextField();

        inputPanel.add(txtRestaurant);

        inputPanel.add(
                createStyledLabel("Amount (₹):")
        );

        txtAmount =
                createStyledTextField();

        inputPanel.add(txtAmount);

        inputPanel.add(
                createStyledLabel("Current Status:")
        );

        txtStatus =
                createStyledTextField();

        inputPanel.add(txtStatus);

        centerPanel.add(
                inputPanel,
                BorderLayout.CENTER
        );

        // ==================================================
        // BUTTONS
        // ==================================================

        JPanel buttonPanel =
                new JPanel(
                        new GridLayout(4, 1, 10, 10)
                );

        buttonPanel.setBackground(bgColor);

        JButton btnAddOrder =
                createStyledButton(
                        "➕ Add Order",
                        new Color(46, 204, 113)
                );

        JButton btnShowAll =
                createStyledButton(
                        "📋 Show All Orders",
                        primaryColor
                );

        JButton btnSort =
                createStyledButton(
                        "↕️ Sort by Amount",
                        primaryColor
                );

        JButton btnPremium =
                createStyledButton(
                        "💎 Show > ₹1000",
                        new Color(155, 89, 182)
                );

        buttonPanel.add(btnAddOrder);
        buttonPanel.add(btnShowAll);
        buttonPanel.add(btnSort);
        buttonPanel.add(btnPremium);

        centerPanel.add(
                buttonPanel,
                BorderLayout.EAST
        );

        frame.add(
                centerPanel,
                BorderLayout.CENTER
        );

        // ==================================================
        // OUTPUT PANEL
        // ==================================================

        JPanel bottomPanel =
                new JPanel(
                        new BorderLayout(5, 5)
                );

        bottomPanel.setBackground(bgColor);

        bottomPanel.setBorder(
                new EmptyBorder(0, 15, 15, 15)
        );

        displayArea =
                new JTextArea();

        displayArea.setEditable(false);

        displayArea.setFont(receiptFont);

        displayArea.setBackground(
                new Color(253, 253, 253)
        );

        JScrollPane scrollPane =
                new JScrollPane(displayArea);

        TitledBorder outputBorder =
                BorderFactory.createTitledBorder(
                        "💻 System Output Console"
                );

        outputBorder.setTitleFont(boldFont);

        scrollPane.setBorder(outputBorder);

        scrollPane.setPreferredSize(
                new Dimension(700, 200)
        );

        bottomPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        // ==================================================
        // REVENUE LABEL
        // ==================================================

        lblRevenue =
                new JLabel(
                        String.format(
                                "Total Platform Revenue: ₹%.2f",
                                trackerLogic.calculateTotalRevenue()
                        )
                );

        lblRevenue.setFont(boldFont);

        lblRevenue.setForeground(
                new Color(192, 57, 43)
        );

        bottomPanel.add(
                lblRevenue,
                BorderLayout.SOUTH
        );

        frame.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        // ==================================================
        // ACTION LISTENERS
        // ==================================================

        btnAddOrder.addActionListener(e -> {

            try {

                String id =
                        txtOrderId.getText().trim();

                String name =
                        txtName.getText().trim();

                String restaurant =
                        txtRestaurant.getText().trim();

                String status =
                        txtStatus.getText().trim();

                if (
                        id.isEmpty()
                                || name.isEmpty()
                                || restaurant.isEmpty()
                                || status.isEmpty()
                ) {

                    throw new IllegalArgumentException(
                            "All fields must be filled out."
                    );
                }

                double amount =
                        Double.parseDouble(
                                txtAmount.getText().trim()
                        );

                if (amount <= 0) {

                    throw new IllegalArgumentException(
                            "Amount must be greater than 0."
                    );
                }

                FoodOrder newOrder =
                        new FoodOrder(
                                id,
                                name,
                                restaurant,
                                amount,
                                status
                        );

                trackerLogic.addOrder(newOrder);

                displayArea.setText(
                        "✅ Order Added Successfully!\n\n"
                );

                displayArea.append(
                        trackerLogic.generateReceiptMessage(
                                newOrder
                        )
                );

                lblRevenue.setText(
                        String.format(
                                "Total Platform Revenue: ₹%.2f",
                                trackerLogic.calculateTotalRevenue()
                        )
                );

                txtOrderId.setText("");
                txtName.setText("");
                txtRestaurant.setText("");
                txtAmount.setText("");
                txtStatus.setText("");

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Amount must be numeric.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (IllegalArgumentException ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        ex.getMessage(),
                        "Input Error",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        // ==================================================
        // SHOW ALL
        // ==================================================

        btnShowAll.addActionListener(e -> {

            displayOrders(
                    trackerLogic.getAllOrders(),
                    "--- ALL REGISTERED ORDERS ---"
            );
        });

        // ==================================================
        // SORT
        // ==================================================

        btnSort.addActionListener(e -> {

            displayOrders(
                    trackerLogic.sortByAmount(),
                    "--- ORDERS SORTED BY AMOUNT ---"
            );
        });

        // ==================================================
        // PREMIUM
        // ==================================================

        btnPremium.addActionListener(e -> {

            displayOrders(
                    trackerLogic.getPremiumOrders(),
                    "--- PREMIUM ORDERS (> ₹1000) ---"
            );
        });

        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }

    // ==================================================
    // UI HELPERS
    // ==================================================

    private JLabel createStyledLabel(String text) {

        JLabel label =
                new JLabel(text);

        label.setFont(mainFont);

        return label;
    }

    private JTextField createStyledTextField() {

        JTextField field =
                new JTextField();

        field.setFont(mainFont);

        return field;
    }

    private JButton createStyledButton(
            String text,
            Color bgColor
    ) {

        JButton button =
                new JButton(text);

        button.setFont(boldFont);

        button.setBackground(bgColor);

        button.setForeground(Color.WHITE);

        button.setFocusPainted(false);

        button.setOpaque(true);

        button.setBorderPainted(false);

        return button;
    }

    // ==================================================
    // DISPLAY ORDERS
    // ==================================================

    private void displayOrders(
            List<FoodOrder> orders,
            String title
    ) {

        if (orders.isEmpty()) {

            displayArea.setText(
                    title +
                            "\n\n⚠️ No orders found."
            );

            return;
        }

        StringBuilder sb =
                new StringBuilder(
                        title + "\n\n"
                );

        for (FoodOrder order : orders) {

            sb.append(order.toString())
                    .append("\n");
        }

        displayArea.setText(sb.toString());
    }

    // ==================================================
    // MAIN METHOD
    // ==================================================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new FoodDeliveryApp();

        });
    }
}