import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Order {
    private static int orderCounter = 1000;
    private int orderId;
    private int userId;
    private Map<Item, Integer> items;
    private double totalAmount;
    private LocalDateTime orderTime;
    private String status;
    private String deliveryLocation;
    private String specialRequest;
    private boolean isVIP;

    public Order(int userId, String deliveryLocation, String specialRequest, boolean isVIP) {
        this.orderId = ++orderCounter;
        this.userId = userId;
        this.items = new HashMap<>();
        this.totalAmount = 0.0;
        this.orderTime = LocalDateTime.now();
        this.status = "Pending";
        this.deliveryLocation = deliveryLocation;
        this.specialRequest = specialRequest;
        this.isVIP = isVIP;
    }

    public void addItem(Item item, int quantity) {
        items.put(item, quantity);
        totalAmount += item.getPrice() * quantity;
    }

    public int getOrderId() { return orderId; }
    public int getUserId() { return userId; }
    public Map<Item, Integer> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isVIP() { return isVIP; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nOrder ID: ").append(orderId)
                .append("\nOrder Time: ").append(orderTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\nStatus: ").append(status)
                .append("\nDelivery Location: ").append(deliveryLocation)
                .append("\nSpecial Request: ").append(specialRequest)
                .append("\n\nItems:");

        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            sb.append("\n").append(entry.getValue()).append("x ")
                    .append(entry.getKey().getName())
                    .append(" (₹").append(entry.getKey().getPrice()).append(" each)");
        }
        sb.append("\n\nTotal Amount: ₹").append(String.format("%.2f", totalAmount));
        return sb.toString();
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public Object getLocation() {
        return deliveryLocation;
    }
}