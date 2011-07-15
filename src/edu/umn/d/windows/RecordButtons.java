/*
 * RecordButtons.java
 *
 * Created on February 19, 2006, 9:08 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package edu.umn.d.windows;

import javax.swing.JButton;

/**
 *
 * @author nels2426
 */
public class RecordButtons {
    JButton capture, start, stop;
    /** Creates a new instance of RecordButtons */
    public RecordButtons(ControlWindow cw) {
        capture = new JButton("Take screenshot");
        capture.setMaximumSize(cw.BUTTON_SIZE);
        capture.setMinimumSize(cw.BUTTON_SIZE);
        capture.setPreferredSize(cw.BUTTON_SIZE);
        capture.addActionListener(cw);
        
        start = new JButton("Start recording");
        start.setMaximumSize(cw.BUTTON_SIZE);
        start.setMinimumSize(cw.BUTTON_SIZE);
        start.setPreferredSize(cw.BUTTON_SIZE);
        start.addActionListener(cw);
        
        stop = new JButton("Stop recording");
        stop.setMaximumSize(cw.BUTTON_SIZE);
        stop.setMinimumSize(cw.BUTTON_SIZE);
        stop.setPreferredSize(cw.BUTTON_SIZE);
        stop.addActionListener(cw);
    }
    
    /**
     * @return A JButton with maximum size equal to cw.BUTTON_SIZE and cw as its action listener and text equal to 'Take screenshot'.
     */
    public JButton getScreenshotButton(){
        return capture;
    }
    
    /**
     * @return A JButton with maximum size equal to cw.BUTTON_SIZE and cw as its action listener and text equal to 'Start recording'.
     */
    public JButton getStartButton(){
        return start;
    }
    
    /**
     * @return A JButton with maximum size equal to cw.BUTTON_SIZE and cw as its action listener and text equal to 'Stop recording'.
     */
    public JButton getStopButton(){
        return stop;
    }
}
