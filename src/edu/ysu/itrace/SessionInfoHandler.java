package edu.ysu.itrace;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.eclipse.core.resources.ResourcesPlugin;

public class SessionInfoHandler {
	//session info
	private final String sessionIDPattern = "yyyyMMdd'T'HHmmss-SSSSZ";
	private String sessionID = null;
	private String sessionPurpose = null;
	private String sessionDescrip = null;
	
	//developer info
	private String devUsername = null;
	private String devName = null;
	
	//needed booleans
	boolean hasSessionInfo = false;
	boolean hasDevInfo = false;
	boolean isConfigured = false;
	
	public SessionInfoHandler() {
		UIManager.put("swing.boldMetal", new Boolean(false)); //make UI font plain
	}
	
	//Getters
	public String getSessionID() {
		return sessionID;
	}
	
	public String getSessionPurpose() {
		return sessionPurpose;
	}
	
	public String getSessionDescrip() {
		return sessionDescrip;
	}
	
	public String getDevUsername() {
		return devUsername;
	}
	
	public String getDevName() {
		return devName;
	}
	
	public boolean isConfigured() {
		return isConfigured;
	}
	
	//UI methods/Setters
	public void setSessionID() {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(sessionIDPattern);
			sessionID = formatter.format(new Date());
		} catch (IllegalArgumentException e) {
			sessionID = sessionIDPattern;
		}
	}
	
	protected void sessionUI() {
		setSessionID();
		//textfields needed for UI
		JTextField sessionIDText = new JTextField(getSessionID());
		sessionIDText.setEditable(false);
		JTextArea sessionDescripText = new JTextArea(4,5);
		sessionDescripText.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(sessionDescripText);
        
		//Radio Button List
		JRadioButton newFeature = new JRadioButton("New Feature");
		JRadioButton bugFix = new JRadioButton("Bug Fix");
		JRadioButton refactoring = new JRadioButton("Refactoring");
		JRadioButton genComp = new JRadioButton("General Comprehension");
		JRadioButton other = new JRadioButton("Other");
		
		//Group the buttons
		ButtonGroup radioList = new ButtonGroup();
		radioList.add(newFeature);
		radioList.add(bugFix);
		radioList.add(refactoring);
		radioList.add(genComp);
		radioList.add(other);
		
		//Add to a JPanel
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		radioPanel.add(newFeature);
		radioPanel.add(bugFix);
		radioPanel.add(refactoring);
		radioPanel.add(genComp);
		radioPanel.add(other);
		
		//Add everything to main JPanel
		JPanel sessionPanel = new JPanel(); //main panel
		sessionPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		
		c.gridx = 0;
		c.gridy = 0;
		sessionPanel.add(new JLabel("Generated Session ID:"),c);
		c.gridx++;
		sessionPanel.add(sessionIDText,c);
		c.gridx = 0;
		c.gridy++;
		sessionPanel.add(new JLabel("Session Purpose (select one):"),c);
		c.gridx++;
		sessionPanel.add(radioPanel,c);
		c.gridx = 0;
		c.gridy++;
		sessionPanel.add(new JLabel("Enter the Session Description:"),c);
		c.gridx++;
		c.fill = GridBagConstraints.HORIZONTAL;
		sessionPanel.add(scrollPane,c);
		
		final int selection = JOptionPane.showConfirmDialog(null, sessionPanel, 
				"Enter the Current Session Info.",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (selection == JOptionPane.OK_OPTION) {
			sessionDescrip = sessionDescripText.getText();
			if (newFeature.isSelected()) {
				sessionPurpose = newFeature.getText();
			} else if (bugFix.isSelected()) {
				sessionPurpose = bugFix.getText();
			} else if (refactoring.isSelected()) {
				sessionPurpose = refactoring.getText();
			} else if (genComp.isSelected()) {
				sessionPurpose = genComp.getText();
			} else if (other.isSelected()) {
				sessionPurpose = other.getText();
			} else {
				sessionPurpose = new String();
				System.out.println("Warning! "
						+ "Your Session Purpose has not been selected.");
			}
			hasSessionInfo = true;
		}  
	}
	
	protected void developerUI() {
		//Textfields needed for data entry
		JTextField devUserText = new JTextField();
		JTextField devNameText = new JTextField();
    	
		//Add text fields to main JPanel
		JPanel devPanel = new JPanel();
		devPanel.setLayout(new BoxLayout(devPanel, BoxLayout.Y_AXIS)); //vertically align
		devPanel.add(new JLabel("Developer Username (Alphanumeric Characters Only):"));
		devPanel.add(devUserText);
		devPanel.add(new JLabel("Developer Name (Optional, Alpha/Space Characters Only):"));
		devPanel.add(devNameText);
		
		final int selection = JOptionPane.showConfirmDialog(null, devPanel, 
				"Enter the Developer Info.",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (selection == JOptionPane.OK_OPTION) {
			Pattern p = Pattern.compile("[^a-zA-Z0-9]");
			Pattern p2 = Pattern.compile("[^A-Za-z ]");
			if (!p.matcher(devUserText.getText()).find() &&
					!devUserText.getText().isEmpty() &&
					!p2.matcher(devNameText.getText()).find()) {
				devUsername = devUserText.getText();
				devName = devNameText.getText();
			} else {
				//error
				JOptionPane.showMessageDialog(null, "You "
						+ "have Non-alphanumeric Characters "
						+ "in your Developer Username or you have "
						+ "Non-alphabetic/Space Characters "
						+ "in your Developer Name or you have not "
						+ "provided a Developer Username. Please re-enter "
						+ "your Developer Information.", "Error", 
						JOptionPane.ERROR_MESSAGE);
				//re-enter devUsername
				developerUI();
			}
			hasDevInfo = true;
		}
	}
	
	public void config() {
		sessionUI();
		developerUI();
		if (hasSessionInfo && hasDevInfo) {
			isConfigured = true;
		}
	}
	
	public void export() throws IOException {
		//export session data if session info has been configured
		if (isConfigured()) {
			String workspaceLocation =
					ResourcesPlugin.getWorkspace().getRoot().getLocation()
					.toString();
			File outFile = new File(workspaceLocation + "/" + getSessionID() +
					"/session-info-" + devUsername + "-" +
					getSessionID() + ".txt");
			if (outFile.getParentFile().mkdir()) {
				if (outFile.exists()) {
					System.out.println("You cannot overwrite this file. If you "
							+ "wish to continue, delete the file " + "manually.");
					return;
				} else {
					System.out.println("Putting files at "
							+ outFile.getAbsolutePath());
					outFile.createNewFile();
					
					//export to new file
					BufferedWriter writer = null;
					try {
						writer = new BufferedWriter( new FileWriter(outFile.getAbsolutePath()));
						writer.write("Session ID,Session Purpose,Session Descrip,"
								+ "Developer Username,Developer Name");
						writer.newLine();
						writer.write(getSessionID() + "," + sessionPurpose + ","
								+ sessionDescrip + "," + devUsername + "," + devName);
					} catch ( IOException e) {
						throw new IOException("Failed to write session info. to file.");
					}
					finally {
						try {
							if ( writer != null) writer.close( );
						} catch ( IOException e) {
							throw new IOException("Failed to write session info. to file.");
						}
					}
				}
			} else {
				throw new IOException("Failed to create directory "
						+ outFile.getParent());
			}
		} else {
			//handled in ControlView startTracking() method
		}
	}
	
	public void reset() {
		sessionPurpose = null;
		sessionDescrip = null;
		devUsername = null;
		devName = null;
		hasSessionInfo = false;
		hasDevInfo = false;
		isConfigured = false;
	}
}
