package edu.ysu.itrace.solvers.emotionpopup;

import edu.ysu.itrace.gaze.IGazeResponse;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * @author nmh4553
 */
public class EmotionPopupWindow{
    private final ClassLoader cl = this.getClass().getClassLoader();
    
    // TODO: Inline this into EmotionPopupHandler.
    public EmotionPopupWindow(IEmotionPopupHandler handler, IGazeResponse response){
        JFrame f=new JFrame("Emotion Selection");
        
        String[] icons = {"Joy.png", "Anger.png", "Disgust.png", "Surprise.png", "Fear.png", "Sadness.png", "Contempt.png"};
        Collections.shuffle(Arrays.asList(icons));
        
        Object[] options = {new ImageIcon(cl.getResource(icons[0])),
                    new ImageIcon(cl.getResource(icons[1])),
                    new ImageIcon(cl.getResource(icons[2])),
                    new ImageIcon(cl.getResource(icons[3])),
                    new ImageIcon(cl.getResource(icons[4])),
                    new ImageIcon(cl.getResource(icons[5])),
                    new ImageIcon(cl.getResource(icons[6]))};
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
}       
