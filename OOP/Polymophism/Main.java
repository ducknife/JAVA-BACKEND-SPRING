package OOP.Polymophism;

public class Main {
    public static void main(String[] args) {
        MomoPayment mp = new MomoPayment();
        mp.pay(1.23);
        CashPayment cp = new CashPayment();
        cp.pay(1.23);
        IPayment ip = new MomoPayment();
        ip.pay(1.23);
    }
}
