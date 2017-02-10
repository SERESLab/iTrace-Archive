package edu.ysu.itrace.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

public class MainFilterFrame implements ActionListener{
	//necessary to add a larger font to enable people with larger screens to be able to read the text
	public static Font biggerFont = new Font("Arial",Font.PLAIN, 14);
	protected Font betterFont = biggerFont;
	private static void createAndShowFilterUI(){
		changeFontBasedOnScreen();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth() * .25;
		double height = screenSize.getHeight() * .45;

		int w = (int) width;
		int h = (int) height;
		JFrame mainFrame = new JFrame("Fixation Filter Settings");
		mainFrame.setFont(biggerFont);
		mainFrame.setSize(w, h);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
		mainFrame.setLayout(new GridBagLayout());
		mainFrame.setResizable(false);
		GridBagConstraints c = new GridBagConstraints();

		//top section of frame should never need to change aside from drop down box
		JPanel unchangingPanel = new JPanel();
		unchangingPanel.setLayout(new GridLayout(2,0));
		unchangingPanel.setFont(biggerFont);
		int preferredHeight = (int) (height * .10);
		unchangingPanel.setPreferredSize(new Dimension((w-5), preferredHeight));

		JLabel label = new JLabel("Select type of filter you wish to edit");
		label.setVisible(true);
		label.setFont(biggerFont);
		//c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 0.5;
		//c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0;
		c.gridy = 0;
		unchangingPanel.add(label);
		mainFrame.add(unchangingPanel, c);


		//creating border for both panels
		Border compound;
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		compound =  BorderFactory.createCompoundBorder(raisedbevel, loweredbevel);
		unchangingPanel.setBorder(compound);



		//add other choices to below variable
		String[] choices = { "Basic Fixation Filter", "Placeholder", "Placeholder" };

		//actual drop down box implementation
		final JComboBox<String> fixationChoices = new JComboBox<String>(choices);
		fixationChoices.setFont(biggerFont);
		unchangingPanel.add(fixationChoices);

		//bottom section of frame, will change with drop down box selection but will have default state of BasicFixationFilter

		BasicFixationFilter basic = new BasicFixationFilter();
		JPanel changingFrame = basic.getFilter();
		//c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		mainFrame.add(changingFrame, c);
		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				basic.applyChanges();
			}
		});
		c.anchor = GridBagConstraints.LINE_END;
		c.gridy = 2;
		applyButton.setFont(biggerFont);
		mainFrame.add(applyButton, c);
		mainFrame.pack();

	}

	public static void changeFontBasedOnScreen(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if(screenSize.width == 3000 && screenSize.height == 2000){
			biggerFont = new Font("Arial", Font.PLAIN, 20);
		}
		else if(screenSize.width == 3840 && screenSize.height == 2160){
			biggerFont = new Font("Arial", Font.PLAIN, 17);
		}
		else if(screenSize.width == 2048){
			biggerFont = new Font("Arial", Font.PLAIN, 15);
		}
	}
	public static Font getFont(){
		return biggerFont;
	}


	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			createAndShowFilterUI();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

}
