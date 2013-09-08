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
 * Copyright (c) 2013 Nicolas Inden
 */

package de.pgrp.core;

import java.awt.EventQueue;
import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.UPNPResponseException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import de.pgrp.gui.EnterUserDataFrame;
import de.pgrp.gui.PGTrayIcon;
import de.pgrp.gui.RefreshData;

/**
 * This processes cmd-line args, initializes all needed settings and starts
 * Peergroups mainloop.
 * 
 * @author Nicolas Inden
 */
public class Peergroup {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		// -- Here we go
		String os = System.getProperty("os.name");
		String java_version = System.getProperty("java.version");
		
		Globals.log.addMsg(
				"Starting " + Globals.PROGNAME + " " + Globals.VERSION
				+ " on " + os + " " + System.getProperty("os.version")
				+ " with Java " + java_version, 2);

		getCmdLineArgs(args);
		if(!getConfig()){
			//In this case the config file either does not exist, or it has wrong format/missing values
			if(Globals.useGUI){
				//If we use GUI, show EnterUserDataFrame
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							EnterUserDataFrame.getInstance().setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				
				//Wait until data is entered
				try {
					Globals.inputBarrier.await();
				} catch (InterruptedException ie) {

				} catch (BrokenBarrierException bbe) {
					Globals.log.addMsg(bbe.toString(), 4);
				}
				
				Globals.inputBarrier.reset();
				
				//Create config file from entered values to avoid reentering the data each start
				createConfig();
				
				PGTrayIcon icon = PGTrayIcon.getInstance();
				icon.createTray();
				
				Thread guiRefresh = new Thread(new RefreshData());
				guiRefresh.run();
			}else{
				//In headless mode we shutdown Peergroup
				quit(10);
			}
		}else{
			//Here we have a valid config file, and can thus continue either with or without GUI
			if(Globals.useGUI){
				PGTrayIcon icon = PGTrayIcon.getInstance();
				icon.createTray();
				
				Thread guiRefresh = new Thread(new RefreshData());
				guiRefresh.start();
			}
		}
		getIPs();
		doUPnP();
		doInitialDirectoryScan();
		joinXMPP();
		enqueueThreadStart();

		// -- Create main thread
		Globals.main = new MainWorker();
		Globals.main.start();
		
		// Register Shutdown hook to gracefully close threads on ctrl-c
		Runtime.getRuntime().addShutdownHook(new Shutdown());

		try {
			Globals.bootupBarrier.await();
		} catch (InterruptedException ie) {

		} catch (BrokenBarrierException bbe) {
			Globals.log.addMsg(bbe.toString(), 4);
		}

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

			for (P2Pdevice d : Globals.p2pDevices) {
				d.closeTransport();
			}

		} catch (InterruptedException ie) {
			Globals.log.addMsg("Couldn't wait for all threads to cleanly shut down! Oh what a mess... Bye!", 1);
		}
		
		cleanup(0);
		
		try {
			Globals.shutdownBarrier.await();
		} catch (InterruptedException ie) {

		} catch (BrokenBarrierException bbe) {
			Globals.log.addMsg(bbe.toString(), 4);
		}
	}

	private static void getCmdLineArgs(String[] cmds) {
		String last = "";

		for (String current : cmds) {
			if (current.equals("-h")) {
				System.out.println(getHelpString());
				quit(9);
			}
			if (last.equals("-c")) {
				Globals.config = current;
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
	 * Reads config from config.xml
	 * 
	 * @return true, if config file exists and is valid, else false
	 */
	private static boolean getConfig() {
		try {
			File xmlConfig = new File(Globals.config);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlConfig);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("xmpp-account");

			for (int i = 0; i < nList.getLength(); i++) {

				Node n = nList.item(i);

				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) n;

					String val;

					val = getTagValue("server", eElement);
					if (val != null) {
						Globals.server = val;
					} else {
						Globals.log.addMsg("Required value missing in config: xmpp-account -> server", 1);
						return false;
					}

					val = getTagValue("user", eElement);
					if (val != null) {
						Globals.user = val;
					} else {
						Globals.log.addMsg("Required value missing in config: xmpp-account -> user", 1);
						return false;
					}

					val = getTagValue("pass", eElement);
					if (val != null) {
						Globals.pass = val;
					} else {
						Globals.log.addMsg("Required value missing in config: xmpp-account -> pass", 1);
						return false;
					}

					val = getTagValue("resource", eElement);
					if (val != null) {
						Globals.resource = val;
					} else {
						Random rand = new Random();
						Globals.resource += rand.nextInt(99999);
					}

					val = getTagValue("port", eElement);
					if (val != null) {
						// Sanity check
						if (Integer.parseInt(val) > 1024
								&& Integer.parseInt(val) < 65536) {
							Globals.port = Integer.parseInt(val);
						} else {
							Globals.log.addMsg("XMPP Port not in valid range: xmpp-account -> port (should be 1025-65535)", 1);
							return false;
						}
					}

				}
			}

			nList = doc.getElementsByTagName("conference");

			for (int i = 0; i < nList.getLength(); i++) {

				Node n = nList.item(i);

				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) n;

					String val;

					val = getTagValue("server", eElement);
					if (val != null) {
						Globals.conference_server = val;
					} else {
						Globals.log.addMsg("Required value missing in config: conference -> server", 1);
						return false;
					}

					val = getTagValue("channel", eElement);
					if (val != null) {
						Globals.conference_channel = val;
					} else {
						Globals.log.addMsg("Required value missing in config: conference -> channel", 1);
						return false;
					}

					val = getTagValue("pass", eElement);
					if (val != null)
						Globals.conference_pass = val;

				}
			}

			nList = doc.getElementsByTagName("pg-settings");

			for (int i = 0; i < nList.getLength(); i++) {

				Node n = nList.item(i);

				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) n;

					String val;

					val = getTagValue("share", eElement);
					if (val != null)
						Globals.rootDirectory = val;

					val = getTagValue("extIP", eElement);
					if (val != null) {
						String parts[] = val.split("\\.");
						if (parts.length != 4) {
							Globals.log.addMsg("Not a valid extIP address in config: " + val);
							return false;
						}
						int test;
						for (String part : parts) {
							test = Integer.parseInt(part);
							if (test < 0 || test > 255) {
								Globals.log.addMsg("Not a valid extIP address in config: " + val);
								return false;
							}
						}
						Globals.remoteIP4 = val;
					}
					
					val = getTagValue("intIP4", eElement);
					if (val != null) {
						String parts[] = val.split("\\.");
						if (parts.length != 4) {
							Globals.log.addMsg("Not a valid intIP4 address in config: " + val);
							return false;
						}
						int test;
						for (String part : parts) {
							test = Integer.parseInt(part);
							if (test < 0 || test > 255) {
								Globals.log.addMsg("Not a valid intIP4 address in config: " + val);
								return false;
							}
						}
						Globals.localIP4 = val;
					}

					val = getTagValue("port", eElement);
					if (val != null) {
						// Sanity check
						if (Integer.parseInt(val) > 1024
								&& Integer.parseInt(val) < 65536) {
							Globals.p2pPort = Integer.parseInt(val);
						} else {
							Globals.log.addMsg("P2P Port not in valid range: xmpp-account -> port (should be 1025-65535)", 1);
							return false;
						}
					}
					
					val = getTagValue("upnp", eElement);
					if (val != null) {
						// Sanity check
						if (val.equals("yes")) {
							Globals.doUPnP = true;
						} else if (val.equals("no")){
							Globals.doUPnP = false;
						} else {
							Globals.log.addMsg("<upnp> tag should be \"yes\" or \"no\"", 1);
							return false;
						}
					}
				}
			}
			Globals.log.addMsg("Using " + Globals.config + "... Ignoring commandline arguments!", 3);
		} catch (FileNotFoundException fnf) {
			Globals.log.addMsg(
					"Could not find config file! Creating sample file...", 1);
			Globals.log.addMsg("Please edit config.smp to your needs and copy to config.xml", 4);
			createSampleConfig();
			return false;
		} catch (NumberFormatException nfe) {
			Globals.log.addMsg("Value is not a number: " + nfe.getMessage() + " - Correct your config file!", 1);
			return false;
		} catch (NullPointerException npe) {
			Globals.log.addMsg("Value missing in config: " + npe, 1);
			Globals.log.addMsg("Please edit config.smp to your needs and copy to config.xml", 4);
			createSampleConfig();
			return false;
		} catch (Exception ioe) {
			Globals.log.addMsg("Error reading config: " + ioe, 1);
			return false;
		}
		return true;
	}

	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = nlList.item(0);

		if (nValue == null || nValue.getNodeValue() == null)
			return null;

		return nValue.getNodeValue();
	}
	
	private static void createConfig() {
		try {
			File conf = new File("config.xml");
			conf.delete();
			conf.createNewFile();
			FileWriter fw = new FileWriter(conf);
			BufferedWriter bw = new BufferedWriter(fw);
			String sample = "<?xml version=\"1.0\"?>\n" + "<peergroup>\n"
					+ "\t<xmpp-account>\n"
					+ "\t\t<server>" + Globals.server + "</server>\n"
					+ "\t\t<user>" + Globals.user + "</user>\n"
					+ "\t\t<pass>" + Globals.pass + "</pass>\n"
					+ "\t\t<resource></resource>\n" + "\t\t<port></port>\n"
					+ "\t</xmpp-account>\n" + "\t<conference>\n"
					+ "\t\t<server>" + Globals.conference_server + "</server>\n"
					+ "\t\t<channel>" + Globals.conference_channel + "</channel>\n"
					+ "\t\t<pass>" + Globals.conference_pass + "</pass>\n" + "\t</conference>\n"
					+ "\t<pg-settings>\n" + "\t\t<share>./share/</share>\n"
					+ "\t\t<extIP></extIP>\n" + "\t\t<intIP4></intIP4>\n" + "\t\t<port>" + Globals.p2pPort + "</port>\n"
					+ "\t\t<upnp>yes</upnp>\n\t</pg-settings>\n" + "</peergroup>";
			bw.write(sample, 0, sample.length());
			bw.close();
		} catch (Exception e) {
			Globals.log.addMsg("Couldn't create config", 1);
			quit(12);
		}
	}

	private static void createSampleConfig() {
		try {
			File conf = new File("config.smp");
			conf.delete();
			conf.createNewFile();
			FileWriter fw = new FileWriter(conf);
			BufferedWriter bw = new BufferedWriter(fw);
			String sample = "<?xml version=\"1.0\"?>\n" + "<peergroup>\n"
					+ "\t<xmpp-account>\n"
					+ "\t\t<server>jabber-server</server>\n"
					+ "\t\t<user>username</user>\n"
					+ "\t\t<pass>password</pass>\n"
					+ "\t\t<resource></resource>\n" + "\t\t<port></port>\n"
					+ "\t</xmpp-account>\n" + "\t<conference>\n"
					+ "\t\t<server>conference-server</server>\n"
					+ "\t\t<channel>channelname</channel>\n"
					+ "\t\t<pass></pass>\n" + "\t</conference>\n"
					+ "\t<pg-settings>\n" + "\t\t<share>./share/</share>\n"
					+ "\t\t<extIP></extIP>\n" + "\t\t<intIP4></intIP4>\n" + "\t\t<port>53333</port>\n"
					+ "\t\t<upnp>yes</upnp>\n\t</pg-settings>\n" + "</peergroup>";
			bw.write(sample, 0, sample.length());
			bw.close();
		} catch (Exception e) {
			Globals.log.addMsg("Couldn't create sample config", 1);
			quit(12);
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
	 * queries the external IP from 185.11.136.10
	 * If neither an IP was set nor one was detected, Peergroup exits.
	 */
	private static void getIPs() {
		// Get local IP
		try {
			InetAddress local = null;
			boolean foundIP = false;
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();
				Enumeration<InetAddress> addresses = iface.getInetAddresses();

				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
						if(Globals.localIP4.equals("")){
							//If local IPv4 address is not explicitly set, use the first one you find
							local = addr;
							foundIP = true;
						}else{
							//If specific local IPv4 address is chosen, check if it exists
							if(Globals.localIP4.equals(addr.getHostAddress())){
								local = addr;
								foundIP = true;
							}
						}
					}
					if(foundIP)
						break;
				}
				if(foundIP)
					break;
			}

			if (local == null) {
				Globals.log.addMsg("Could not determine local IP address!");
				System.exit(0);
			}

			Globals.localIP4 = local.getHostAddress();
			Globals.log.addMsg("Detected local IPv4 as: " + Globals.localIP4);
		} catch (SocketException se) {
			Globals.log.addMsg("Cannot get local IP: " + se, 4);
		}

		// Get external IPv4
		if (!Globals.remoteIP4.equals("")) {
			Globals.log.addMsg("External IPv4 was manually set, skipping the guessing.");
			return;
		}
		try {
			//URL whatismyip = new URL("http://files.smashnet.de/getIP.php");
			Socket whatismyip = new Socket("185.11.136.10",8000);
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.getInputStream()));

			Globals.remoteIP4 = in.readLine();
			whatismyip.close();
			Globals.log.addMsg("Found external IPv4 address: " + Globals.remoteIP4);
		} catch (Exception e) {
			Globals.log.addMsg("Couldn't get external IPv4 address! " + e + " Try setting it manually!", 1);
			quit(1);
		}
		
		// Get external IPv6
		if (!Globals.remoteIP6.equals("")) {
			Globals.log.addMsg("External IPv6 was manually set, skipping the guessing.");
			return;
		}
		try {
			//URL whatismyip = new URL("http://files.smashnet.de/getIP.php");
			Socket whatismyip = new Socket("2a03:2900:2:1::13a",8001);
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.getInputStream()));

			Globals.remoteIP6 = in.readLine();
			whatismyip.close();
			Globals.log.addMsg("Found external IPv6 address: " + Globals.remoteIP6);
		} catch (Exception e) {
			if(!Globals.remoteIP4.equals("")){
				Globals.log.addMsg("Couldn't get external IPv6 address! Using IPv4 only...", 1);
			}else{
				Globals.log.addMsg("Couldn't get any external address! Try setting them manually!", 1);
				quit(1);
			}
		}
	}

	private static void doUPnP() {
		if (!Globals.doUPnP)
			return;
		int discoveryTimeout = 5000; // 5 secs to receive a response from
		// devices
		try {
			InternetGatewayDevice[] IGDs = InternetGatewayDevice
					.getDevices(discoveryTimeout);
			if (IGDs != null) {
				// let's the the first device found
				Globals.igd = IGDs[0];
				Globals.log.addMsg("Found device "
						+ Globals.igd.getIGDRootDevice().getModelName());

				// now let's open the port
				// we assume that localHostIP is something else than 127.0.0.1
				boolean mapped = Globals.igd.addPortMapping("Peergroup",
						null, Globals.p2pPort, Globals.p2pPort,
						Globals.localIP4, 0, "TCP");
				if (mapped) {
					Globals.log.addMsg("Port " + Globals.p2pPort
							+ " mapped to " + Globals.localIP4);
				}
			} else {
				Globals.log.addMsg(
						"No UPnP enabled router found! You probably have to forward port "
								+ Globals.p2pPort
								+ " in your router manually to your local IP "
								+ Globals.localIP4, 4);
			}
		} catch (IOException ex) {
			Globals.log.addMsg("Failed to open port via UPnP: Maybe the port is already open, or your router does not support UPnP?", 4);
		} catch (UPNPResponseException respEx) {
			Globals.log.addMsg("Failed to open port via UPnP: Maybe the port is already open, or your router does not support UPnP?", 4);
		}
	}

	private static void doInitialDirectoryScan() {
		Globals.folders = new LinkedList<String>();
		Globals.log.addMsg("Doing initial scan of share directory...");
		File root = Storage.getInstance().getDirHandle();
		iterateFilesOnInitScan(root);
	}

	private static void iterateFilesOnInitScan(File dir) {
		for (File newFile : dir.listFiles()) {
			if (newFile.getName().charAt(0) == '.') {
				continue;
			}
			if (newFile.isDirectory()) {
				Globals.folders.add(newFile.getPath());
				iterateFilesOnInitScan(newFile);
			} else if (newFile.isFile()) {
				Globals.log.addMsg("Found: " + newFile.getName(), 2);
				handleLocalFileInitScan(new FSRequest(
						Globals.LOCAL_FILE_INITSCAN, newFile.getPath()));
			}
		}
	}
	
	private static void handleLocalFileInitScan(FSRequest request) {
		String newEntry = StorageWorker.getPurePath(request.getContent());
		if (Storage.getInstance().fileExists(newEntry) != null) {
			Globals.log.addMsg("InitScan: File already exists, ignoring!", 4);
			return;
		}
		Storage.getInstance().newFileFromLocal(newEntry);
	}

	private static void joinXMPP() {
		Network xmppNet = Network.getInstance();
		if (!xmppNet.isConnected() || !xmppNet.isLoggedIn()) {
			// There must have been some error while connecting,
			// so we need to shut down Peergroup
			quit(5);
		}
		xmppNet.joinMUC(Globals.user, Globals.conference_pass, Globals.conference_channel + "@" + Globals.conference_server);
		xmppNet.sendMUCmessage("Hi, I'm a peergroup client. I do awesome things :-)");
	}

	private static void enqueueThreadStart() {
		Globals.requestQueue.offer(new Request(Globals.START_THREADS));
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
