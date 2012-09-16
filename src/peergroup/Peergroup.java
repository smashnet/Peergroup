/*
* Peergroup - Peergroup.java
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
* Copyright (c) 2012 Nicolas Inden
*/

package peergroup;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import java.util.concurrent.CyclicBarrier;
import net.sbbi.upnp.*;
import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.UPNPResponseException;

/**
 * This processes cmd-line args, initializes all needed settings
 * and starts Peergroups mainloop.
 *
 * @author Nicolas Inden
 */
public class Peergroup {
	
	/**
	* @param args the command line arguments
	*/
    public static void main(String[] args) {
		// -- Handle SIGINT and SIGTERM
		SignalHandler signalHandler = new SignalHandler() {
			public void handle(Signal signal) {
			    if(Constants.caughtSignal){
			        Constants.log.addMsg("Last interrupt couldn't successfully quit the program: Using baseball bat method :-/",1);
			        quit(5);
			    }
				Constants.log.addMsg("Caught signal: " + signal + ". Gracefully shutting down!",1);
				Constants.caughtSignal = true;
				Constants.storage.stopStorageWorker();
				Constants.network.stopNetworkWorker();
				Constants.thrift.stopThriftWorker();
				Constants.thriftClient.stopPoolExecutor();
				if(Constants.enableModQueue){
					Constants.modQueue.interrupt();
				}
				Constants.main.interrupt();
			}
		};
		Signal.handle(new Signal("TERM"), signalHandler);
		Signal.handle(new Signal("INT"), signalHandler);
		
		// -- Here we go
		String os = System.getProperty("os.name");
        Constants.log.addMsg("Starting " + Constants.PROGNAME + " " 
			+ Constants.VERSION + " on " + os + " " + System.getProperty("os.version"),2);
      		
        getCmdArgs(args);
		getIPs();
		doInitialDirectoryScan();
		joinXMPP();
		doUPnP();
		enqueueThreadStart();
		
		if(os.equals("Linux") || os.equals("Windows 7"))
				Constants.enableModQueue = true;
		
		// -- Create main thread
		Constants.main = new MainWorker();		
		Constants.main.start();
		
		try{
			Constants.myBarrier.await();
		}catch(InterruptedException ie){
		
		}catch(BrokenBarrierException bbe){
			Constants.log.addMsg(bbe.toString(),4);
		}
		
		
		// -- Wait for threads to terminate (only happens through SIGINT/SIGTERM see handler above)
		try{
			Constants.storage.join();
			Constants.network.join();
			Constants.thrift.join();
			Constants.thriftClient.join();
			Constants.main.join();
			if(Constants.enableModQueue){
				Constants.modQueue.join();
			}
			
			for(P2Pdevice d : Constants.p2pDevices){
				d.closeTransport();
			}
			
		}catch(InterruptedException ie){
			Constants.log.addMsg("Couldn't wait for all threads to cleanly shut down! Oh what a mess... Bye!",1);
		}
        
        quit(0);
    }
    
	/**
	* Parses the arguments given on the command line
	*
	* @param args the array of commands
	*/
    private static void getCmdArgs(String[] args){
		boolean resSet = false;
		String last = "";
        for(String s: args){
            if(s.equals("-h") || s.equals("--help")){
				System.out.println(getHelpString());
				quit(0);
			}
			if(last.equals("-dir")){
				if(s.charAt(s.length()-1) != '/'){ //Probably need sth special for windows here
					s = s.concat("/");
				}
				Constants.rootDirectory = s;
				Constants.log.addMsg("Set share directory to: " + Constants.rootDirectory,3);
			}
			if(last.equals("-jid")){
				String jid[] = s.split("@");
				if(jid.length < 2){
					Constants.log.addMsg("Invalid JID!",1);
					quit(1);
				}
				Constants.user = jid[0];
				Constants.server = jid[1];
				Constants.log.addMsg("Set JID to: " + Constants.user + "@" + Constants.server,3);
			}
			if(last.equals("-res")){
				Constants.resource = s;
				Constants.log.addMsg("Set resource to: " + Constants.resource,3);
				resSet = true;
			}
			if(last.equals("-chan")){
				String conf[] = s.split("@");
				if(conf.length < 2){
					Constants.log.addMsg("Invalid conference channel!",1);
					quit(1);
				}
				Constants.conference_channel = conf[0];
				Constants.conference_server = conf[1];
				Constants.log.addMsg("Set conference channel to: " + Constants.conference_channel + "@" 
					+ Constants.conference_server,3);
			}
			if(last.equals("-XMPPport")){
				try{
					Constants.port = Integer.parseInt(s);
				}catch(NumberFormatException nan){
					Constants.log.addMsg("Invalid port!",1);
					quit(1);
				}
				Constants.log.addMsg("Set XMPP port to: " + Constants.port,3);
			}
			if(last.equals("-P2Pport")){
				try{
					Constants.p2pPort = Integer.parseInt(s);
				}catch(NumberFormatException nan){
					Constants.log.addMsg("Invalid port!",1);
					quit(1);
				}
				Constants.log.addMsg("Set P2P port to: " + Constants.p2pPort,3);
			}
			if(last.equals("-cSize")){
				try{
					Constants.chunkSize = Integer.parseInt(s);
				}catch(NumberFormatException nan){
					Constants.log.addMsg("Invalid chunkSize!",1);
					quit(1);
				}
				Constants.log.addMsg("Set chunk size to: " + Constants.chunkSize,3);
			}
			/*if(last.equals("-limit")){
				try{
					Constants.shareLimit = Long.parseLong(s);
				}catch(NumberFormatException nan){
					Constants.log.addMsg("Invalid share limit!",1);
					quit(1);
				}
				Constants.log.addMsg("Set share limit to: " + Constants.shareLimit,3);
			}*/
			if(last.equals("-pass")){
				Constants.pass = s;
			}
			if(last.equals("-ip")){
				Constants.ipAddress = s;
				Constants.log.addMsg("Set external IP to: " + Constants.ipAddress,3);
			}
			if(s.equals("-noEventQueue")){
				Constants.enableModQueue = false;
				Constants.log.addMsg("Manually disabled Event-Queue",3);
			}
			if(s.equals("-noUPnP")){
				Constants.doUPnP = false;
				Constants.log.addMsg("Manually disabled UPnP",3);
			}
			last = s;
        }
		if(Constants.user.equals("") || Constants.pass.equals("") || 
			Constants.conference_channel.equals("") || Constants.conference_server.equals("") ||
			Constants.server.equals("")){
				Constants.log.addMsg("Cannot start! Require: -jid -pass -chan");
				quit(0);	
		}
		if(!resSet){
			Random gen = new Random(System.currentTimeMillis());
			int append_no = 10000+gen.nextInt(90000);
			Constants.resource += append_no;
			Constants.log.addMsg("Set resource to: " + Constants.resource,3);
		}
    }
	
	/**
	* Creates the help string containing all information of how to use this program
	*
	* @return the help string
	*/
	private static String getHelpString(){
		String out = "";
		out += "  -h                            prints this help\n";
		out += "  -dir            [DIR]         set the shared files directory (default: ./share/)\n";
		out += "  -jid            [JID]         set your jabber ID (e.g. foo@jabber.bar.com)\n";
		out += "  -res            [RESOURCE]    set the resource (default: peergroup)\n";
		out += "  -pass           [PASS]        set the password for your JID\n";
		out += "  -XMPPport       [PORT]        set the XMPP server port (default: 5222)\n";
		out += "  -chan           [CHANNEL]     set the conference channel to join\n";
		out += "                                (e.g. foo@conference.jabber.bar.com)\n";
		out += "  -ip             [IP]          manually set your external IP\n";
		out += "                                (the IP is usually guessed by the program)\n";
		out += "  -P2Pport        [PORT]        set the port for P2P data exchange (default: 43334)\n";
		out += "  -cSize          [SIZE]        set the chunk size for P2P data exchange (default: 512000Byte)\n";
		//out += "  -limit          [LIMIT]       set the amount of space you want to share in MB\n";
		//out += "                                (default: 2048MB)\n";
		out += "  -noUPnP                       disable UPnP port forwarding (default: enabled)\n";
		return out;
	}
    
	/**
	* If the external IP was not set by the cmd-line argument, this function queries
	* the external IP from http://files.smashnet.de/getIP.php
	* If neither an IP was set nor one was detected, Peergroup exits.
	*/
	private static void getIPs(){
		// Get local IP
		try{
			Constants.localIP = InetAddress.getLocalHost().getHostAddress();
		}catch(UnknownHostException uhe){
			Constants.log.addMsg("Cannot get local IP: " + uhe,4);
		}
		
		
		// Get external IP
		if(!Constants.ipAddress.equals("")){
			Constants.log.addMsg("External IP was manually set, skipping the guessing.");
			return;
		}
		try{
			URL whatismyip = new URL("http://files.smashnet.de/getIP.php");
			BufferedReader in = new BufferedReader(new InputStreamReader(
			                whatismyip.openStream()));
		
			Constants.ipAddress = in.readLine();
			Constants.log.addMsg("Found external IP: " + Constants.ipAddress);
		}catch(Exception e){
			Constants.log.addMsg("Couldn't get external IP! " + e + " Try setting it manually!",1);
			quit(1);
		}
	}
	
	private static void doUPnP(){
		if(!Constants.doUPnP)
			return;
		int discoveryTimeout = 5000; // 5 secs to receive a response from devices
		try {
			InternetGatewayDevice[] IGDs = InternetGatewayDevice.getDevices( discoveryTimeout );
			if ( IGDs != null ) {
				// let's the the first device found
				Constants.igd = IGDs[0];
				Constants.log.addMsg( "Found device " + Constants.igd.getIGDRootDevice().getModelName() );
				// now let's open the port
				// we assume that localHostIP is something else than 127.0.0.1
				boolean mapped = Constants.igd.addPortMapping( "Peergroup", 
		        							   			null, Constants.p2pPort, Constants.p2pPort,
		                                   				 Constants.localIP, 0, "TCP" );
				if ( mapped ) {
					Constants.log.addMsg( "Port " + Constants.p2pPort + " mapped to " + Constants.localIP );
				}
			}
		} catch ( IOException ex ) {
			Constants.log.addMsg("Failed to open port: " + ex,4);
			Constants.log.addMsg("Maybe the port is already open?",4);
		} catch( UPNPResponseException respEx ) {
			Constants.log.addMsg("Failed to open port: " + respEx,4);
			Constants.log.addMsg("Maybe the port is already open?",4);
		}
	}
	
	private static void doInitialDirectoryScan(){
		Constants.log.addMsg("Doing initial scan of share directory...");
		File root = Storage.getInstance().getDirHandle();
		iterateFilesOnInitScan(root);
	}
	
	private static void iterateFilesOnInitScan(File dir){
		for(File newFile : dir.listFiles() ){
			if(newFile.getName().charAt(0) == '.'){
				continue;
			}
			if(newFile.isDirectory()){
				iterateFilesOnInitScan(newFile);
			}else if(newFile.isFile()){
				Constants.log.addMsg("Found: " + newFile.getName(),2);
				Constants.requestQueue.offer(new FSRequest(Constants.LOCAL_FILE_INITSCAN,newFile.getPath()));
			}
		}
	}
	
	private static void joinXMPP(){
		Network xmppNet = Network.getInstance();
		if(!xmppNet.isConnected() || !xmppNet.isLoggedIn()){
			// There must have been some error while connecting,
			// so we need to shut down Peergroup
			quit(5);
		}
		xmppNet.joinMUC(Constants.user, Constants.pass, 
			Constants.conference_channel + "@" + Constants.conference_server);
		xmppNet.sendMUCmessage("Hi, I'm a peergroup client. I do awesome things :-)");
	}
	
	private static void enqueueThreadStart(){
		Constants.requestQueue.offer(new Request(Constants.START_THREADS));
	}
	
	public static void quit(int no){
		if(Constants.igd != null){
			try{
				boolean unmapped = Constants.igd.deletePortMapping( null, Constants.p2pPort, "TCP" );
				if ( unmapped ) {
					Constants.log.addMsg("Released port mapping for Peergroup on port " + Constants.p2pPort);
				}
			}catch(IOException ioe){
				Constants.log.addMsg("Error unmapping port: " + ioe,4);
			}catch(UPNPResponseException respEx){
				Constants.log.addMsg("Error unmapping port: " + respEx,4);
			}
		}
		
		Constants.log.closeLog();
		System.exit(no);
	}
}
