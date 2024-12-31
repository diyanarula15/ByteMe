import java.util.ArrayList;
import java.util.List;

class Item {
    private int id;
    private String name;
    private double price;
    private String category;
    private boolean available;
    private List<String> reviews;
    private int quantity;        // Added quantity field
    private String description;  // Added description field

    public Item(int id, String name, double price, String category, int quantity, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.quantity = quantity;
        this.description = description;
        this.available = true;
        this.reviews = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public List<String> getReviews() { return reviews; }
    public void addReview(String review) { reviews.add(review); }
    public int getQuantity() { return quantity; }                // Added getter for quantity
    public void setQuantity(int quantity) { this.quantity = quantity; }  // Optional setter for quantity
    public String getDescription() { return description; }       // Added getter for description

    @Override
    public String toString() {
        return String.format("%-5d %-20s ₹%.2f %-15s %s",
                id, name, price, category, available ? "Available" : "Not Available");
    }
}

