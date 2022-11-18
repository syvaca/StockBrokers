package syvaca_CS201_Assignment3;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import helper.Message;
import helper.StockTrade;

import java.io.*;

public class TraderClient<T> {
	private ObjectInputStream is;
	private ObjectOutputStream os;
	private Map<String, Double> tickerPrices;
	private long start_time;
	
	public TraderClient(String hostname, int port) {
		try {
			Socket s = new Socket(hostname, port);
			is = new ObjectInputStream(s.getInputStream());
			os = new ObjectOutputStream(s.getOutputStream());
		} catch (IOException ioe) {
			System.out.println("ioe in ChatClient constructor: " + ioe.getMessage());
		}
	}
	
	public void broadcast(Message<?> message) {
		try {
			os.writeObject(message);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void runTrade(ArrayList<StockTrade> allTrades) throws InterruptedException {
		//assign message
		for(StockTrade st : allTrades) {
			double total = tickerPrices.get(st.getTicker()) * Math.abs(st.getStocksBought());
			if(st.getStocksBought() > 0) {
				System.out.println(Server.timeFormat(System.currentTimeMillis()-start_time) + 
						"Assigned purchase of " + st.getStocksBought() + " stock(s) of " + st.getTicker() + 
						". Total cost estimate = " + tickerPrices.get(st.getTicker()) + " * " + st.getStocksBought() + " = " + total);
			}
			else if(st.getStocksBought() < 0) {
				System.out.println(Server.timeFormat(System.currentTimeMillis()-start_time) + 
						"Assigned sale of " + Math.abs(st.getStocksBought()) + " stock(s) of " + st.getTicker() + 
						". Total gain = " + tickerPrices.get(st.getTicker()) + " * " + Math.abs(st.getStocksBought()) + " = " + total);
			}
		}
		
		//start + finish
		for(StockTrade st : allTrades) {
			//start message
			double total = tickerPrices.get(st.getTicker()) * Math.abs(st.getStocksBought());
			if(st.getStocksBought() > 0) {
				System.out.println(Server.timeFormat(System.currentTimeMillis()-start_time) + 
						"Starting purchase of " + st.getStocksBought() + " stock(s) of " + st.getTicker() + 
						". Total cost = " + tickerPrices.get(st.getTicker()) + " * " + st.getStocksBought() + " = " + total);
			}
			else if(st.getStocksBought() < 0) {
				System.out.println(Server.timeFormat(System.currentTimeMillis()-start_time) + 
						"Starting sale of " + Math.abs(st.getStocksBought()) + " stock(s) of " + st.getTicker() + 
						". Total gain estimate = " + tickerPrices.get(st.getTicker()) + " * " + Math.abs(st.getStocksBought()) + " = " + total);
			}
			Thread.sleep(1000);
			
			//finish message
			if(st.getStocksBought() > 0) {
				System.out.println(Server.timeFormat(System.currentTimeMillis()-start_time) + 
						"Finished purchase of " + st.getStocksBought() + " stock(s) of " + st.getTicker() + ".");
			}
			else if(st.getStocksBought() < 0) {
				System.out.println(Server.timeFormat(System.currentTimeMillis()-start_time) + 
						"Finished sale of " + Math.abs(st.getStocksBought()) + " stock(s) of " + st.getTicker() + ".");
			}	
		}
		//inform server we are open again
		broadcast(new Message<Boolean>(true, Message.MessageType.STATUS));
	}
	
	//uses code from https://piazza.com/class/l6h0gih4nb0fg/post/691
	public void run() { 
		
		try {
			while(true) {
				Message<T> message = (Message<T>)(is.readObject());
				Message.MessageType messageType = message.getType();
				
				switch(messageType) {
                	case STRING -> System.out.println(message.getMessage());
                	case TRADES -> {
                		ArrayList<StockTrade> stockTrades = (ArrayList<StockTrade>) message.getMessage();
                		runTrade(stockTrades);	
                	}
                	case PRICES -> {
                		tickerPrices = (Map<String, Double>) message.getMessage();
                	}
                	case TIME -> {
                		start_time = (long) message.getMessage();
                	}
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args) {
		System.out.println("Welcome to SalStocks v2.0!");
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the server hostname:");
		String hostname = scanner.nextLine();
		System.out.println("Enter the server port:");
		int port = scanner.nextInt();

		TraderClient tc = new TraderClient(hostname, port);
		tc.run();
		
	}	
}
