package OOP.Polymophism;

public class CashPayment implements IPayment{
    @Override
    public void pay(double amount) {
        System.out.println("Da thanh toan " + amount + "bang Tien Mat");
    }
}

// từ khóa implements giống như kí vào hợp đồng, để triển khai các giao diện.