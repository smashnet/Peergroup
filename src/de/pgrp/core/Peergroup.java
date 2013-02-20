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
		
		Constants.log.addMsg(
				"Starting " + Constants.PROGNAME + " " + Constants.VERSION
				+ " on " + os + " " + System.getProperty("os.version")
				+ " with Java " + java_version, 2);

		getCmdLineArgs(args);
		if(!getConfig()){
			//In this case the config file either does not exist, or it has wrong format/missing values
			if(Constants.useGUI){
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
					Constants.inputBarrier.await();
				} catch (InterruptedException ie) {

				} catch (BrokenBarrierException bbe) {
					Constants.log.addMsg(bbe.toString(), 4);
				}
				
				Constants.inputBarrier.reset();
				
				//Create config file from entered values to avoid reentering the data each start
				createConfig();
				
				PGTrayIcon icon = PGTrayIcon.getInstance();
				icon.createTray();
			}else{
				//In headless mode we shutdown Peergroup
				quit(10);
			}
		}else{
			//Here we have a valid config file, and can thus continue either with or without GUI
			if(Constants.useGUI){
				PGTrayIcon icon = PGTrayIcon.getInstance();
				icon.createTray();
			}
		}
		getIPs();
		doUPnP();
		doInitialDirectoryScan();
		joinXMPP();
		enqueueThreadStart();

		// -- Create main thread
		Constants.main = new MainWorker();
		Constants.main.start();
		
		// Register Shutdown hook to gracefully close threads on ctrl-c
		Runtime.getRuntime().addShutdownHook(new Shutdown());

		try {
			Constants.bootupBarrier.await();
		} catch (InterruptedException ie) {

		} catch (BrokenBarrierException bbe) {
			Constants.log.addMsg(bbe.toString(), 4);
		}

		// -- Wait for threads to terminate (only happens through SIGINT/SIGTERM
		// see handler above)
		try {
			if (!Constants.serverMode)
				Constants.storage.join();

			Constants.network.join();
			Constants.thriftClient.join();
			Constants.main.join();
			if (Constants.enableModQueue) {
				Constants.modQueue.join();
			}

			for (P2Pdevice d : Constants.p2pDevices) {
				d.closeTransport();
			}

		} catch (InterruptedException ie) {
			Constants.log.addMsg("Couldn't wait for all threads to cleanly shut down! Oh what a mess... Bye!", 1);
		}
		
		cleanup(0);
		
		try {
			Constants.shutdownBarrier.await();
		} catch (InterruptedException ie) {

		} catch (BrokenBarrierException bbe) {
			Constants.log.addMsg(bbe.toString(), 4);
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
				Constants.config = current;
			}
			if (current.equals("-s")) {
				Constants.serverMode = true;
				Constants.log.addMsg("Running in server mode", 2);
			}
			if (current.equals("-noGUI")) {
				Constants.useGUI = false;
				Constants.log.addMsg("Running in headless mode", 2);
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
			File xmlConfig = new File(Constants.config);
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
						Constants.server = val;
					} else {
						Constants.log.addMsg("Required value missing in config: xmpp-account -> server", 1);
						return false;
					}

					val = getTagValue("user", eElement);
					if (val != null) {
						Constants.user = val;
					} else {
						Constants.log.addMsg("Required value missing in config: xmpp-account -> user", 1);
						return false;
					}

					val = getTagValue("pass", eElement);
					if (val != null) {
						Constants.pass = val;
					} else {
						Constants.log.addMsg("Required value missing in config: xmpp-account -> pass", 1);
						return false;
					}

					val = getTagValue("resource", eElement);
					if (val != null) {
						Constants.resource = val;
					} else {
						Random rand = new Random();
						Constants.resource += rand.nextInt(99999);
					}

					val = getTagValue("port", eElement);
					if (val != null) {
						// Sanity check
						if (Integer.parseInt(val) > 1024
								&& Integer.parseInt(val) < 65536) {
							Constants.port = Integer.parseInt(val);
						} else {
							Constants.log.addMsg("XMPP Port not in valid range: xmpp-account -> port (should be 1025-65535)", 1);
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
						Constants.conference_server = val;
					} else {
						Constants.log.addMsg("Required value missing in config: conference -> server", 1);
						return false;
					}

					val = getTagValue("channel", eElement);
					if (val != null) {
						Constants.conference_channel = val;
					} else {
						Constants.log.addMsg("Required value missing in config: conference -> channel", 1);
						return false;
					}

					val = getTagValue("pass", eElement);
					if (val != null)
						Constants.conference_pass = val;

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
						Constants.rootDirectory = val;

					val = getTagValue("extIP", eElement);
					if (val != null) {
						String parts[] = val.split(".");
						if (parts.length != 4) {
							Constants.log.addMsg("Not a valid IP address in config: " + val);
							return false;
						}
						int test;
						for (String part : parts) {
							test = Integer.parseInt(part);
							if (test < 0 || test > 255) {
								Constants.log.addMsg("Not a valid IP address in config: " + val);
								return false;
							}
						}
						Constants.ipAddress = val;
					}

					val = getTagValue("port", eElement);
					if (val != null) {
						// Sanity check
						if (Integer.parseInt(val) > 1024
								&& Integer.parseInt(val) < 65536) {
							Constants.p2pPort = Integer.parseInt(val);
						} else {
							Constants.log.addMsg("P2P Port not in valid range: xmpp-account -> port (should be 1025-65535)", 1);
							return false;
						}
					}
				}
			}
			Constants.log.addMsg("Using " + Constants.config
					+ "... Ignoring commandline arguments!", 3);
		} catch (FileNotFoundException fnf) {
			Constants.log.addMsg(
					"Could not find config file! Creating sample file...", 1);
			Constants.log.addMsg("Please edit config.smp to your needs and rename it to config.xml", 4);
			createSampleConfig();
			return false;
		} catch (NumberFormatException nfe) {
			Constants.log.addMsg("Value is not a number: " + nfe.getMessage() + " - Correct your config file!", 1);
			return false;
		} catch (NullPointerException npe) {
			Constants.log.addMsg("Value missing in config: " + npe, 1);
			Constants.log.addMsg("Please edit config.smp to your needs and rename it to config.xml", 4);
			createSampleConfig();
			return false;
		} catch (Exception ioe) {
			Constants.log.addMsg("Error reading config: " + ioe, 1);
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
					+ "\t\t<server>" + Constants.server + "</server>\n"
					+ "\t\t<user>" + Constants.user + "</user>\n"
					+ "\t\t<pass>" + Constants.pass + "</pass>\n"
					+ "\t\t<resource></resource>\n" + "\t\t<port></port>\n"
					+ "\t</xmpp-account>\n" + "\t<conference>\n"
					+ "\t\t<server>" + Constants.conference_server + "</server>\n"
					+ "\t\t<channel>" + Constants.conference_channel + "</channel>\n"
					+ "\t\t<pass>" + Constants.conference_pass + "</pass>\n" + "\t</conference>\n"
					+ "\t<pg-settings>\n" + "\t\t<share>./share/</share>\n"
					+ "\t\t<extIP></extIP>\n" + "\t\t<port>" + Constants.p2pPort + "</port>\n"
					+ "\t</pg-settings>\n" + "</peergroup>";
			bw.write(sample, 0, sample.length());
			bw.close();
		} catch (Exception e) {
			Constants.log.addMsg("Couldn't create config", 1);
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
					+ "\t\t<extIP></extIP>\n" + "\t\t<port>53333</port>\n"
					+ "\t</pg-settings>\n" + "</peergroup>";
			bw.write(sample, 0, sample.length());
			bw.close();
		} catch (Exception e) {
			Constants.log.addMsg("Couldn't create sample config", 1);
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
		return out;
	}

	/**
	 * If the external IP was not set by the cmd-line argument, this function
	 * queries the external IP from http://files.smashnet.de/getIP.php If
	 * neither an IP was set nor one was detected, Peergroup exits.
	 */
	private static void getIPs() {
		// Get local IP
		try {
			InetAddress local = null;
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements() && local == null) {
				NetworkInterface iface = ifaces.nextElement();
				Enumeration<InetAddress> addresses = iface.getInetAddresses();

				while (addresses.hasMoreElements() && local == null) {
					InetAddress addr = addresses.nextElement();
					if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
						local = addr;
					}
				}
			}

			if (local == null) {
				System.out.println("Could not determine local address!");
				System.exit(0);
			}

			Constants.localIP = local.getHostAddress();
		} catch (SocketException se) {
			Constants.log.addMsg("Cannot get local IP: " + se, 4);
		}

		// Get external IP
		if (!Constants.ipAddress.equals("")) {
			Constants.log.addMsg("External IP was manually set, skipping the guessing.");
			return;
		}
		try {
			URL whatismyip = new URL("http://files.smashnet.de/getIP.php");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

			Constants.ipAddress = in.readLine();
			Constants.log.addMsg("Found external IP: " + Constants.ipAddress);
		} catch (Exception e) {
			Constants.log.addMsg("Couldn't get external IP! " + e + " Try setting it manually!", 1);
			quit(1);
		}
	}

	private static void doUPnP() {
		if (!Constants.doUPnP)
			return;
		int discoveryTimeout = 5000; // 5 secs to receive a response from
		// devices
		try {
			InternetGatewayDevice[] IGDs = InternetGatewayDevice
					.getDevices(discoveryTimeout);
			if (IGDs != null) {
				// let's the the first device found
				Constants.igd = IGDs[0];
				Constants.log.addMsg("Found device "
						+ Constants.igd.getIGDRootDevice().getModelName());

				// now let's open the port
				// we assume that localHostIP is something else than 127.0.0.1
				boolean mapped = Constants.igd.addPortMapping("Peergroup",
						null, Constants.p2pPort, Constants.p2pPort,
						Constants.localIP, 0, "TCP");
				if (mapped) {
					Constants.log.addMsg("Port " + Constants.p2pPort
							+ " mapped to " + Constants.localIP);
				}
			} else {
				Constants.log.addMsg(
						"No UPnP enabled router found! You probably have to forward port "
								+ Constants.p2pPort
								+ " in your router manually to your local IP "
								+ Constants.localIP, 4);
			}
		} catch (IOException ex) {
			Constants.log.addMsg("Failed to open port: " + ex, 4);
			Constants.log.addMsg("Maybe the port is already open?", 4);
		} catch (UPNPResponseException respEx) {
			Constants.log.addMsg("Failed to open port: " + respEx, 4);
			Constants.log.addMsg("Maybe the port is already open?", 4);
		}
	}

	private static void doInitialDirectoryScan() {
		Constants.folders = new LinkedList<String>();
		Constants.log.addMsg("Doing initial scan of share directory...");
		File root = Storage.getInstance().getDirHandle();
		iterateFilesOnInitScan(root);
	}

	private static void iterateFilesOnInitScan(File dir) {
		for (File newFile : dir.listFiles()) {
			if (newFile.getName().charAt(0) == '.') {
				continue;
			}
			if (newFile.isDirectory()) {
				Constants.folders.add(newFile.getPath());
				iterateFilesOnInitScan(newFile);
			} else if (newFile.isFile()) {
				Constants.log.addMsg("Found: " + newFile.getName(), 2);
				handleLocalFileInitScan(new FSRequest(
						Constants.LOCAL_FILE_INITSCAN, newFile.getPath()));
			}
		}
	}
	
	private static void handleLocalFileInitScan(FSRequest request) {
		String newEntry = StorageWorker.getPurePath(request.getContent());
		if (Storage.getInstance().fileExists(newEntry) != null) {
			Constants.log.addMsg("InitScan: File already exists, ignoring!", 4);
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
		xmppNet.joinMUC(Constants.user, Constants.conference_pass, Constants.conference_channel + "@" + Constants.conference_server);
		xmppNet.sendMUCmessage("Hi, I'm a peergroup client. I do awesome things :-)");
	}

	private static void enqueueThreadStart() {
		Constants.requestQueue.offer(new Request(Constants.START_THREADS));
	}

	public static void cleanup(int no) {
		if (Constants.quitting)
			return;

		Constants.quitting = true;

		if (Constants.igd != null) {
			try {
				boolean unmapped = Constants.igd.deletePortMapping(null, Constants.p2pPort, "TCP");
				if (unmapped) {Constants.log.addMsg("Released port mapping for Peergroup on port "
							+ Constants.p2pPort);
				}
			} catch (IOException ioe) {
				Constants.log.addMsg("Error unmapping port: " + ioe, 4);
			} catch (UPNPResponseException respEx) {
				Constants.log.addMsg("Error unmapping port: " + respEx, 4);
			}
		}

		Constants.log.closeLog();
	}
	
	public static void quit(int no) {
		if (Constants.quitting)
			return;

		Constants.quitting = true;

		if (Constants.igd != null) {
			try {
				boolean unmapped = Constants.igd.deletePortMapping(null,
						Constants.p2pPort, "TCP");
				if (unmapped) {
					Constants.log
					.addMsg("Released port mapping for Peergroup on port "
							+ Constants.p2pPort);
				}
			} catch (IOException ioe) {
				Constants.log.addMsg("Error unmapping port: " + ioe, 4);
			} catch (UPNPResponseException respEx) {
				Constants.log.addMsg("Error unmapping port: " + respEx, 4);
			}
		}

		Constants.log.closeLog();
		System.exit(no);
	}
}
