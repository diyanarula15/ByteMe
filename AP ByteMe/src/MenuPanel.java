import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class MenuPanel extends JPanel {
    private ByteMeSystem system;
    private JTable menuTable;
    private DefaultTableModel tableModel;

    public MenuPanel(ByteMeSystem system) {
        this.system = system;

        // Set layout
        setLayout(new BorderLayout());

        // Create table model with appropriate column names
        String[] columnNames = {"Item ID", "Item Name", "Price", "Availability", "Category"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // Create and configure table
        menuTable = new JTable(tableModel);
        menuTable.setFillsViewportHeight(true);

        // Add table to scroll pane and then to the panel
        JScrollPane scrollPane = new JScrollPane(menuTable);
        add(scrollPane, BorderLayout.CENTER);

        // Populate the menu
        populateMenu();
    }

    private void populateMenu() {
        tableModel.setRowCount(0);

        for (Item item : system.getMenu()) {
            Object[] row = {
                    item.getName(),
                    String.format("₹%.2f", item.getPrice()),
                    item.getQuantity(),
                    item.getDescription()
            };
            tableModel.addRow(row);
        }
    }



    // Method to refresh the menu table (can be called after updates in CLI)
    public void refreshMenu() {
        populateMenu();
    }
}

