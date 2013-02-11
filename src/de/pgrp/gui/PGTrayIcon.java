package de.pgrp.gui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

public class PGTrayIcon {
	
	private static PGTrayIcon instance = new PGTrayIcon();
	private Image img;
	private TrayIcon ico;
	
	public PGTrayIcon(){
		this.img = Toolkit.getDefaultToolkit().getImage("art/pg_logo.png");
		this.ico = new TrayIcon(img,"PGTray");
	}
	
	public static PGTrayIcon getInstance(){
		return instance;
	}
	
	public void createTray(){
		if(SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			ico.setImageAutoSize(true);
			
			PopupMenu menu = new PopupMenu();

		    MenuItem messageItem = new MenuItem("Configure");
		    messageItem.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        JOptionPane.showMessageDialog(null, "Here we'd show the configuration frame.");
		      }
		    });
		    menu.add(messageItem);

		    MenuItem closeItem = new MenuItem("Close");
		    closeItem.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        System.exit(0);
		      }
		    });
		    menu.add(closeItem);
		    ico = new TrayIcon(img, "SystemTray Demo", menu);
		    ico.setImageAutoSize(true);

		    try {
		      tray.add(ico);
		    } catch (AWTException e) {
		      System.err.println("TrayIcon could not be added.");
		    }
		}
	}
}
