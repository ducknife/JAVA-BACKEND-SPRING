package OOP.Encapsulation;

public class Product {
    private String name;
    private double price;

    public Product() {

    }

    public void displayInfo() {
        System.out.println("San pham + " + name);
        System.out.println("Gia san pham + " + String.format("%.2f", price) + "VND");
    }

    public String getName() {
        return name;
    }

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

}