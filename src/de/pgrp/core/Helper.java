package de.pgrp.core;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.BrokenBarrierException;

import org.apache.commons.validator.routines.InetAddressValidator;

import de.pgrp.gui.EnterUserDataFrame;
import de.pgrp.gui.PGTrayIcon;
import de.pgrp.gui.RefreshData;

public class Helper {

	/**
	 * Gets the path of a file including the shared folder, and returns the path
	 * without the shared folder.
	 * 
	 * E.g. ./share/log/myLogFile123.log -> log/myLogFile123.log
	 * 
	 * @param entry
	 *            The path to a file in the shared folder
	 * @return The path to this file relative to the shared folder
	 */
	protected static String getPurePath(String entry) {
		int rootLength = Globals.shareDirectory.length();
		return entry.substring(rootLength, entry.length());
	}
	
	/**
	 * Recursively finds files and folders
	 * @param dir the origin directory of the recursion
	 */
	protected static void iterateFilesOnInitScan(File dir) {
		for (File newFile : dir.listFiles()) {
			if (newFile.getName().charAt(0) == '.') {
				//Ignore (hidden) files starting with a "."
				continue;
			}
			if (newFile.isDirectory()) {
				//Directory found: Add directory and recurse
				Globals.folders.add(newFile.getPath());
				iterateFilesOnInitScan(newFile);
			} else if (newFile.isFile()) {
				//File found: Add file
				Globals.log.addMsg("Found: " + newFile.getName(), 2);
				handleLocalFileInitScan(new FSRequest(Globals.LOCAL_FILE_INITSCAN, newFile.getPath()));
			}
		}
	}
	
	/**
	 * Add file to StorageDB if not added already.
	 * @param request the FSRequest containing the filename detected during the inital scan
	 */
	protected static void handleLocalFileInitScan(FSRequest request) {
		String newEntry = Helper.getPurePath(request.getContent());
		if (Storage.getInstance().fileExists(newEntry) != null) {
			Globals.log.addMsg("InitScan: File already exists, ignoring!", 4);
			return;
		}
		Storage.getInstance().newFileFromLocal(newEntry);
	}
	
	/**
	 * Initialize basic GUI elements:
	 * <ul>
	 * 	<li>TrayIcon</li>
	 * 	<li>GUI refresh thread</li>
	 * </ul>
	 */
	protected static void initGUI() {
		//Create tray icon
		PGTrayIcon icon = PGTrayIcon.getInstance();
		icon.createTray();
		
		//Start thread that periodically refreshes information shown in GUI
		Thread guiRefresh = new Thread(new RefreshData());
		guiRefresh.run();
	}
	
	/**
	 * Reads config from config.xml
	 * 
	 * @return true, if config file exists and is valid, else false
	 */
	protected static boolean readConfigFile() {
		try {
			Properties savedProps = new Properties();
			FileInputStream in = new FileInputStream(Globals.configFile);
			savedProps.load(in);
			in.close();
			
			return sanityCheckProperties(savedProps);
		} catch (FileNotFoundException e) {
			Globals.log.addMsg("Could not find config file. Creating sample config in \"sample.cfg\"", Logger.RED);
		} catch (IOException e) {
			Globals.log.addMsg("Error reading config file.", Logger.RED);
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * If there is no valid config file found on startup, we decide depending on
	 * GUI or headless mode:
	 * <ul>
	 * 	<li>GUI: Open corresponding frame so that user can enter data.</li>
	 * 	<li>Headless: Request user to fix config and quit.</li>
	 * </ul>
	 */
	protected static void handleMissingConfigFile() {
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
			saveProperties("peergroup.cfg");
			
			initGUI();
		}else{
			//In headless mode we shutdown Peergroup
			createSampleConfig();
			Peergroup.quit(10);
		}
	}
	
	/**
	 * Perform sanity checks on the read properties and save them in the Globals class.
	 * 
	 * @param props the properies object
	 * @return true if all sanity checks are passed, else false
	 */
	private static boolean sanityCheckProperties(Properties props) {
		// ---------- Check XMPP server ----------
		Globals.xmpp_server = props.getProperty("xmpp-server");
		if(Globals.xmpp_server == null || Globals.xmpp_server.equals("")) {
			Globals.log.addMsg("xmpp-server missing in config.", Logger.RED);
			return false;
		}else if(!Globals.xmpp_server.contains(".")) {
			Globals.log.addMsg("Not a valid xmpp-server entry: " + Globals.xmpp_server, Logger.RED);
			return false;
		}
		Globals.xmpp_server = Globals.xmpp_server.trim();
		
		// ---------- Check XMPP user ----------
		Globals.xmpp_user = props.getProperty("xmpp-user");
		if(Globals.xmpp_user == null || Globals.xmpp_user.equals("")) {
			Globals.log.addMsg("xmpp-user missing in config.", Logger.RED);
			return false;
		}
		Globals.xmpp_user = Globals.xmpp_user.trim();
		
		// ---------- Check XMPP pass ----------
		Globals.xmpp_pass = props.getProperty("xmpp-pass");
		if(Globals.xmpp_pass == null || Globals.xmpp_pass.equals("")) {
			Globals.log.addMsg("xmpp-pass missing in config.", Logger.RED);
			return false;
		}
		Globals.xmpp_pass = Globals.xmpp_pass.trim();
		
		// ---------- Check XMPP resource ----------
		if(props.getProperty("xmpp-resource") != null && !props.getProperty("xmpp-resource").equals("")) {
			Globals.xmpp_resource = props.getProperty("xmpp-resource");
		}
		Globals.xmpp_resource = Globals.xmpp_resource.trim();
		
		// ---------- Check XMPP port ----------
		if(props.getProperty("xmpp-port") != null && !props.getProperty("xmpp-port").equals("")) {
			Globals.xmpp_port = Integer.valueOf(props.getProperty("xmpp-port").trim());
			if(!(Globals.xmpp_port > 1024 && Globals.xmpp_port < 65536)) {
				Globals.log.addMsg("XMPP port not in range 1024-65535", Logger.RED);
				return false;
			}
		}
		
		// ---------- Check conference server ----------
		Globals.conference_server = props.getProperty("conference-server");
		if(Globals.conference_server == null || Globals.conference_server.equals("")) {
			Globals.log.addMsg("conference-server missing in config.", Logger.RED);
			return false;
		}else if(!Globals.conference_server.contains(".")){
			Globals.log.addMsg("Not a valid conference-server entry: " + Globals.conference_server, Logger.RED);
			return false;
		}
		Globals.conference_server = Globals.conference_server.trim();
		
		// ---------- Check conference channel ----------
		Globals.conference_channel = props.getProperty("conference-channel");
		if(Globals.conference_channel == null || Globals.conference_channel.equals("")) {
			Globals.log.addMsg("conference-channel missing in config.", Logger.RED);
			return false;
		}
		Globals.conference_channel = Globals.conference_channel.trim();
		
		// ---------- Check conference pass ----------
		Globals.conference_pass = props.getProperty("conference-pass");
		if(Globals.conference_pass == null || Globals.conference_pass.equals("")) {
			Globals.log.addMsg("conference-pass missing in config.", Logger.RED);
			return false;
		}
		Globals.conference_pass = Globals.conference_pass.trim();
		
		// ---------- Check share directory ----------
		if(props.getProperty("pg-ShareDirectory") != null && !props.getProperty("pg-ShareDirectory").equals(""))
			Globals.shareDirectory = props.getProperty("pg-ShareDirectory").trim();
		
		// ---------- Check external IP4 address ----------
		if(props.getProperty("pg-extIP4") != null && !props.getProperty("pg-extIP4").equals("")) {
			if(!InetAddressValidator.getInstance().isValid(props.getProperty("pg-extIP4"))) {
				Globals.log.addMsg("Not a valid external IPv4 address.", Logger.RED);
				return false;
			}
			Globals.externalIP4 = props.getProperty("pg-extIP4").trim();
		}
		
		// ---------- Check external IP6 address ----------
		if(props.getProperty("pg-extIP6") != null && !props.getProperty("pg-extIP6").equals("")) {
			if(!InetAddressValidator.getInstance().isValid(props.getProperty("pg-extIP6"))) {
				Globals.log.addMsg("Not a valid external IPv6 address.", Logger.RED);
				return false;
			}
			Globals.externalIP6 = props.getProperty("pg-extIP6").trim();
		}
		
		// ---------- Check internal IP4 address ----------
		if(props.getProperty("pg-intIP4") != null && !props.getProperty("pg-intIP4").equals("")) {
			if(!InetAddressValidator.getInstance().isValid(props.getProperty("pg-intIP4"))) {
				Globals.log.addMsg("Not a valid internal IPv4 address.", Logger.RED);
				return false;
			}
			Globals.internalIP4 = props.getProperty("pg-intIP4").trim();
		}
		
		// ---------- Check P2P port ----------
		if(props.getProperty("pg-P2Pport") != null && !props.getProperty("pg-P2Pport").equals("")) {
			Globals.p2pPort = Integer.valueOf(props.getProperty("pg-P2Pport").trim());
			if(!(Globals.p2pPort > 1024 && Globals.p2pPort < 65536)) {
				Globals.log.addMsg("P2P port not in range 1024-65535", Logger.RED);
				return false;
			}
		}
		
		// ---------- Check encrypt data transfer ----------
		if(props.getProperty("pg-encryptTransfers") != null && !props.getProperty("pg-encryptTransfers").equals(""))
			Globals.encryptDataTransfers = Boolean.parseBoolean(props.getProperty("pg-encryptTransfers").trim());
		
		// ---------- Check do port forwarding ----------
		if(props.getProperty("pg-doUPnP") != null && !props.getProperty("pg-doUPnP").equals(""))
			Globals.doUPnP = Boolean.parseBoolean(props.getProperty("pg-doUPnP").trim());
		
		return true;
	}

	/**
	 * Write a sample config file to "sample.cfg" so the user only
	 * needs to insert his login credentials.
	 */
	protected static void createSampleConfig() {
		try {
			File conf = new File("sample.cfg");
			conf.delete();
			conf.createNewFile();
			FileWriter fw = new FileWriter(conf);
			BufferedWriter bw = new BufferedWriter(fw);
			String sample = 
					  "xmpp-server=          # XMPP server from your JID (e.g. jabber.ccc.de)\n"
					+ "xmpp-user=            # Username from your JID    (e.g. john.doe)\n"
					+ "xmpp-pass=            # Your XMPP password        (e.g. qwertz) just joking ;-P\n"
					+ "xmpp-resource=        # Resource name to be used (optional, default: peergroup[rnd])\n"
					+ "xmpp-port=            # XMPP server connection port (optional, default: 5222)\n\n"
					+ "conference-server=    # Conference server   (e.g. conference.ccc.de)\n"
					+ "conference-channel=   # Conference channel  (e.g. staff-meeting)\n"
					+ "conference-pass=      # Conference channel password\n\n"
					+ "pg-ShareDirectory=./share/  # Share directory\n"
					+ "pg-extIP4=                  # External IPv4 address (optional, default: auto-detect)\n"
					+ "pg-extIP6=                  # External IPv6 address (optional, default: auto-detect)\n"
					+ "pg-intIP4=                  # Internal IPv4 address (optional, default: auto-detect)\n"
					+ "pg-P2Pport=                 # Port used for incoming P2P connections (optional, default: random above 50000)\n"
					+ "pg-encryptTransfers=true    # Encrypt P2P file transfers\n"
					+ "pg-doUPnP=false             # Forward P2P port in NAT using UPnP if supported\n\n"
					+ "#Examples for IP declaration:\n"
					+ "#pg-extIP4=1.2.3.4\n"
					+ "#pg-extIP6=52a4\\:4233\\:e123\\:4d00\\:52ba\\:3560\\:4bb2\\:b183\n";
			bw.write(sample, 0, sample.length());
			bw.close();
		} catch (Exception e) {
			Globals.log.addMsg("Couldn't create sample config", 1);
			Peergroup.quit(12);
		}
	}
	
	/**
	 * Save current peergroup settings to file
	 * 
	 * @param fileName the filename
	 */
	protected static void saveProperties(String fileName) {
		Properties props = new Properties();
		props.put("xmpp-server", Globals.xmpp_server);
		props.put("xmpp-user", Globals.xmpp_user);
		props.put("xmpp-pass", Globals.xmpp_pass);
		props.put("xmpp-resource", Globals.xmpp_resource);
		props.put("xmpp-port", "" + Globals.xmpp_port);
		
		props.put("conference-server", Globals.conference_server);
		props.put("conference-channel", Globals.conference_channel);
		props.put("conference-pass", Globals.conference_pass);
		
		props.put("pg-ShareDirectory", Globals.shareDirectory);
		props.put("pg-encryptTransfers", "" + Globals.encryptDataTransfers);
		props.put("pg-extIP4", Globals.externalIP4);
		props.put("pg-extIP6", Globals.externalIP6);
		props.put("pg-intIP4", Globals.internalIP4);
		props.put("pg-P2Pport", "" + Globals.p2pPort);
		props.put("pg-doUPnP", "" + Globals.doUPnP);
		
		try {
			props.store(new FileOutputStream(fileName), null);
		} catch (FileNotFoundException e) {
			Globals.log.addMsg("Error saving properties.", Logger.RED);
			e.printStackTrace();
		} catch (IOException e) {
			Globals.log.addMsg("Error saving properties.", Logger.RED);
			e.printStackTrace();
		}
	}
	
	/**
	 * Detect internal IPv4 address and save to Globals.internalIP4
	 */
	protected static void getInternalIP4() {
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
						if(Globals.internalIP4.equals("")){
							//If local IPv4 address is not explicitly set, use the first one you find
							local = addr;
							foundIP = true;
						}else{
							//If specific local IPv4 address is chosen, check if it exists
							if(Globals.internalIP4.equals(addr.getHostAddress())){
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

			Globals.internalIP4 = local.getHostAddress();
			Globals.log.addMsg("Detected local IPv4 as: " + Globals.internalIP4);
		} catch (SocketException se) {
			Globals.log.addMsg("Cannot get local IP: " + se, 4);
		}
	}

	/**
	 * Detect external IPv4 address and save to Globals.externalIP4
	 */
	protected static void getExternalIP4() {
		try {
			Socket whatismyip = new Socket("37.120.160.33",17533);
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.getInputStream()));

			Globals.externalIP4 = in.readLine();
			whatismyip.close();
			Globals.log.addMsg("Found external IPv4 address: " + Globals.externalIP4);
		} catch (Exception e) {
			Globals.log.addMsg("Couldn't get external IPv4 address! " + e + " Try setting it manually!", 1);
			Peergroup.quit(1);
		}
	}

	/**
	 * Detect external IPv6 address and save to Globals.externalIP6
	 */
	protected static void getExternalIP6() {
		try {
			Socket whatismyip = new Socket("2a03:4000:6:3007::1",17533);
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.getInputStream()));

			Globals.externalIP6 = in.readLine();
			whatismyip.close();
			Globals.log.addMsg("Found external IPv6 address: " + Globals.externalIP6);
		} catch (Exception e) {
			if(!Globals.externalIP4.equals("")){
				Globals.log.addMsg("Couldn't get external IPv6 address! Using IPv4 only...", 1);
			}else{
				Globals.log.addMsg("Couldn't get any external address! Try setting them manually!", 1);
				Peergroup.quit(1);
			}
		}
	}
}
