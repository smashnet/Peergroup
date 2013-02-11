package de.pgrp.gui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JOptionPane;

public class PGTrayIcon {
	
	private static PGTrayIcon instance = new PGTrayIcon();
	private Image img;
	private TrayIcon ico;
	
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
		    		JOptionPane.showMessageDialog(null, "Here we'd show the configuration frame.");
		    	}
		    });
		    menu.add(openFolderItem);
		    
		    menu.addSeparator();
		    
		    MenuItem folderSizeItem = new MenuItem("Folder Size: x MB");
		    folderSizeItem.setEnabled(false);
		    MenuItem downRateItem = new MenuItem("Download: x KB/s");
		    downRateItem.setEnabled(false);
		    MenuItem upRateItem = new MenuItem("Upload: x KB/s");
		    upRateItem.setEnabled(false);
		    menu.add(folderSizeItem);
		    menu.add(downRateItem);
		    menu.add(upRateItem);
		    
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
}
