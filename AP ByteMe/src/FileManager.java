import java.io.*;
import java.util.*;

public class FileManager {
    private static final String ORDER_HISTORY_FILE = "assets/order_history.txt";
    private static final String USERS_FILE = "assets/users.txt";
    private static final String CART_FILE = "assets/cart.txt";
    private static final String MENU_FILE = "assets/menu.txt";
    private final ByteMeSystem system;

    public FileManager(ByteMeSystem system) {
        this.system = system;
    }

    // Save user order history
    public void saveUserOrderHistory(User user, Order order) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDER_HISTORY_FILE, true))) {
            writer.write(String.format("%d,%s,%f,%s,%s",
                    order.getOrderId(),
                    user.getName(),
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getLocation()));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving order history: " + e.getMessage());
        }
    }

    public void updateOrderStatus(int orderId, String newStatus) {
        try {
            // Read existing orders
            List<String> updatedOrders = new ArrayList<>();
            boolean orderFound = false;

            try (BufferedReader reader = new BufferedReader(new FileReader("orders.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    int currentOrderId = Integer.parseInt(parts[0].trim());

                    // If the orderId matches, update the status
                    if (currentOrderId == orderId) {
                        // Reconstruct the line with new status
                        line = String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                                currentOrderId,
                                parts[1],   // userId
                                newStatus,  // Updated status
                                parts[3],   // totalAmount
                                parts[4],   // orderTime
                                parts[5],   // deliveryLocation
                                parts[6],   // specialRequest
                                parts[7]    // isVIP
                        );
                        orderFound = true;
                    }
                    updatedOrders.add(line);
                }
            }

            // If order not found, throw an exception
            if (!orderFound) {
                throw new IllegalArgumentException("Order not found");
            }

            // Write updated orders back to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("orders.txt"))) {
                for (String orderLine : updatedOrders) {
                    writer.write(orderLine);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating orders file: " + e.getMessage());
        }
    }
    // Load user order history
    public List<Order> loadUserOrderHistory(User user) {
        List<Order> orders = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDER_HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[1].equals(user.getName())) {
                    Order order = new Order(user.getId(), parts[4], parts[3], user.isVIP());
                    order.setOrderId(Integer.parseInt(parts[0]));
                    order.setTotalAmount(Double.parseDouble(parts[2]));
                    orders.add(order);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading order history: " + e.getMessage());
        }
        return orders;
    }

    // Save user data
    public void saveUserData(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(String.format("%d,%s,%s,%s,%b",
                    user.getId(),
                    user.getName(),
                    user.getPassword(),
                    user.getRole(),
                    user.isVIP()));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }

    // Load all users
    public Map<Integer, User> loadUsers() {
        Map<Integer, User> users = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                String password = parts[2];
                String role = parts[3];
                boolean isVIP = Boolean.parseBoolean(parts[4]);
                users.put(id, new User(id, name, password, role, isVIP));
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    // Save cart
    public void saveTemporaryCart(Map<Item, Integer> cart) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CART_FILE))) {
            for (Map.Entry<Item, Integer> entry : cart.entrySet()) {
                writer.write(String.format("%d,%d,%f",
                        entry.getKey().getId(),
                        entry.getValue(),
                        entry.getKey().getPrice()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving cart: " + e.getMessage());
        }
    }

    // Load cart
    public Map<Item, Integer> loadTemporaryCart() {
        Map<Item, Integer> cart = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CART_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int itemId = Integer.parseInt(parts[0]);
                int quantity = Integer.parseInt(parts[1]);
                Item item = system.getMenuItemById(itemId);
                if (item != null) {
                    cart.put(item, quantity);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading cart: " + e.getMessage());
        }
        return cart;
    }

    // Save all users
    public void saveUsers(Map<Integer, User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User user : users.values()) {
                writer.write(String.format("%d,%s,%s,%s,%b",
                        user.getId(),
                        user.getName(),
                        user.getPassword(),
                        user.getRole(),
                        user.isVIP()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    // Load menu
    public Map<Integer, Item> loadMenu() {
        Map<Integer, Item> menu = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(MENU_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                Item item = new Item(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        Double.parseDouble(parts[2]),
                        parts[3],
                        Integer.parseInt(parts[4]),
                        parts[5]
                );
                menu.put(item.getId(), item);
            }
        } catch (IOException e) {
            System.err.println("Error loading menu: " + e.getMessage());
        }
        return menu;
    }

    // Save menu
    public void saveMenu(Map<Integer, Item> menu) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MENU_FILE))) {
            for (Item item : menu.values()) {
                writer.write(String.format("%d,%s,%f,%s,%d,%s",
                        item.getId(),
                        item.getName(),
                        item.getPrice(),
                        item.getCategory(),
                        item.getQuantity(),
                        item.getDescription()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving menu: " + e.getMessage());
        }
    }

    // Save all orders
    public void saveOrders(List<Order> orders) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDER_HISTORY_FILE))) {
            for (Order order : orders) {
                User user = system.getUserById(order.getUserId());
                writer.write(String.format("%d,%s,%f,%s,%s",
                        order.getOrderId(),
                        user.getName(),
                        order.getTotalAmount(),
                        order.getStatus(),
                        order.getLocation()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving orders: " + e.getMessage());
        }
    }

    public List<Order> loadOrders() {
        List<Order> orders = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(ORDER_HISTORY_FILE))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int orderId = Integer.parseInt(parts[0]);
                String userName = parts[1];
                double totalAmount = Double.parseDouble(parts[2]);
                String status = parts[3];
                String location = parts[4];

                // Retrieve the user associated with this order
                User user = system.getUserByName(userName);
                if (user != null) {
                    // Create and populate the Order object
                    Order order = new Order(orderId, location, status, user.isVIP());
                    order.setTotalAmount(totalAmount);
                    orders.add(order);
                } else {
                    System.err.println("Warning: User not found for order ID " + orderId);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }

        return orders;
    }

}
