package helper;

public class SingleTrader {
	private int serial;
	private int balance;
	
	public SingleTrader(int serial, int balance) {
		this.serial = serial;
		this.balance = balance;
	}
	
	public int getSerial() {
		return serial;
	}
	
	public int getBalance() {
		return balance;
	}
	
}
