package OOP.Introduce;

public class Product {
    String id;
    String name;
    double price;
    public void displayInfo() {
        System.out.println("San pham + " + name);
        System.out.println("Gia san pham + " + String.format("%.2f", price) + "VND");
    }
}
