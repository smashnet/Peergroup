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
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import java.util.concurrent.CyclicBarrier;
import net.sbbi.upnp.*;
import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.UPNPResponseException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

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
				Constants.log.addMsg("Caught signal: " + signal + ". Gracefully shutting down!",1);
				
				if(Constants.storage != null)
					Constants.storage.stopStorageWorker();
				if(Constants.network != null)
					Constants.network.stopNetworkWorker();
				if(Constants.thriftClient != null)
					Constants.thriftClient.stopPoolExecutor();
				if(Constants.enableModQueue){
					if(Constants.modQueue != null)
						Constants.modQueue.interrupt();
				}
				if(Constants.main != null)
					Constants.main.interrupt();
				
				Peergroup.quit(666);
			}
		};
		Signal.handle(new Signal("TERM"), signalHandler);
		Signal.handle(new Signal("INT"), signalHandler);
		
		// -- Here we go
		String os = System.getProperty("os.name");
		String java_version = System.getProperty("java.version");
		
        Constants.log.addMsg("Starting " + Constants.PROGNAME + " " 
			+ Constants.VERSION + " on " + os + " " + System.getProperty("os.version")
			+ " with Java " + java_version,2);
      		
		getCmdLineArgs(args);
        getConfig();
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
    
	
	private static void getCmdLineArgs(String[] cmds){
		String last = "";
		
		for(String current : cmds){
			if(current.equals("-h")){
				System.out.println(getHelpString());
				quit(9);
			}
			if(last.equals("-c")){
				Constants.config = current;
			}
			last = current;
		}
	}
	/**
	* Reads config from config.xml
	*/
    private static void getConfig(){
		try{
			File xmlConfig = new File(Constants.config);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlConfig);
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("xmpp-account");
			
			for(int i = 0; i < nList.getLength(); i++){
				
				Node n = nList.item(i);
				
				if(n.getNodeType() == Node.ELEMENT_NODE){
					Element eElement = (Element)n;
					
					String val;
					
					val = getTagValue("server",eElement);
					if(val != null){
						Constants.server = val;
					}else{
						Constants.log.addMsg("Required value missing in config: xmpp-account -> server",1);
						quit(9);
					}
						
					val = getTagValue("user",eElement);
					if(val != null){
						Constants.user = val;
					}else{
						Constants.log.addMsg("Required value missing in config: xmpp-account -> user",1);
						quit(9);
					}
					
					val = getTagValue("pass",eElement);
					if(val != null){
						Constants.pass = val;
					}else{
						Constants.log.addMsg("Required value missing in config: xmpp-account -> pass",1);
						quit(9);
					}
					
					val = getTagValue("resource",eElement);
					if(val != null)
						Constants.resource = val;
					
					val = getTagValue("port",eElement);
					if(val != null)
						Constants.port = Integer.parseInt(val);
				}
			}
			
			nList = doc.getElementsByTagName("conference");
			
			for(int i = 0; i < nList.getLength(); i++){
				
				Node n = nList.item(i);
				
				if(n.getNodeType() == Node.ELEMENT_NODE){
					Element eElement = (Element)n;
					
					String val;
					
					val = getTagValue("server",eElement);
					if(val != null){
						Constants.conference_server = val;
					}else{
						Constants.log.addMsg("Required value missing in config: conference -> server",1);
						quit(9);
					}
					
					val = getTagValue("channel",eElement);
					if(val != null){
						Constants.conference_channel = val;
					}else{
						Constants.log.addMsg("Required value missing in config: conference -> channel",1);
						quit(9);
					}
					
				}
			}
			
			nList = doc.getElementsByTagName("pg-settings");
			
			for(int i = 0; i < nList.getLength(); i++){
				
				Node n = nList.item(i);
				
				if(n.getNodeType() == Node.ELEMENT_NODE){
					Element eElement = (Element)n;
					
					String val;
					
					val = getTagValue("share",eElement);
					if(val != null)
						Constants.rootDirectory = val;
					
					val = getTagValue("extIP",eElement);
					if(val != null)
						Constants.ipAddress = val;
					
					val = getTagValue("port",eElement);
					if(val != null)
						Constants.p2pPort = Integer.parseInt(val);
				}
			}
		Constants.log.addMsg("Using " + Constants.config + "... Ignoring commandline arguments!",3);
		}catch(FileNotFoundException fnf){
			Constants.log.addMsg("Could not find config file! Creating sample file...",1);
			Constants.log.addMsg("Please edit config.smp to your needs and rename it to config.xml",4);
			createSampleConfig();
			quit(10);
		}catch(Exception ioe){
			Constants.log.addMsg("Error reading config: " + ioe,1);
			quit(11);
		}
    }
	
	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);
		
		if(nValue == null || nValue.getNodeValue() == null)
			return null;
 
		return nValue.getNodeValue();
	}
	
	private static void createSampleConfig(){
		try{
			File conf = new File("config.smp");
			conf.delete();
			conf.createNewFile();
			FileWriter fw = new FileWriter(conf);
			BufferedWriter bw = new BufferedWriter(fw);
			String sample = "<?xml version=\"1.0\"?>\n"
						  + "<peergroup>\n"
						  + "\t<xmpp-account>\n"
						  + "\t\t<server>jabber-server</server>\n"
						  + "\t\t<user>username</user>\n"
						  + "\t\t<pass>password</pass>\n"
						  + "\t\t<resource></resource>\n"
						  + "\t\t<port></port>\n"
					      + "\t</xmpp-account>\n"
						  + "\t<conference>\n"
						  + "\t\t<server>conference-server</server>\n"
						  + "\t\t<channel>channelname</channel>\n"
						  + "\t</conference>\n"
						  + "\t<pg-settings>\n"
					      + "\t\t<share>./share/</share>\n"
					      + "\t\t<extIP></extIP>\n"
						  + "\t\t<port>53333</port>\n"
						  + "\t</pg-settings>\n"
						  + "</peergroup>";
			bw.write(sample,0,sample.length());
			bw.close();
		}catch(Exception e){
			Constants.log.addMsg("Couldn't create sample config",1);
			quit(12);
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
		out += "  -c              [CONFIG]      set the config xml file\n";
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
		Constants.folders = new LinkedList<String>();
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
				Constants.folders.add(newFile.getPath());
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
