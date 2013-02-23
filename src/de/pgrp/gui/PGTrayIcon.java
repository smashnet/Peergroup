package de.pgrp.gui;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;

import de.pgrp.core.Globals;

public class PGTrayIcon {
	
	private static PGTrayIcon instance = new PGTrayIcon();
	private Image img;
	private TrayIcon ico;
	private MenuItem folderSizeItem;
	private MenuItem downRateItem;
	private MenuItem upRateItem;
	
	public PGTrayIcon(){
		URL imgURL = ClassLoader.getSystemResource("man.png");
		this.img = Toolkit.getDefaultToolkit().getImage(imgURL);
		this.ico = new TrayIcon(img,"PGTray");
	}
	
	public static PGTrayIcon getInstance(){
		return instance;
	}
	
	public void createTray(){
		if(SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			ico.setImageAutoSize(true);
			ico.setToolTip("Hi! Here's Peergroup!");
			
			PopupMenu menu = new PopupMenu();

		    MenuItem configureItem = new MenuItem("Configure");
		    configureItem.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {
		    		JOptionPane.showMessageDialog(null, "Here we'd show the configuration frame.");
		    	}
		    });
		    menu.add(configureItem);
		    
		    MenuItem openFolderItem = new MenuItem("Open shared folder");
		    openFolderItem.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {
		    		try {
						Desktop.getDesktop().open(new File(Globals.getAbsoluteShareFolderPath()));
					} catch (IOException e1) {
						Globals.log.addMsg("Could not open shared folder with Desktop! " + e1, 4);
					}
		    	}
		    });
		    if(!Desktop.isDesktopSupported()){
		    	openFolderItem.setEnabled(false);
		    }
		    menu.add(openFolderItem);
		    
		    folderSizeItem = new MenuItem("Folder Size: Waiting");
		    folderSizeItem.setEnabled(false);
		    downRateItem = new MenuItem("Download: Waiting");
		    downRateItem.setEnabled(false);
		    upRateItem = new MenuItem("Upload: Waiting");
		    upRateItem.setEnabled(false);
		    MenuItem showLogItem = new MenuItem("Show log window");
		    showLogItem.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {
		    		EventQueue.invokeLater(new Runnable() {
		    			public void run() {
		    				try {
		    					LogWindow.getInstance().setVisible(true);
		    				} catch (Exception e) {
		    					e.printStackTrace();
		    				}
		    			}
		    		});
		    	}
		    });
		    menu.add(showLogItem);
		    menu.addSeparator();
		    
		    menu.add(folderSizeItem);
		    //menu.add(downRateItem);
		    //menu.add(upRateItem);
		    
		    menu.addSeparator();

		    MenuItem closeItem = new MenuItem("Close");
		    closeItem.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {
		    		System.exit(0);
		    	}
		    });
		    menu.add(closeItem);
		    ico.setPopupMenu(menu);

		    try {
		    	tray.add(ico);
		    } catch (AWTException e) {
		    	System.err.println("TrayIcon could not be added.");
		    }
		    
		    ico.displayMessage("Peergroup", "Starting the magic...", MessageType.INFO);
		}
	}
	
	public void setFolderSize(int size){
		this.folderSizeItem.setLabel("Folder size: " + size + " MB");
	}
	public void setDownrate(double downrate){
		this.downRateItem.setLabel("Download: " + downrate + " kB/s");
	}
	public void setUprate(double uprate){
		this.upRateItem.setLabel("Upload: " + uprate + " kB/s");
	}
}
