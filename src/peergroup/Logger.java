/*
* Peergroup - Logger.java
* 
* Peergroup is a P2P Shared Storage System using XMPP for data- and 
* participantmanagement and Apache Thrift for direct data-
* exchange between users.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* License: Not for public distribution!
*/

package peergroup;

import java.io.*;
import java.util.Calendar;

/**
* This class supplies console- and fileouput logging abilities
*
* @author Nicolas Inden
*/
public class Logger {

	private File output;
	private FileWriter fw;
	private BufferedWriter bw;
	private Calendar cal;
	private boolean color;
	
	/**
	* Default constructor
	*/
	public Logger(){
		try{
			this.cal = Calendar.getInstance();
			String date = getYear() + "-" 
		        + getMonth() + "-" 
		        + getDayOfMonth() + "_" 
		        + getHourOfDay() 
		        + getMinute();
			if(System.getProperty("os.name").equals("Windows 7")){
				this.color = false;
			}else{
				this.color = true;
			}
			this.output = new File(date + "_peergroup.log");
			this.fw = new FileWriter(this.output);
			this.bw = new BufferedWriter(this.fw);
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	/**
	* Constructor
	*
	* @param filename is the name of the logfile as string
	*/
	public Logger(String filename){
		this.cal = Calendar.getInstance();
		try{
			this.color = true;
			this.output = new File(filename);
			this.fw = new FileWriter(this.output);
			this.bw = new BufferedWriter(this.fw);
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	/**
	* Returns a string representing the current date and time 
	* in the following syle: "[YYYY/MM/DD HH:MM::SS] - "
	*
	* @return tmp the string
	*/
	private String getTimeString(){
		this.cal = Calendar.getInstance();
		String tmp = "[" + getYear() + "/" 
		        + getMonth() + "/" 
		        + getDayOfMonth() + " " 
		        + getHourOfDay() + ":" 
		        + getMinute() + ":" 
				+ getSecond() + "] - ";
		return tmp;
	}
	
	/**
	* Adds a message to the current log, this message displays 
	* on the console and is written into the logfile
	*
	* @param txt the logged string
	*/
	public synchronized void addMsg(String txt){
		try{
			String log = this.getTimeString() + txt;
			this.bw.write(log + '\n');
			System.out.println(log);
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	/**
	* Adds a message to the current log, this message displays 
	* on the console (colored) and is written into the logfile
	*
	* @param txt the logged string
	* @param color 1(red),2(green),3(blue),4(yellow)
	*/
	public synchronized void addMsg(String txt, int color){
		try{
			String time = this.getTimeString();
			if(this.color){
				switch(color){
					case 1: /* Red */ 
						this.bw.write(time + txt + '\n');
						System.out.print(time + "\033[31m" + txt 
						                 + "\033[0m" + '\n');
						break;
					case 2: /* Green */
						this.bw.write(time + txt + '\n');
						System.out.print(time + "\033[32m" + txt 
						                 + "\033[0m" + '\n');
						break;
					case 3: /* Blue */
						this.bw.write(time + txt + '\n');
						System.out.print(time + "\033[34m" + txt 
						                 + "\033[0m" + '\n');
						break;
					case 4: /* Yellow*/
						this.bw.write(time + txt + '\n');
						System.out.print(time + "\033[1;33m" + txt 
						                 + "\033[0m" + '\n');
						break;
					default: 
						this.bw.write(time + txt + '\n');
						System.out.print(time + txt + '\n');
						break;
				}
			}else{
				this.bw.write(time + txt + '\n');
				System.out.print(time + txt + '\n');
			}
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	/**
	* Adds a line seperator to the log
	*/
	public synchronized void addSeperator(){
		try{
			this.bw.write("-------------------------\n");
			System.out.print("-------------------------\n");
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	/**
	* Closes the log and the FileWriter
	*/
	public void closeLog(){
		try{
			this.bw.write("-------------------------\nEnd of log...\n");
			System.out.println("-------------------------\nEnd of log...");
			this.bw.close();
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	private String getYear(){
		String res;
		int tmp = cal.get(Calendar.YEAR);
		if(tmp < 10){
			res = "0" + tmp;
		}else{
			res = "" + tmp;
		}
		return res;
	}
	
	private String getMonth(){
		String res;
		int tmp = (cal.get(Calendar.MONTH)+1);
		if(tmp < 10){
			res = "0" + tmp;
		}else{
			res = "" + tmp;
		}
		return res;
	}
	
	private String getDayOfMonth(){
		String res;
		int tmp = cal.get(Calendar.DAY_OF_MONTH);
		if(tmp < 10){
			res = "0" + tmp;
		}else{
			res = "" + tmp;
		}
		return res;
	}
	
	private String getHourOfDay(){
		String res;
		int tmp = cal.get(Calendar.HOUR_OF_DAY);
		if(tmp < 10){
			res = "0" + tmp;
		}else{
			res = "" + tmp;
		}
		return res;
	}
	
	private String getMinute(){
		String res;
		int tmp = cal.get(Calendar.MINUTE);
		if(tmp < 10){
			res = "0" + tmp;
		}else{
			res = "" + tmp;
		}
		return res;
	}
	
	private String getSecond(){
		String res;
		int tmp = cal.get(Calendar.SECOND);
		if(tmp < 10){
			res = "0" + tmp;
		}else{
			res = "" + tmp;
		}
		return res;
	}
}
