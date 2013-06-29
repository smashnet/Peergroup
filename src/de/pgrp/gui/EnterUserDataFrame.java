package de.pgrp.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JButton;

import de.pgrp.core.Globals;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Font;
import java.util.concurrent.BrokenBarrierException;

public class EnterUserDataFrame extends JFrame {

	
	private static final long serialVersionUID = -3512918865856302797L;
	private static EnterUserDataFrame instance = new EnterUserDataFrame();
	private JPanel contentPane;
	private JTextField txtJID;
	private JPasswordField pwdJID;
	private JTextField txtConference;
	private JPasswordField pwdConference;

	
	public static EnterUserDataFrame getInstance(){
		return instance;
	}
	
	/**
	 * Create the frame.
	 */
	public EnterUserDataFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 285);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblPleaseEnterYour = new JLabel("Please enter your data to connect with your peers:");
		lblPleaseEnterYour.setBounds(12, 12, 387, 15);
		contentPane.add(lblPleaseEnterYour);
		
		JLabel lblJabberId = new JLabel("Jabber ID:");
		lblJabberId.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblJabberId.setBounds(87, 61, 60, 15);
		contentPane.add(lblJabberId);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblPassword.setBounds(83, 88, 64, 15);
		contentPane.add(lblPassword);
		
		JLabel lblConferenceChannel = new JLabel("Conference channel:");
		lblConferenceChannel.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblConferenceChannel.setBounds(17, 143, 130, 15);
		contentPane.add(lblConferenceChannel);
		
		JLabel lblPassword_1 = new JLabel("Password:");
		lblPassword_1.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblPassword_1.setBounds(83, 170, 64, 15);
		contentPane.add(lblPassword_1);
		
		txtJID = new JTextField();
		txtJID.setToolTipText("Like: user@jabber.server.tld");
		txtJID.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(txtJID.getText().equals("(required)"))
						txtJID.setText("");
			}
		});
		txtJID.setText("(required)");
		txtJID.setBounds(176, 59, 256, 19);
		contentPane.add(txtJID);
		txtJID.setColumns(10);
		
		pwdJID = new JPasswordField();
		pwdJID.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(String.valueOf(pwdJID.getPassword()).equals("(required)"))
					pwdJID.setText("");
			}
		});
		pwdJID.setText("(required)");
		pwdJID.setBounds(176, 86, 256, 19);
		contentPane.add(pwdJID);
		pwdJID.setColumns(10);
		
		txtConference = new JTextField();
		txtConference.setToolTipText("Like: channel@conference.jabber.server.tld");
		txtConference.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(txtConference.getText().equals("(required)"))
					txtConference.setText("");
			}
		});
		txtConference.setText("(required)");
		txtConference.setBounds(176, 141, 256, 19);
		contentPane.add(txtConference);
		txtConference.setColumns(10);
		
		pwdConference = new JPasswordField();
		pwdConference.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(String.valueOf(pwdConference.getPassword()).equals("(optional)"))
					pwdConference.setText("");
			}
		});
		pwdConference.setText("(optional)");
		pwdConference.setBounds(176, 168, 256, 19);
		contentPane.add(pwdConference);
		pwdConference.setColumns(10);
		
		JButton btnSave = new JButton("Save");
		btnSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//Get Jabber ID
				String jid[] = txtJID.getText().split("@");
				if(jid.length != 2){
					JOptionPane.showMessageDialog(new JFrame(), "Not a valid Jabber ID!", "Input Error", JOptionPane.WARNING_MESSAGE);
					return;
				}else{
					Globals.user = jid[0];
					Globals.server = jid[1];
				}
				
				//Get Conference Channel
				String conference[] = txtConference.getText().split("@");
				if(conference.length != 2){
					JOptionPane.showMessageDialog(new JFrame(), "Not a valid Conference Channel!", "Input Error", JOptionPane.WARNING_MESSAGE);
					return;
				}else{
					Globals.conference_channel = conference[0];
					Globals.conference_server = conference[1];
				}
				
				//Get Passwords
				Globals.pass = String.valueOf(pwdJID.getPassword());
				Globals.conference_pass = String.valueOf(pwdConference.getPassword());
				
				try {
					Globals.inputBarrier.await();
				} catch (InterruptedException ie) {

				} catch (BrokenBarrierException bbe) {
					Globals.log.addMsg(bbe.toString(), 4);
				}
				
				EnterUserDataFrame.getInstance().dispose();
			}
		});
		btnSave.setBounds(315, 222, 117, 25);
		contentPane.add(btnSave);
	}
}
