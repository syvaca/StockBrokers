package helper;

import java.util.ArrayList;

public class Traders {
private ArrayList<SingleTrader> data = null;
	
	public Traders(ArrayList<SingleTrader> tempSingleTraders) {
		this.data = tempSingleTraders;
	}
	
	public ArrayList<SingleTrader> getTraders() {
		return data;
	}
	
	public void setTrades(ArrayList<SingleTrader> newTraders) {
		this.data = newTraders;
	}
}
