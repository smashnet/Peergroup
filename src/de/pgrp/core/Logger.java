/*
* Peergroup - Logger.java
* 
* This file is part of Peergroup.
*
* Peergroup is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Peergroup is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* Copyright (c) 2013 Nicolas Inden
*/

package de.pgrp.core;

import java.io.*;
import java.util.Calendar;

import de.pgrp.gui.LogWindow;

/**
* This class supplies console- and fileouput logging abilities
*
* @author Nicolas Inden
*/
public class Logger {

	public static Logger log = new Logger();
	public static int RED = 1;
	public static int GREEN = 2;
	public static int BLUE = 3;
	public static int YELLOW = 4;
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
			String date = getYear()
		        + getMonth()
		        + getDayOfMonth() + "_" 
		        + getHourOfDay() 
		        + getMinute();
			this.color = false;
			File directory = new File("log");
			directory.mkdir();
			this.output = new File("log/" + date + "_peergroup.log");
			this.fw = new FileWriter(this.output);
			this.bw = new BufferedWriter(this.fw);
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}catch(SecurityException se){
			System.out.println("Cannot create directory! " + se);
		}
	}
	
	/**
	* Color constructor
	*/
	public Logger(boolean colored){
		try{
			this.cal = Calendar.getInstance();
			String date = getYear()
		        + getMonth()
		        + getDayOfMonth() + "_" 
		        + getHourOfDay() 
		        + getMinute();
			this.color = colored;
			File directory = new File("log");
			directory.mkdir();
			this.output = new File("log/" + date + "_peergroup.log");
			this.fw = new FileWriter(this.output);
			this.bw = new BufferedWriter(this.fw);
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}catch(SecurityException se){
			System.out.println("Cannot create directory! " + se);
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
	
	public static Logger getInstance(){
		return log;
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
				+ getSecond() + "."
				+ getMillis() + "] - ";
		return tmp;
	}
	
	/**
	* Adds a message to the current log, this message displays 
	* on the console and is written into the logfile
	*
	* @param txt the logged string
	*/
	public  void addMsg(String txt){
		try{
			String log = this.getTimeString() + txt;
			this.bw.write(log + '\n');
			System.out.println(log);
			if(Globals.useGUI)
				LogWindow.getInstance().addText(log + System.getProperty( "line.separator" ));
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
	public  void addMsg(String txt, int color){
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
				if(Globals.useGUI)
					LogWindow.getInstance().addText(time + txt + System.getProperty( "line.separator" ));
			}
			this.bw.flush();
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	/**
	* Adds a line seperator to the log
	*/
	public  void addSeperator(){
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
		res = "" + (tmp%100);
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
	
	private String getMillis(){
		String res;
		int tmp = cal.get(Calendar.MILLISECOND);
		if(tmp < 10){
			res = "00" + tmp;
		}else if(tmp < 100 && tmp >= 10){
			res = "0" + tmp;
		}else{
			res = "" + tmp;
		}
		return res;
	}
}
