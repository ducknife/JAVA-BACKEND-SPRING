package OOP.Polymophism;

public class MomoPayment implements IPayment {
    @Override // bắt buộc có từ khóa Override khi implement
    public void pay(double amount) {
        System.out.println("Da thanh toan " + amount + " bang Momo");
    }
}
