package helper;

import java.util.ArrayList;

public class Trades {
private ArrayList<StockTrade> data = null;
	
	public Trades(ArrayList<StockTrade> tempStockTrades) {
		this.data = tempStockTrades;
	}
	
	public ArrayList<StockTrade> getTrades() {
		return data;
	}
	
	public void setTrades(ArrayList<StockTrade> newTrades) {
		this.data = newTrades;
	}
}
