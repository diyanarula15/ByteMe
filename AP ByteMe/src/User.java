import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String name;
    private String password;
    private String role;
    private boolean isVIP;
    private List<Order> orderHistory;

    public User(int id, String name, String password, String role, boolean isVIP) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
        this.isVIP = isVIP;
        this.orderHistory = new ArrayList<>();
    }


    public int getId() { return id; }
    public String getName() { return name; } // Use this to get the user's name
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public boolean isVIP() { return isVIP; }
    public void setVIP(boolean isVIP) { this.isVIP = isVIP; }
    public List<Order> getOrderHistory() { return orderHistory; }
    public void addOrder(Order order) { orderHistory.add(order); }
}
