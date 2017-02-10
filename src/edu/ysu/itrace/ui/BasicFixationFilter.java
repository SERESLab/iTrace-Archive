import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import java.awt.*;

public class BasicFixationFilter extends FixationFilters {
	JPanel basicFilter = new JPanel(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	private static Font biggerFont = MainFilterFrame.getFont();
	private JTextField thresholdDurationTextField = new JTextField(5);
	private JSlider r;
	private JTextField radiusTextField = new JTextField(5);
	private JTextField thresholdChangeTextField = new JTextField(5);




	public BasicFixationFilter(){
		basicFilter.setVisible(true);
		basicFilter.setFont(biggerFont);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth() * .25;
		double height = screenSize.getHeight() * .50;

		int preferredWidth, preferredHeight;
		preferredWidth = (int) (width * .99);
		preferredHeight = (int) (height * .75);
		basicFilter.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

		//creating slider for r variable as described in github issue
		int rMin = 1;
		int rMax = 15;
		int rDefault = 5;
		r = new JSlider(JSlider.HORIZONTAL, rMin, rMax, rDefault);
		r.setMajorTickSpacing(2);
		r.setMinorTickSpacing(1);
		r.setPaintTicks(true);
		r.setPaintLabels(true);
		JPanel sliderPanel = new JPanel(new BorderLayout());
		JLabel sliderLabel = new JLabel("Number of samples before and after current sample to average difference vector");
		sliderLabel.setFont(biggerFont);
		sliderPanel.setFont(biggerFont);
		sliderPanel.setVisible(true);
	//	sliderPanel.addChangeListener(new SliderListener());
		sliderPanel.add(r, BorderLayout.CENTER);
		sliderPanel.add(sliderLabel, BorderLayout.NORTH);
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		basicFilter.add(sliderPanel, c);

		//creating text field for radius in px
		JPanel radiusPanel = new JPanel(new GridBagLayout());
		JPanel outsidePanel = new JPanel(new BorderLayout());
		radiusPanel.setFont(biggerFont);
		outsidePanel.setFont(biggerFont);
		radiusTextField.setFont(biggerFont);
		radiusTextField.setText("10");
		JLabel beforeRadiusBox = new JLabel("Minimum distance between two fixations: ");
		JLabel afterRadiusBox = new JLabel("px");
		beforeRadiusBox.setFont(biggerFont);
		afterRadiusBox.setFont(biggerFont);
		radiusPanel.add(beforeRadiusBox);
		radiusPanel.add(radiusTextField);
		radiusPanel.add(afterRadiusBox);
		outsidePanel.add(radiusPanel, BorderLayout.CENTER);
		c.gridy = 1;
		basicFilter.add(radiusPanel, c);


		//creating another text field for duration Threshold
		JPanel thresholdDurationPanel = new JPanel(new GridBagLayout());
		thresholdDurationTextField.setText("60");
		JLabel beforeThresholdDurationBox = new JLabel("Minimum fixation duration for a fixation: ");
		JLabel afterThresholdDurationBox = new JLabel("ms");
		thresholdDurationPanel.setFont(biggerFont);
		thresholdDurationTextField.setFont(biggerFont);
		beforeThresholdDurationBox.setFont(biggerFont);
		afterThresholdDurationBox.setFont(biggerFont);
		thresholdDurationPanel.add(beforeThresholdDurationBox);
		thresholdDurationPanel.add(thresholdDurationTextField);
		thresholdDurationPanel.add(afterThresholdDurationBox);
		c.gridy = 2;
		basicFilter.add(thresholdDurationPanel, c);


		//creating a text field for gaze change threshold
		JPanel thresholdChangePanel = new JPanel(new GridBagLayout());
		thresholdChangeTextField.setText("35");
		JLabel beforeThresholdChangeBox = new JLabel("Minimum change in gaze for fixation: ");
		JLabel afterThresholdChangeBox = new JLabel("px");
		thresholdChangePanel.setFont(biggerFont);
		thresholdChangeTextField.setFont(biggerFont);
		beforeThresholdChangeBox.setFont(biggerFont);
		afterThresholdChangeBox.setFont(biggerFont);
		thresholdChangePanel.add(beforeThresholdChangeBox);
		thresholdChangePanel.add(thresholdChangeTextField);
		thresholdChangePanel.add(afterThresholdChangeBox);
		c.gridy = 3;
		basicFilter.add(thresholdChangePanel, c);

		//button for saving configuration
		//TODO
	/*	JPanel buttonPanel = new JPanel(new GridBagLayout());
		//buttonPanel.setMaximumSize(new Dimension(395, 50));
		JPanel blankPanel = new JPanel();
		JButton saveConfigButton = new JButton("Save Config");
		JButton loadConfigButton = new JButton("Load Config");
		saveConfigButton.setSize(new Dimension(100,50));
		loadConfigButton.setSize(new Dimension(100,50));
		saveConfigButton.setFont(biggerFont);
		loadConfigButton.setFont(biggerFont);
		buttonPanel.add(blankPanel);
		buttonPanel.add(saveConfigButton);
		buttonPanel.add(blankPanel);
		buttonPanel.add(loadConfigButton);
		c. gridy = 4;
		basicFilter.add(buttonPanel, c);*/




		//creating various title borders for each panel
		TitledBorder rSlider;
		rSlider = BorderFactory.createTitledBorder("Sample Space");
		rSlider.setTitleFont(biggerFont);
		sliderPanel.setBorder(rSlider);

		TitledBorder radiusField;
		radiusField = BorderFactory.createTitledBorder("Minimum Gaze Radius");
		radiusField.setTitleFont(biggerFont);
		radiusPanel.setBorder(radiusField);

		TitledBorder thresholdDurationField;
		thresholdDurationField = BorderFactory.createTitledBorder("Minimum Fixation Duration Threshold");
		thresholdDurationField.setTitleFont(biggerFont);
		thresholdDurationPanel.setBorder(thresholdDurationField);

		TitledBorder thresholdChangeField;
		thresholdChangeField = BorderFactory.createTitledBorder("Minimum Change Threshold");
		thresholdChangeField.setTitleFont(biggerFont);
		thresholdChangePanel.setBorder(thresholdChangeField);


		//border for the whole panel to separate it from the top and bottom of the Main filter frame
		Border compound;
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		compound =  BorderFactory.createCompoundBorder(raisedbevel, loweredbevel);
		basicFilter.setBorder(compound);



	super.filter = basicFilter;
	}

	public String filterName(){
		return "Basic Fixation Filter";
	}
	public void applyChanges(){
		//TODO Apply to Jenna's values
		String thresholdDurationText = thresholdDurationTextField.getText();
		String thresholdChangeText = thresholdChangeTextField.getText();
		String radiusText = radiusTextField.getText();
		int rValue = r.getValue();
		//System.out.println(thresholdDurationText + thresholdChangeText + radiusText + rValue);
	}

}
