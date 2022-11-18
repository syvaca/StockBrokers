package helper;

import java.io.Serializable;

public class StockTrade implements Serializable {
	private static final long serialVersionUID = 1L;
	private int initiated;
	private String ticker;
	private int stocks_bought;
	private String date;
	
	public StockTrade(int initiated, String ticker, int stocks_bought) {
		this.initiated = initiated;
		this.ticker = ticker;
		this.stocks_bought = stocks_bought;
	}
	
	public int getInitiated() {
		return initiated;
	}
	
	public void setInitiated(int initiation) {
		this.initiated = initiation;
	}
	
	public String getTicker() {
		return ticker;
	}
	
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	
	public int getStocksBought() {
		return stocks_bought;
	}
	
	public void setStocksBought(int bought) {
		this.stocks_bought = bought;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}

}
