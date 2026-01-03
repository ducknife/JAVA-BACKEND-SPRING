package JAVA_ADVANCED.StreamAPI;

/**
 * Class Product dùng cho các ví dụ Stream API
 */
public class Product {
    private int id;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private boolean inStock;

    // Constructor
    public Product(int id, String name, String category, double price, int quantity, boolean inStock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.inStock = inStock;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public boolean isInStock() { return inStock; }

    // Setters
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', category='%s', price=%.2f, qty=%d, inStock=%s}",
                id, name, category, price, quantity, inStock);
    }
}
