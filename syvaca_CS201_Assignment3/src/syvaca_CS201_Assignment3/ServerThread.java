package syvaca_CS201_Assignment3;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import helper.Message;
import helper.SingleTrader;
import helper.StockTrade;

public class ServerThread extends Thread {
	private ObjectOutputStream os;
	private ObjectInputStream is;
	public SingleTrader trader;
	private boolean available;
	private double spent;
	private double made;
	
	
	public ServerThread(Socket sock, SingleTrader trader) {
		try {
			this.os = new ObjectOutputStream(sock.getOutputStream());
			this.is = new ObjectInputStream(sock.getInputStream());
			this.trader = trader;	
			this.spent = 0.0;
			this.made = 0.0;
			this.available = true;
			this.start();
		} catch (IOException ioe) {
			System.out.println("ioe in ServerThread constructor: " + ioe.getMessage());
		}
	}
	
	//uses code from https://piazza.com/class/l6h0gih4nb0fg/post/691
	public void run() {
		try {
			while(true) {
				Message<?> message = (Message<?>)(is.readObject());
	            Message.MessageType messageType = message.getType();
	            switch(messageType) {
                case STATUS -> available = (boolean) message.getMessage();
	            }
	            
			}
		} catch(IOException | ClassNotFoundException ioe) {
			 interrupt();
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
	
	public void soloTrades(ArrayList<StockTrade> trades) {
		available = false;
		Message<ArrayList<StockTrade>> message = new Message<ArrayList<StockTrade>>(trades, Message.MessageType.TRADES);
		broadcast(message); //send over the trades that will be completed by a single trader
	}
	
	public double getProfit() {
		return made;
	}
	
	public void setProfit(double add) {
		made += add;
	}
	
	public double getSpent() {
		return spent;
	}
	
	public void setSpent(double add) {
		spent += add;
	}
	
	public boolean isBusy() {
		if(!this.available) {
			return true;
		}
		return false;
	}
	
}
