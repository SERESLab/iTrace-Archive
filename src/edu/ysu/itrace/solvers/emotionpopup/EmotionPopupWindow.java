package edu.ysu.itrace.solvers.emotionpopup;

import edu.ysu.itrace.gaze.IGazeResponse;

import java.awt.Image;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * @author nmh4553
 */
public class EmotionPopupWindow extends Thread {
    private IEmotionPopupHandler handler;
    private IGazeResponse response;
    
    public EmotionPopupWindow(IEmotionPopupHandler handler, IGazeResponse response) {
        this.handler = handler;
        this.response = response;
    }
    
    public void run() {
        JFrame f=new JFrame("Emotion Selection");
        f.setAlwaysOnTop(true);
        f.setLocation(0, 0);
        
        // TODO: Data structure for icons that handles loading of images.
        String[] icons = {"Joy", "Anger", "Disgust", "Surprise", "Fear", "Sadness", "Contempt"};
        Collections.shuffle(Arrays.asList(icons));
        
        Object[] options = {EmotionPopupWindow.getImageIcon(icons[0]),
                            EmotionPopupWindow.getImageIcon(icons[1]),
                            EmotionPopupWindow.getImageIcon(icons[2]),
                            EmotionPopupWindow.getImageIcon(icons[3]),
                            EmotionPopupWindow.getImageIcon(icons[4]),
                            EmotionPopupWindow.getImageIcon(icons[5]),
                            EmotionPopupWindow.getImageIcon(icons[6])};
        int selected = JOptionPane.showOptionDialog(f,
            "Pick the emoji that best describes your current mood:",
            "Current Mood",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            null);
        
        handler.writeResponse(response, icons[selected], icons);
    }
    
    public static ImageIcon getImageIcon(String name) {
    	Bundle bundle = Platform.getBundle("edu.ysu.itrace");
    	URL url = FileLocator.find(bundle, new Path("res/" + name + ".png"), null);
    	Image image = null;
    	try {
    		image = ImageIO.read(url);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	return image != null ? new ImageIcon(image) : null;
    }
}       
