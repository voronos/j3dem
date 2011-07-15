/*
 * CameraButtons.java
 *
 * Created on February 19, 2006, 9:26 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package edu.umn.d.windows;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

/**
 *
 * @author nels2426
 */
public class CameraButtons {
    JRadioButton fly, orbit;
    /** Creates a new instance of CameraButtons */
    public CameraButtons(ControlWindow cw) {
        fly = new JRadioButton("Fly");
        fly.addActionListener(cw);
        orbit = new JRadioButton("Orbit");
        orbit.addActionListener(cw);
        orbit.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(fly);
        group.add(orbit);
    }
    
    public JRadioButton getFly(){
        return fly;
    }
    
    public JRadioButton getOrbit(){
        return orbit;
    }
    
}
