package syvaca_CS201_Assignment3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;


import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;

import helper.Message;
import helper.SingleTrader;
import helper.StockTrade;
import helper.Trades;

public class Server {
	
	public static ArrayList<StockTrade> allTrades = new ArrayList<StockTrade>();
	private static ArrayList<SingleTrader> allTraders = new ArrayList<SingleTrader>();
	public static Vector<ServerThread> serverThreads;
	public static Map<String, Double> tickerPrices = new HashMap<String, Double>();
	public static int left;
	public static long start_time;
	private int port;
	
	public Server(int port) {
		this.port = port;
	}
	
	public static String timeFormat(long milli) {
		DateFormat simple_date = new SimpleDateFormat("H:mm:ss:SSS");
		Date resultDate = new Date(milli - (16*60*60*1000));
		String resultString = "[" + simple_date.format(resultDate) + "]";
		return resultString;
	}
	
	// taken from https://www.baeldung.com/java-read-json-from-url
	public static String stream(URL url) throws IOException {
	    try (InputStream input = url.openStream()) {
	        InputStreamReader isr = new InputStreamReader(input);
	        BufferedReader reader = new BufferedReader(isr);
	        StringBuilder json = new StringBuilder();
	        int c;
	        while ((c = reader.read()) != -1) {
	            json.append((char) c);
	        }
	        return json.toString();
	    }
	}

	public static void main(String[] args) {
		//start a server
		Server server = new Server(3456);
		
		//READ IN SCHEDULE FILE 
		// used code found in https://www.javatpoint.com/how-to-read-csv-file-in-java
		boolean validCsv = false;
		Scanner scan_csv = new Scanner(System.in);
		String csvFile = "";
		String csv_info = "";
		while (!validCsv) {
			System.out.println("What is the path of the schedule file?");
			csvFile = scan_csv.nextLine();
			try {
				Scanner finalScanner = new Scanner(new File(csvFile));
				finalScanner.useDelimiter(",");  
				String[] values; 
				while (finalScanner.hasNext())  {  
					csv_info = finalScanner.nextLine();
					validCsv = true;
					values = csv_info.split(",");
					StockTrade newTrade = new StockTrade(Integer.parseInt(values[0]), values[1], Integer.parseInt(values[2]));
					allTrades.add(newTrade);
				}
			} catch(FileNotFoundException e) {
				System.out.println("\nThe file " + csvFile + " could not be found\n");
				validCsv = false;
				continue;
			}
		}
		System.out.println("The schedule file has been properly read.");
		
		
		//READ IN TRADERS FILE
		// used code found in https://www.javatpoint.com/how-to-read-csv-file-in-java
		boolean tradersCsv = false;
		Scanner scan_trader = new Scanner(System.in);
		String traderFile = "";
		String trader_info = "";
		while (!tradersCsv) {
			System.out.println("What is the path of the traders file?");
			traderFile = scan_trader.nextLine();
			try {
				Scanner traderScanner = new Scanner(new File(traderFile));
				traderScanner.useDelimiter(",");  
				String[] values; 
				while (traderScanner.hasNext())  {  
					trader_info = traderScanner.nextLine();
					tradersCsv = true;
					values = trader_info.split(",");
					SingleTrader newTrader = new SingleTrader(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
					allTraders.add(newTrader);
				}
			} catch(FileNotFoundException e) {
				System.out.println("\nThe file " + traderFile + " could not be found\n");
				tradersCsv = false;
				continue;
			}
		}
		System.out.println("The traders file has been properly read.");
		
		
		//collect stock prices from FINNHUB API
		
		//get all ticker names
		ArrayList<String> allTicker = new ArrayList<String>();	
		for(StockTrade st : allTrades) {
			boolean add = true;
			String temp = st.getTicker();
			for(int i = 0; i < allTicker.size(); i++) {
				if (allTicker.get(i).equals(temp)) {
					add = false;
				}
			}
			if(add) {
				allTicker.add(temp);
			}
		}
		//get prices for each ticker
		for(String tickerString : allTicker) {
			String info = "";
			try {
				URL url = new URL("https://finnhub.io/api/v1/quote?symbol=" + tickerString + "&token=cdc6neaad3i6ap45oiq0cdc6neaad3i6ap45oiqg");
				info = stream(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch(IOException ex) {
				System.out.println("ioe exception: " + ex.getMessage());
			}
			// fix the string to delete unnecessary characters
			info = info.substring(5);
			info = info.substring(0, info.indexOf(","));
		    
			//add prices of tickers to map
			double price = Double.parseDouble(info); 
			tickerPrices.put(tickerString, price);
		}
		
//		for (Map.Entry<String, Double> set : tickerPrices.entrySet()) {
//
//           // Printing all elements of a Map
//           System.out.println(set.getKey() + " = "
//                              + set.getValue());
//       }
		
		
		//connect clients from traders to server
		//code gotten from lecture slides (ChatRoom.java file)
		try {
			ServerSocket ss = new ServerSocket(3456);
			System.out.println("Listening on port 3456.");
			serverThreads = new Vector<ServerThread>();
			System.out.println("Waiting for traders...");
			
			
			while(serverThreads.size() != allTraders.size()) {
				Socket socket = ss.accept();
				System.out.println("Connection from: " + socket.getInetAddress());
				ServerThread st = new ServerThread(socket, allTraders.get(serverThreads.size()));
				serverThreads.add(st);
				
				if(serverThreads.size() != allTraders.size()) {
					left = allTraders.size() - serverThreads.size();
					System.out.println("Waiting for " + left + " more trader(s)...");
					
					//message all clients
					for(ServerThread serT : serverThreads) {
						serT.broadcast(new Message<>(left + " more trader(s) is needed before the service can begin.", Message.MessageType.STRING));
						serT.broadcast(new Message<>("Waiting...", Message.MessageType.STRING));
					}
				}
			}
			ss.close();
		} catch (IOException ioe) {
			System.out.println("ioe exception: " + ioe.getMessage());
		}
		
		System.out.println("Starting service.");
		start_time = System.currentTimeMillis();
		//send over all necessary info to clients (OG start time and prices)
		for(ServerThread serT : serverThreads) {
			serT.broadcast(new Message<>("All traders have arrived"  + '\n' + "Starting service.", Message.MessageType.STRING));
			serT.broadcast(new Message<>(tickerPrices, Message.MessageType.PRICES));
			serT.broadcast(new Message<>(start_time, Message.MessageType.TIME));
		}
		
		ArrayList<StockTrade> waitTrades = new ArrayList<StockTrade>();
		
		int trade = 0;
		while(trade < allTrades.size()) {
			
			//add all the waiting trades to the queue
			while((trade < allTrades.size()) && ((System.currentTimeMillis()-start_time)/1000 >= allTrades.get(trade).getInitiated())) {
				waitTrades.add(allTrades.get(trade));
				// code gotten from https://www.javatpoint.com/java-get-current-date
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
				LocalDateTime now = LocalDateTime.now();   
				allTrades.get(trade).setDate(dtf.format(now));
				trade++;
			}
				
			if(!waitTrades.isEmpty()) {
				for(ServerThread sThread : serverThreads) {
					ArrayList<StockTrade> canBeCompleted = new ArrayList<StockTrade>();
					if(!sThread.isBusy()) {
						for(StockTrade st : waitTrades) {
							double price = Math.abs(st.getStocksBought()) * tickerPrices.get(st.getTicker());
							//buying
							if(st.getStocksBought() > 0) {
								//checking if has enough budget before adding to list
								if(price < (sThread.trader.getBalance() - sThread.getSpent())) {
									sThread.setSpent(price);
									canBeCompleted.add(st);
								}	
							}
							//selling
							else if (st.getStocksBought() <= 0) {
								sThread.setProfit(price);
								canBeCompleted.add(st);
							}
						}
						
						sThread.soloTrades(canBeCompleted);	
						for(StockTrade stockTrade : canBeCompleted)  {
							waitTrades.remove(stockTrade);
						}
					}
				}
			}
		}
		
		//final messages
		String incompleteTrades = "Incomplete Trades: ";
		if(waitTrades.isEmpty()) {
			incompleteTrades += "NONE";
			incompleteTrades += '\n';
		}
		else {
			for(StockTrade st : waitTrades) {
				String individual = "(" + st.getInitiated() + ", " + st.getTicker() + ", " + st.getStocksBought() + ", " + st.getDate() + ")";
				incompleteTrades += individual;
				incompleteTrades += '\n'; 
				incompleteTrades += "                   ";	
			}
		}
		
		incompleteTrades = incompleteTrades.strip();
		incompleteTrades += '\n'; 
			
		//message all clients
		for(ServerThread serT : serverThreads) {
			serT.broadcast(new Message<>(incompleteTrades + "Total Profit Earned: $" + serT.getProfit() + '\n', Message.MessageType.STRING));
			serT.broadcast(new Message<>("Processing complete.", Message.MessageType.STRING));
		}			
				
		System.out.println("Processing complete.");

	}
}
