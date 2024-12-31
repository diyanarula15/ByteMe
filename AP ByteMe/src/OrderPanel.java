import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OrderPanel extends JPanel {
    private ByteMeSystem system;
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton, updateStatusButton;

    public OrderPanel(ByteMeSystem system) {
        this.system = system;

        // Set layout
        setLayout(new BorderLayout());

        // Create table model
        String[] columnNames = {"Order ID", "User", "Total Price", "Status", "Items"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // Create table
        orderTable = new JTable(tableModel);
        orderTable.setFillsViewportHeight(true);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add action buttons
        JPanel buttonPanel = new JPanel();
        refreshButton = new JButton("Refresh");
        updateStatusButton = new JButton("Update Order Status");

        buttonPanel.add(refreshButton);
        buttonPanel.add(updateStatusButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Add button action listeners
        refreshButton.addActionListener(e -> refreshOrders());
        updateStatusButton.addActionListener(e -> updateOrderStatus());

        // Populate orders
        populateOrders();
    }

    private void populateOrders() {
        tableModel.setRowCount(0);
        for (Order order : system.getPendingOrders()) {
            Object[] row = {
                    order.getOrderId(),
                    system.getUserById(order.getUserId()).getName(), // Use getName() here
                    String.format("₹%.2f", order.getTotalAmount()),
                    order.getStatus(),
                    getOrderItemsString(order)
            };
            tableModel.addRow(row);
        }
    }


    private String getOrderItemsString(Order order) {
        // Convert order items to readable string
        StringBuilder itemsBuilder = new StringBuilder();
        order.getItems().forEach((item, quantity) ->
                itemsBuilder.append(item.getName())
                        .append(" (Qty: ").append(quantity).append("), ")
        );
        return itemsBuilder.length() > 0 ? itemsBuilder.substring(0, itemsBuilder.length() - 2) : "No items";
    }


    public void refreshOrders() {
        populateOrders();
    }

    public void updateOrderStatus() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an order to update!");
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);
        int userId = (int) tableModel.getValueAt(selectedRow, 1);
        User user = system.getUserById(userId);

        if (!user.getRole().equalsIgnoreCase("staff")) {
            JOptionPane.showMessageDialog(this, "Only staff members can update order status.");
            return;
        }

        String newStatus = JOptionPane.showInputDialog(this, "Enter new status (Pending/Completed/Cancelled):");

        if (newStatus != null && !newStatus.isBlank()) {
            boolean updated = system.updateOrderStatus(orderId, newStatus, user);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Order status updated!");
                refreshOrders();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update order status.");
            }
        }
    }
}
