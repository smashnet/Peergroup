package de.pgrp.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LogWindow extends JFrame {

	private static final long serialVersionUID = -2824997479308895105L;
	public static LogWindow instance = new LogWindow();
	private JPanel contentPane;
	private JTextArea console;
	private JScrollPane scrolly;

	/**
	 * Create the frame.
	 */
	public LogWindow() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 700, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		console = new JTextArea();
		console.setBackground(Color.DARK_GRAY);
		console.setForeground(Color.ORANGE);
		console.setEditable(false);
		console.setLineWrap(true);
		console.setBounds(6, 6, 680, 420);
		
		scrolly = new JScrollPane(console);
		scrolly.setBounds(6, 6, 688, 420);
		contentPane.add(scrolly);
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LogWindow.getInstance().dispose();
			}
		});
		btnClose.setBounds(577, 438, 117, 29);
		contentPane.add(btnClose);
	}
	
	public static LogWindow getInstance(){
		return instance;
	}
	
	public void addText(String text){
		this.console.append(text);
	}
}
