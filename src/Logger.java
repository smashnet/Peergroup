/*
*    Logger.java - A tiny lib for log messages
*    Copyright (C) 2011  Nicolas Inden
*    Contact: nico@smashnet.de
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package peergroup;

import java.io.*;
import java.util.Calendar;

/**
* This class supplies console- and fileouput logging abilities
*/
public class Logger {

	private File output;
	private FileWriter fw;
	private BufferedWriter bw;
	private Calendar cal;
	private boolean color;
	
	public Logger(){
		try{
			this.color = true;
			this.output = new File("std.log");
			this.fw = new FileWriter(this.output);
			this.bw = new BufferedWriter(this.fw);
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	public Logger(String filename){
		try{
			this.color = true;
			this.output = new File(filename);
			this.fw = new FileWriter(this.output);
			this.bw = new BufferedWriter(this.fw);
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	private String getTimeString(){
		this.cal = Calendar.getInstance();
		String tmp = "[" + cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DAY_OF_MONTH)
				+ " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" 
				+ cal.get(Calendar.SECOND) + "] - ";
		return tmp;
	}
	
	public void addMsg(String txt){
		try{
			String log = this.getTimeString() + txt;
			this.bw.write(log + '\n');
			System.out.println(log);
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	public void addMsg(String txt, int color){
		try{
			String time = this.getTimeString();
			if(this.color){
				switch(color){
					case 1: /* Red */ 
						this.bw.write(time + txt + '\n');
						System.out.print(time + "\033[31m" + txt + "\033[0m" + '\n');
						break;
					case 2: /* Green */
						this.bw.write(time + txt + '\n');
						System.out.print(time + "\033[32m" + txt + "\033[0m" + '\n');
						break;
					case 3: /* Blue */
						this.bw.write(time + txt + '\n');
						System.out.print(time + "\033[34m" + txt + "\033[0m" + '\n');
						break;
					case 4: /* Yellow*/
						this.bw.write(time + txt + '\n');
						System.out.print(time + "\033[1;33m" + txt + "\033[0m" + '\n');
						break;
					default: 
						this.bw.write(time + txt + '\n');
						System.out.print(time + txt + '\n');
						break;
				}
			}else{
				this.bw.write(this.getTimeString() + txt + '\n');
			}
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	public void addSeperator(){
		try{
			this.bw.write("-------------------------\n");
			System.out.print("-------------------------\n");
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
	
	public void closeLog(){
		try{
			this.bw.write("-------------------------\nEnd of log...");
			System.out.println("-------------------------\nEnd of log...");
			this.bw.close();
		}catch(IOException ioe){
			System.out.println("Caught error: " + ioe);
		}
	}
}
