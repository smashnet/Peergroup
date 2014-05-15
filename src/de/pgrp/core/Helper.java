package de.pgrp.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.validator.routines.InetAddressValidator;

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
	 * Perform sanity checks on the read properties and save them in the Globals class.
	 * 
	 * @param props the properies object
	 * @return true if all sanity checks are passed, else false
	 */
	private static boolean sanityCheckProperties(Properties props) {
		// ---------- Check XMPP server ----------
		Globals.xmpp_server = props.getProperty("xmpp-server");
		if(Globals.xmpp_server == null || Globals.xmpp_server == "") {
			Globals.log.addMsg("xmpp-server missing in config.", Logger.RED);
			return false;
		}else if(!Globals.xmpp_server.contains(".")) {
			Globals.log.addMsg("Not a valid xmpp-server entry: " + Globals.xmpp_server, Logger.RED);
			return false;
		}
		
		// ---------- Check XMPP user ----------
		Globals.xmpp_user = props.getProperty("xmpp-user");
		if(Globals.xmpp_user == null || Globals.xmpp_user == "") {
			Globals.log.addMsg("xmpp-user missing in config.", Logger.RED);
			return false;
		}
		
		// ---------- Check XMPP pass ----------
		Globals.xmpp_pass = props.getProperty("xmpp-pass");
		if(Globals.xmpp_pass == null || Globals.xmpp_pass == "") {
			Globals.log.addMsg("xmpp-pass missing in config.", Logger.RED);
			return false;
		}
		
		// ---------- Check XMPP resource ----------
		if(props.getProperty("xmpp-resource") != null && props.getProperty("xmpp-resource") != "") {
			Globals.xmpp_resource = props.getProperty("xmpp-resource");
		}
		
		// ---------- Check XMPP port ----------
		if(props.getProperty("xmpp-port") != null && props.getProperty("xmpp-port") != "") {
			Globals.xmpp_port = Integer.valueOf(props.getProperty("xmpp-port"));
			if(!(Globals.xmpp_port > 1024 && Globals.xmpp_port < 65536)) {
				Globals.log.addMsg("XMPP port not in range 1024-65535", Logger.RED);
				return false;
			}
		}
		
		// ---------- Check conference server ----------
		Globals.conference_server = props.getProperty("conference-server");
		if(Globals.conference_server == null) {
			Globals.log.addMsg("conference-server missing in config.", Logger.RED);
			return false;
		}else if(!Globals.conference_server.contains(".")){
			Globals.log.addMsg("Not a valid conference-server entry: " + Globals.conference_server, Logger.RED);
			return false;
		}
		
		// TODO: Complete sanity/existence checks
		// ---------- Check conference channel ----------
		Globals.conference_channel = props.getProperty("conference-channel");
		// ---------- Check conference pass ----------
		Globals.conference_pass = props.getProperty("conference-pass");
		// ---------- Check share directory ----------
		Globals.shareDirectory = props.getProperty("pg-ShareDirectory");
		
		// ---------- Check external IP4 address ----------
		Globals.externalIP4 = props.getProperty("pg-extIP4");
		if(!InetAddressValidator.getInstance().isValid(Globals.externalIP4)) {
			Globals.log.addMsg("Not a valid IPv4 address: " + Globals.externalIP4,	Logger.RED);
			return false;
		}
		
		// ---------- Check external IP6 address ----------
		Globals.externalIP6 = props.getProperty("pg-ext6");
		if(!InetAddressValidator.getInstance().isValid(Globals.externalIP6)) {
			Globals.log.addMsg("Not a valid IPv6 address: " + Globals.externalIP4,	Logger.RED);
			return false;
		}
		
		// ---------- Check internal IP4 address ----------
		Globals.internalIP4 = props.getProperty("pg-intIP4");
		if(!InetAddressValidator.getInstance().isValid(Globals.internalIP4)) {
			Globals.log.addMsg("Not a valid IPv4 address: " + Globals.internalIP4,	Logger.RED);
			return false;
		}
		
		// ---------- Check P2P port ----------
		Globals.p2pPort = Integer.valueOf(props.getProperty("pg-P2Pport"));
		if(!(Globals.p2pPort > 1024 && Globals.p2pPort < 65536)) {
			Globals.log.addMsg("P2P port not in range 1024-65535", Logger.RED);
			return false;
		}
		
		// ---------- Check encrypt data transfer ----------
		Globals.encryptDataTransfers = Boolean.parseBoolean(props.getProperty("pg-encryptTransfers"));
		// ---------- Check do port forwarding ----------
		Globals.doUPnP = Boolean.parseBoolean(props.getProperty("pg-doUPnP"));
		
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
}
