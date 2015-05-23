package edu.ysu.itrace;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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

public class SessionInfoHandler {
	//session info
	private final String sessionIDPattern = "yyyyMMdd'T'HHmmss-SSSSZ";
	private String sessionPurpose = null;
	private String sessionDescrip = null;
	
	//developer info
	private String devUsername = null;
	private String devName = null;
	
	//use default constructor
	
	//Getters
	public String getSessionID() {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(sessionIDPattern);
			return formatter.format(new Date());
		} catch (IllegalArgumentException e) {
			return  sessionIDPattern;
		}
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
	
	//UI methods/Setters
	protected void sessionUI() {
		UIManager.put("swing.boldMetal", new Boolean(false)); //make font plain
		
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
        		//sessionPurpose remains null
        		System.out.println("Warning! "
        				+ "Your Session Purpose has not been selected.");
        	};
        }  
	}
	
	protected void developerUI() {
		UIManager.put("swing.boldMetal", new Boolean(false)); //make font plain
    	
		//Textfields needed for data entry
    	JTextField devUserText = new JTextField();
    	JTextField devNameText = new JTextField();
    	
    	//Add text fields to main JPanel
    	JPanel devPanel = new JPanel();
    	devPanel.setLayout(new BoxLayout(devPanel, BoxLayout.Y_AXIS)); //vertically align
        devPanel.add(new JLabel("Developer Username (Alphanumeric Characters Only):"));
        devPanel.add(devUserText);
        devPanel.add(new JLabel("Developer Name (Optional):"));
        devPanel.add(devNameText);

		final int selection = JOptionPane.showConfirmDialog(null, devPanel, 
                "Enter the Developer Info.",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (selection == JOptionPane.OK_OPTION) {
        	Pattern p = Pattern.compile("[^a-zA-Z0-9]");
        	if (!p.matcher(devUserText.getText()).find()) {
        		devUsername = devUserText.getText();
        	} else {
        		//warning
        		JOptionPane.showMessageDialog(null, "You "
        				+ "have Non-alphanumeric Characters "
        				+ "in your Developer Username. Please re-enter "
        				+ "your Developer Information.", "Error", 
        				JOptionPane.ERROR_MESSAGE);
        		//re-enter devUsername
        		developerUI();
        	}
        	devName = devNameText.getText();
        }
	}
	
	public void config() {
		sessionUI();
		developerUI();
	}
	
	public void export() {
		//export session data
	}
}
