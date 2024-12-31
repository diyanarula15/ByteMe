import java.awt.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;

public class ByteMeSystem {
    private Map<Integer, Item> menu;
    private Map<Integer, User> users;
    private List<Order> activeOrders;
    private Scanner scanner;
    private ExecutorService guiExecutor; // Executor for GUI thread
    private JFrame guiFrame;
    private FileManager fileManager;
    private Map<Item, Integer> cart;// Store the GUI frame instance

    public ByteMeSystem() {
        this.menu = new HashMap<>();
        this.users = new HashMap<>();
        this.activeOrders = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.guiExecutor = Executors.newSingleThreadExecutor(); // Initialize single-threaded executor
        this.fileManager = new FileManager(this); // Initialize FileManager
        initializeData();
    }

    private void initializeData() {
        this.menu = fileManager.loadMenu();
        this.users = fileManager.loadUsers();
        this.activeOrders = fileManager.loadOrders();

        // If files are empty, populate default data
        if (menu.isEmpty()) {
            menu.put(1, new Item(1, "Veg Biryani", 120.0, "Main Course", 10, "Delicious Veg Biryani with spices"));
            menu.put(2, new Item(2, "Chicken Burger", 80.0, "Snacks", 15, "Juicy chicken burger with fresh lettuce"));
            menu.put(3, new Item(3, "Masala Dosa", 60.0, "Breakfast", 20, "Crispy dosa with spicy masala filling"));
            menu.put(4, new Item(4, "Cold Coffee", 40.0, "Beverages", 25, "Chilled coffee with ice cream"));
        }

        if (users.isEmpty()) {
            users.put(101, new User(101, "student1", "pass123", "student", false));
            users.put(102, new User(102, "staff1", "admin123", "staff", true));
        }
    }

    public void showGUI() {
        // Check if GUI is already running
        if (guiFrame != null && guiFrame.isVisible()) {
            System.out.println("GUI is already running.");
            return;
        }

        guiExecutor.submit(() -> {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    guiFrame = new JFrame("Byte Me - Canteen Management System");
                    guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    guiFrame.setSize(800, 600);

                    CardLayout cardLayout = new CardLayout();
                    JPanel mainPanel = new JPanel(cardLayout);

                    MenuPanel menuPanel = new MenuPanel(this);
                    OrderPanel orderPanel = new OrderPanel(this);

                    mainPanel.add(menuPanel, "Menu");
                    mainPanel.add(orderPanel, "Orders");

                    JPanel navigationPanel = new JPanel();
                    JButton menuButton = new JButton("Menu");
                    JButton ordersButton = new JButton("Orders");

                    menuButton.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
                    ordersButton.addActionListener(e -> cardLayout.show(mainPanel, "Orders"));

                    navigationPanel.add(menuButton);
                    navigationPanel.add(ordersButton);

                    guiFrame.setLayout(new BorderLayout());
                    guiFrame.add(navigationPanel, BorderLayout.NORTH);
                    guiFrame.add(mainPanel, BorderLayout.CENTER);

                    // Listen for the window closing event
                    guiFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            System.out.println("\nReturning to CLI...");
                            guiFrame.dispose();
                            guiFrame = null; // Reset the GUI frame
                            start(); // Resume CLI
                        }
                    });

                    guiFrame.setVisible(true);
                });
            } catch (Exception e) {
                System.out.println("Error launching GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void start() {
        while (true) {
            System.out.println("\n=== Welcome to Byte Me! ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Launch GUI");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> login();
                case 2 -> register();
                case 3 -> {
                    System.out.println("Launching GUI...");
                    showGUI(); // Call the GUI display method
                    return; // Exit the CLI loop
                }
                case 4 -> {
                    System.out.println("Thank you for using Byte Me!");
                    guiExecutor.shutdown(); // Shutdown the GUI thread pool
                    return;
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    public void login() {
        System.out.print("Enter ID: ");
        int id = readInt();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = users.get(id);

        if (user != null && user.getPassword().equals(password)) {
            if (user.getRole().equals("student"))
                studentMenu(user);
            else
                staffMenu(user);
        } else {
            System.out.println("Invalid credentials!");
        }
    }

    private void register() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Role (student/staff): ");
        String role = scanner.nextLine().toLowerCase();

        if (!role.equals("student") && !role.equals("staff")) {
            System.out.println("Invalid role!");
            return;
        }

        System.out.print("Are you a VIP? (yes/no): ");
        String vipResponse = scanner.nextLine().toLowerCase();
        boolean isVIP = vipResponse.equals("yes");

        int id = users.size() + 101;
        users.put(id, new User(id, name, password, role, isVIP));

        System.out.println("Registration successful! Your ID is: " + id);
        if (isVIP) {
            System.out.println("Welcome to the VIP club! Enjoy your perks.");
        }

        // Save updated users to file
        fileManager.saveUsers(users);
    }

    private void searchMenu() {
        System.out.print("Enter item name or keyword to search: ");
        String keyword = scanner.nextLine().toLowerCase();
        System.out.println("\n=== Search Results ===");
        menu.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(keyword))
                .forEach(System.out::println);
    }

    private void filterMenuByCategory() {
        System.out.print("Enter category to filter: ");
        String category = scanner.nextLine().toLowerCase();
        System.out.println("\n=== Filtered Results ===");
        menu.values().stream()
                .filter(item -> item.getCategory().toLowerCase().equals(category))
                .forEach(System.out::println);
    }

    private void sortMenuByPrice() {
        System.out.println("\n=== Menu Sorted by Price ===");
        menu.values().stream()
                .sorted(Comparator.comparingDouble(Item::getPrice))
                .forEach(System.out::println);
    }

    private void cancelOrder(User user) {
        List<Order> userActiveOrders = activeOrders.stream()
                .filter(order -> order.getUserId() == user.getId() &&
                        order.getStatus().equals("Pending"))
                .toList();

        if (userActiveOrders.isEmpty()) {
            System.out.println("No active orders available for cancellation.");
            return;
        }

        System.out.println("\n=== Active Orders Available for Cancellation ===");
        userActiveOrders.forEach(System.out::println);

        System.out.print("Enter order ID to cancel: ");
        int orderId = readInt();

        activeOrders.stream()
                .filter(order -> order.getOrderId() == orderId &&
                        order.getUserId() == user.getId() &&
                        order.getStatus().equals("Pending"))
                .findFirst()
                .ifPresentOrElse(order -> {
                    order.setStatus("Cancelled");
                    System.out.println("Order cancelled successfully.");

                    // Save updated orders to file
                    fileManager.saveOrders(activeOrders);
                }, () -> System.out.println("Invalid order ID or order cannot be cancelled."));
    }


    private void studentMenu(User user) {
        while (true) {
            System.out.println("\n=== Student Menu ===");
            System.out.println("1. View Menu");
            System.out.println("2. Search Menu");
            System.out.println("3. Filter Menu by Category");
            System.out.println("4. Sort Menu by Price");
            System.out.println("5. Place Order");
            System.out.println("6. View Order History");
            System.out.println("7. Track Active Order");
            System.out.println("8. Cancel Order");
            System.out.println("9. Become VIP");
            System.out.println("10. Provide Review");
            System.out.println("11. View Reviews");
            System.out.println("12. Logout");
            System.out.print("Choose an option: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> displayMenu();
                case 2 -> searchMenu();
                case 3 -> filterMenuByCategory();
                case 4 -> sortMenuByPrice();
                case 5 -> placeOrder(user);
                case 6 -> viewOrderHistory(user);
                case 7 -> trackActiveOrder(user);
                case 8 -> cancelOrder(user);
                case 9 -> becomeVIP(user);
                case 10 -> provideReview(user);
                case 11 -> viewReviews();
                case 12 -> { return; }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private void staffMenu(User user) {
        while (true) {
            System.out.println("\n=== Staff Menu ===");
            System.out.println("1. View Menu");
            System.out.println("2. Add Menu Item");
            System.out.println("3. Update Item Availability");
            System.out.println("4. Remove Menu Item");
            System.out.println("5. View Active Orders");
            System.out.println("6. Update Order Status");
            System.out.println("7. Process Refunds");
            System.out.println("8. Generate Daily Sales Report");
            System.out.println("9. Logout");
            System.out.print("Choose an option: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> displayMenu();
                case 2 -> addMenuItem();
                case 3 -> updateItemAvailability();
                case 4 -> removeMenuItem();
                case 5 -> viewActiveOrders();
                case 6 -> updateOrderStatus();
                case 7 -> processRefunds();
                case 8 -> generateDailySalesReport();
                case 9 -> { return; }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private void updateOrderStatus() {
        System.out.print("Enter order ID to update status: ");
        int orderId = readInt();
        System.out.print("Enter new status (Pending/Completed/Cancelled): ");
        String newStatus = scanner.nextLine();

        Optional<Order> orderOptional = activeOrders.stream()
                .filter(order -> order.getOrderId() == orderId)
                .findFirst();

        orderOptional.ifPresentOrElse(order -> {
            order.setStatus(newStatus);
            System.out.println("Order status updated.");

            // Save updated orders to file
            fileManager.saveOrders(activeOrders);

        }, () -> System.out.println("Order not found."));
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return -1;
        }
    }

    private void displayMenu() {
        System.out.println("\n=== Menu ===");
        menu.values().forEach(System.out::println);
    }

    private void addMenuItem() {
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();
        System.out.print("Enter item price: ");
        double price = readDouble();
        System.out.print("Enter category: ");
        String category = scanner.nextLine();
        System.out.print("Enter Description: ");
        String description = scanner.nextLine();

        int id = menu.size() + 1;
        int quantity = 10;

        menu.put(id, new Item(id, name, price, category, quantity, description));
        System.out.println("Item added successfully!");

        // Save updated menu to file
        fileManager.saveMenu(menu);
    }


    private double readDouble() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return -1;
        }
    }

    private void updateItemAvailability() {
        displayMenu();
        System.out.print("Enter item ID to update: ");
        int itemId = readInt();
        Item item = menu.get(itemId);

        if (item != null) {
            item.setAvailable(!item.isAvailable());
            System.out.println("Item availability updated!");

            // Save updated menu to file
            fileManager.saveMenu(menu);
        } else {
            System.out.println("Invalid item ID!");
        }
    }


    private void removeMenuItem() {
        displayMenu();
        System.out.print("Enter item ID to remove: ");
        int itemId = readInt();

        if (menu.containsKey(itemId)) {
            menu.remove(itemId);
            System.out.println("Item removed successfully!");

            // Save updated menu to file
            fileManager.saveMenu(menu);
        } else {
            System.out.println("Invalid item ID!");
        }
    }


    public void placeOrder(User user) {
        Map<Item, Integer> cart = new HashMap<>();

        while (true) {
            System.out.println("\n=== Cart ===");
            if (!cart.isEmpty()) {
                displayCart(cart);
                System.out.println("\nTotal: ₹" + calculateTotal(cart));
            } else {
                System.out.println("Cart is empty");
            }

            System.out.println("\n1. Add item to cart");
            System.out.println("2. Modify quantity");
            System.out.println("3. Remove item from cart");
            System.out.println("4. Proceed to checkout");
            System.out.println("5. Cancel order");
            System.out.print("Choose an option: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> addToCart(cart);
                case 2 -> modifyCartQuantity(cart);
                case 3 -> removeFromCart(cart);
                case 4 -> {
                    if (!cart.isEmpty()) {
                        checkout(user, cart);
                        return;
                    } else {
                        System.out.println("Cart is empty!");
                    }
                }
                case 5 -> { return; }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private void displayCart(Map<Item, Integer> cart) {
        cart.forEach((item, quantity) ->
                System.out.printf("%-20s x%d (₹%.2f each)\n",
                        item.getName(), quantity, item.getPrice()));
    }

    private double calculateTotal(Map<Item, Integer> cart) {
        return cart.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();
    }


    private void addToCart(Map<Item, Integer> cart) {
        displayMenu();
        System.out.print("Enter item ID to add: ");
        int itemId = readInt();

        Item item = menu.get(itemId);
        if (item == null || !item.isAvailable()) {
            System.out.println("Invalid or unavailable item!");
            return;
        }

        System.out.print("Enter quantity: ");
        int quantity = readInt();
        if (quantity > 0) {
            cart.put(item, cart.getOrDefault(item, 0) + quantity);
            System.out.println("Item added to cart.");
        }
    }

    private void modifyCartQuantity(Map<Item, Integer> cart) {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty!");
            return;
        }

        displayCart(cart);
        System.out.print("Enter item ID to modify: ");
        int itemId = readInt();

        cart.entrySet().stream()
                .filter(entry -> entry.getKey().getId() == itemId)
                .findFirst()
                .ifPresentOrElse(entry -> {
                    System.out.print("Enter new quantity (0 to remove): ");
                    int quantity = readInt();
                    if (quantity > 0) {
                        cart.put(entry.getKey(), quantity);
                        System.out.println("Quantity updated.");
                    } else if (quantity == 0) {
                        cart.remove(entry.getKey());
                        System.out.println("Item removed from cart.");
                    }
                }, () -> System.out.println("Item not found in cart!"));
    }

    private void removeFromCart(Map<Item, Integer> cart) {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty!");
            return;
        }

        displayCart(cart);
        System.out.print("Enter item ID to remove: ");
        int itemId = readInt();

        cart.entrySet().removeIf(entry -> {
            if (entry.getKey().getId() == itemId) {
                System.out.println("Item removed from cart.");
                return true;
            }
            return false;
        });
    }

    private void checkout(User user, Map<Item, Integer> cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }

        System.out.print("Enter delivery location(hostel no): ");
        String location = scanner.nextLine();
        System.out.print("Enter any special requests: ");
        String specialRequest = scanner.nextLine();

        Order order = new Order(user.getId(), location, specialRequest, user.isVIP());
        cart.forEach(order::addItem);
        activeOrders.add(order);
        user.addOrder(order);

        // Save updated orders to file
        fileManager.saveOrders(activeOrders);

        System.out.println("Order placed successfully!");
    }


    private void viewOrderHistory(User user) {
        System.out.println("\n=== Order History ===");
        user.getOrderHistory().forEach(System.out::println);
    }

    private void trackActiveOrder(User user) {
        List<Order> activeUserOrders = activeOrders.stream()
                .filter(order -> order.getUserId() == user.getId())
                .toList();

        if (activeUserOrders.isEmpty()) {
            System.out.println("No active orders.");
        } else {
            activeUserOrders.forEach(System.out::println);
        }
    }

    private void becomeVIP(User user) {
        System.out.print("Confirm VIP membership for ₹500? (yes/no): ");
        if (scanner.nextLine().equalsIgnoreCase("yes")) {
            user.setVIP(true);
            System.out.println("You are now a VIP member!");
        }
    }

    private void provideReview(User user) {
        System.out.print("Enter item ID to review: ");
        int itemId = readInt();
        Item item = menu.get(itemId);

        if (item != null) {
            System.out.print("Enter your review: ");
            String review = scanner.nextLine();
            item.addReview(review);
            System.out.println("Review added!");
        } else {
            System.out.println("Invalid item ID!");
        }
    }

    private void viewReviews() {
        System.out.print("Enter item ID to view reviews: ");
        int itemId = readInt();
        Item item = menu.get(itemId);

        if (item != null) {
            System.out.println("\n=== Reviews for " + item.getName() + " ===");
            item.getReviews().forEach(System.out::println);
        } else {
            System.out.println("Invalid item ID!");
        }
    }

    private void viewActiveOrders() {
        System.out.println("\n=== Active Orders ===");
        activeOrders.stream()
                .sorted((o1, o2) -> Boolean.compare(o2.isVIP(), o1.isVIP()))
                .forEach(System.out::println);
    }

    public boolean updateOrderStatus(int orderId, String newStatus, User user) {
        if (!user.getRole().equalsIgnoreCase("staff")) {
            System.out.println("Access denied! Only staff can update order statuses.");
            return false;
        }

        if (!newStatus.equalsIgnoreCase("Pending") &&
                !newStatus.equalsIgnoreCase("Completed") &&
                !newStatus.equalsIgnoreCase("Cancelled")) {
            System.out.println("Invalid status! Please enter Pending, Completed, or Cancelled.");
            return false;
        }

        return activeOrders.stream()
                .filter(order -> order.getOrderId() == orderId)
                .findFirst()
                .map(order -> {
                    order.setStatus(newStatus);
                    System.out.println("Order status updated to: " + newStatus);
                    return true;
                })
                .orElseGet(() -> {
                    System.out.println("Order not found.");
                    return false;
                });
    }

    private void processRefunds() {
        System.out.print("Enter order ID to process refund: ");
        int orderId = readInt();

        activeOrders.stream()
                .filter(order -> order.getOrderId() == orderId && order.getStatus().equals("Completed"))
                .findFirst()
                .ifPresentOrElse(order -> {
                    order.setStatus("Refunded");
                    System.out.println("Refund processed successfully.");
                }, () -> System.out.println("Refund not available for this order."));
    }
    private void generateDailySalesReport() {
        System.out.println("\n=== Daily Sales Report ===");
        double totalSales = activeOrders.stream()
                .filter(order -> order.getStatus().equals("Completed"))
                .mapToDouble(Order::getTotalAmount)
                .sum();

        System.out.println("Total Sales: ₹" + String.format("%.2f", totalSales));
        activeOrders.stream()
                .filter(order -> order.getStatus().equals("Completed"))
                .sorted((o1, o2) -> Boolean.compare(o2.isVIP(), o1.isVIP()))
                .forEach(System.out::println);
    }
    public Collection<Item> getMenu() {
        return menu.values();
    }
    public List<Order> getPendingOrders() {
        return activeOrders.stream()
                .filter(order -> order.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }
    public User getUserById(int userId) {
        return users.get(userId);
    }
    public Item getMenuItemById(int itemId) {
        return menu.get(itemId);
    }

    public User getUserByName(String userName) {
        return null;
    }
}