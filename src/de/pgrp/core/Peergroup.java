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
 * Copyright (c) 2013, 2014 Nicolas Inden
 */

package de.pgrp.core;

import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.BrokenBarrierException;

import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.UPNPResponseException;

import javax.crypto.*;
import javax.crypto.spec.*;

import java.security.spec.*;

/**
 * This processes cmd-line args, initializes all needed settings and starts
 * Peergroups mainloop.
 * 
 * @author Nicolas Inden
 */
public class Peergroup {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// ---------- Hello and welcome ----------
		String os = System.getProperty("os.name");
		String java_version = System.getProperty("java.version");
		
		Globals.log.addMsg(
				"Starting " + Globals.PROGNAME + " " + Globals.VERSION
				+ " on " + os + " " + System.getProperty("os.version")
				+ " with Java " + java_version, 2);

		// ---------- Initialization ----------
		processCmdLineArgs(args);
		
		if(!Helper.readConfigFile()){
			//In this case the config file either does not exist, or it has wrong format/missing values
			Helper.handleMissingConfigFile();
		}else{
			//Here we have a valid config file, and can thus continue either with or without GUI
			if(Globals.useGUI){
				Helper.initGUI();
			}
		}
		
		//More initialization
		detectInternalExternalIPs();
		openPortWithUPnP();
		doInitialDirectoryScan();
		connectToXMPPServer();
		initEncryptionKeys();
		
		// ---------- Get things started ----------
		
		//Push a request to start worker threads for StorageWorker and NetworkWorker
		Globals.requestQueue.offer(new Request(Globals.START_THREADS));

		// -- Create main thread
		Globals.main = new MainWorker();
		Globals.main.start();
		
		// Register Shutdown hook to gracefully close threads on ctrl-c
		Runtime.getRuntime().addShutdownHook(new Shutdown());

		//Barrier is free as soon as all threads/workers are started
		try {
			Globals.bootupBarrier.await();
		} catch (InterruptedException ie) {
			//Nothing
		} catch (BrokenBarrierException bbe) {
			Globals.log.addMsg(bbe.toString(), 4);
		}

		// ---------- Ready for shutdown ----------
		
		// -- Wait for threads to terminate (only happens through SIGINT/SIGTERM
		// see handler above)
		try {
			if (!Globals.serverMode)
				Globals.storage.join();

			Globals.network.join();
			Globals.thriftClient.join();
			Globals.main.join();
			if (Globals.enableModQueue) {
				Globals.modQueue.join();
			}

			//Close all open connections to P2PDevices
			for (P2Pdevice d : Globals.p2pDevices) {
				d.closeTransport();
			}

		} catch (InterruptedException ie) {
			Globals.log.addMsg("Couldn't wait for all threads to cleanly shut down! Oh what a mess... Bye!", 1);
		}
		
		cleanup(0);
		
		//Wait until ShutdownHook has finished
		try {
			Globals.shutdownBarrier.await();
		} catch (InterruptedException ie) {

		} catch (BrokenBarrierException bbe) {
			Globals.log.addMsg(bbe.toString(), 4);
		}
	}

	/**
	 * Process the arguments the user supplied while starting peergroup.
	 * 
	 * @param cmds the string array containing supplied arguments
	 */
	private static void processCmdLineArgs(String[] cmds) {
		String last = "";

		for (String current : cmds) {
			if (current.equals("-h")) {
				System.out.println(getHelpString());
				quit(9);
			}
			if (last.equals("-c")) {
				Globals.configFile = current;
			}
			if (current.equals("-s")) {
				Globals.serverMode = true;
				Globals.log.addMsg("Running in server mode", 2);
			}
			if (current.equals("-GUI")) {
				Globals.useGUI = true;
				Globals.log.addMsg("Running in GUI mode", 2);
			}
			last = current;
		}
	}
	
	/**
	 * Here we derive a secret key from the password of the MUC channel.
	 * If the channel has no password the secret key is derived from the
	 * string "P33rgr0up".
	 */
	private static void initEncryptionKeys(){
		// Initial settings for encryption/decryption
		String plainkey = "P33rgr0up";
		byte[] salt = { 0x12, 0x78, 0x4F, 0x33, 0x13, 0x4B, 0x6B, 0x2F };
	
		// If we use a password for our channel, use it to decrypt the data
		if (!Globals.conference_pass.equals(""))
			plainkey = Globals.conference_pass;
		
		try{
			SecretKeyFactory fac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(plainkey.toCharArray(), salt,	65536, 128);
			SecretKey tmp1 = fac.generateSecret(spec);
			Globals.secKey = new SecretKeySpec(tmp1.getEncoded(),	"AES");
		}catch(Exception e){
			Globals.log.addMsg("Error while deriving secret key from password: " + e);
			quit(11);
		}
	}

	/**
	 * Creates the help string containing all information of how to use this
	 * program
	 * 
	 * @return the help string
	 */
	private static String getHelpString() {
		String out = "";
		out += "  -h                            Prints this help\n";
		out += "  -c              [CONFIG]      Set the config xml file\n";
		out += "  -s                            Server mode (no reaction on hd activity)\n";
		out += "  -GUI                          Show a nice GUI on startup";
		return out;
	}

	/**
	 * If the external IP was not set by the cmd-line argument, this function
	 * detects it automatically.
	 * 
	 * If neither an IP was set nor one was detected, Peergroup exits.
	 */
	private static void detectInternalExternalIPs() {
		// Get local IP
		if(!Globals.internalIP4.equals("")) {
			Globals.log.addMsg("Internal IPv4 was manually set, skip the guessing.");
		} else {
			Helper.getInternalIP4();
		}

		// Get external IPv4
		if (!Globals.externalIP4.equals("")) {
			Globals.log.addMsg("External IPv4 was manually set, skip the guessing.");
		} else {
			Helper.getExternalIP4();
		}
		
		
		// Get external IPv6
		if (!Globals.externalIP6.equals("")) {
			Globals.log.addMsg("External IPv6 was manually set, skip the guessing.");
		} else {
			Helper.getExternalIP6();
		}
	}

	

	/**
	 * This function forwards the chosen P2P port to the detected
	 * internal IP using UPnP.
	 * 
	 * Needs UPnP to be supported by and activated in the router.
	 */
	private static void openPortWithUPnP() {
		if (!Globals.doUPnP)
			return;
		int discoveryTimeout = 5000; // 5 secs to receive a response from
		// devices
		try {
			InternetGatewayDevice[] IGDs = InternetGatewayDevice.getDevices(discoveryTimeout);
			if (IGDs != null) {
				// let's the the first device found
				Globals.igd = IGDs[0];
				Globals.log.addMsg("Found device " + Globals.igd.getIGDRootDevice().getModelName());

				// now let's open the port
				// we assume that localHostIP is something else than 127.0.0.1
				boolean mapped = Globals.igd.addPortMapping("Peergroup",
						null, Globals.p2pPort, Globals.p2pPort,
						Globals.internalIP4, 0, "TCP");
				if (mapped) {
					Globals.log.addMsg("Port " + Globals.p2pPort
							+ " mapped to " + Globals.internalIP4);
				}
			} else {
				Globals.log.addMsg(
						"No UPnP enabled router found! You probably have to forward port "
								+ Globals.p2pPort
								+ " in your router manually to your local IP "
								+ Globals.internalIP4, 4);
			}
		} catch (IOException ex) {
			Globals.log.addMsg("Failed to open port via UPnP: Maybe the port is already open, or your router does not support UPnP?", 4);
		} catch (UPNPResponseException respEx) {
			Globals.log.addMsg("Failed to open port via UPnP: Maybe the port is already open, or your router does not support UPnP?", 4);
		}
	}

	/**
	 * Performs an initial scan of the shared directory
	 */
	private static void doInitialDirectoryScan() {
		Globals.folders = new LinkedList<String>();
		Globals.log.addMsg("Doing initial scan of share directory...");
		File root = Storage.getInstance().getDirHandle();
		Helper.iterateFilesOnInitScan(root);
	}

	/**
	 * Establish connection to the XMPP server and join channel
	 */
	private static void connectToXMPPServer() {
		Network xmppNet = Network.getInstance();
		
		//connect to server
		if (xmppNet.xmppConnect()) {
			xmppNet.xmppLogin();
		}
		
		if (!xmppNet.isConnected() || !xmppNet.isLoggedIn()) {
			// There must have been some error while connecting,
			// so we need to shut down Peergroup
			quit(5);
		}
		
		//Join MUC channel
		xmppNet.joinMUC(Globals.xmpp_user, Globals.conference_pass, Globals.conference_channel + "@" + Globals.conference_server);
		//Say "Hi!" :-)
		xmppNet.sendMUCmessage("Hi, I'm a peergroup client. I do awesome things :-)");
	}

	public static void cleanup(int no) {
		if (Globals.quitting)
			return;

		Globals.quitting = true;

		if (Globals.igd != null) {
			try {
				boolean unmapped = Globals.igd.deletePortMapping(null, Globals.p2pPort, "TCP");
				if (unmapped) {Globals.log.addMsg("Released port mapping for Peergroup on port " + Globals.p2pPort);
				}
			} catch (IOException ioe) {
				Globals.log.addMsg("Error unmapping port: UPnP unsupported or deactivated!", 4);
			} catch (UPNPResponseException respEx) {
				Globals.log.addMsg("Error unmapping port: UPnP unsupported or deactivated!", 4);
			}
		}

		Globals.log.closeLog();
	}
	
	public static void quit(int no) {
		if (Globals.quitting)
			return;

		Globals.quitting = true;

		if (Globals.igd != null) {
			try {
				boolean unmapped = Globals.igd.deletePortMapping(null,Globals.p2pPort, "TCP");
				if (unmapped) {
					Globals.log.addMsg("Released port mapping for Peergroup on port " + Globals.p2pPort);
				}
			} catch (IOException ioe) {
				Globals.log.addMsg("Error unmapping port: " + ioe, 4);
			} catch (UPNPResponseException respEx) {
				Globals.log.addMsg("Error unmapping port: " + respEx, 4);
			}
		}

		Globals.log.closeLog();
		System.exit(no);
	}
}
