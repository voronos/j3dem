package edu.umn.d.windows;
import javax.swing.*;
import java.awt.*;
/**
 *  Creates a panel displaying keyboard and mouse control commands
 * @author  Mark Pendergast
 * @version 1.0 February 2003
 *
 */
public class InstructionPanel extends JPanel {
    /**
     * Creates a panel displaying keyboard and mouse control commands.
     */
    public InstructionPanel() {
        Font bf = new Font("Dialog",Font.PLAIN,10);
        setLayout(new GridLayout(4,3));
        
        add(new JLabel("Left turn - Left click on the left side of the image.",JLabel.CENTER));
        add(new JLabel("Forward - Left click on the top side of the image.",JLabel.CENTER));
        add(new JLabel("Right turn - Left click on the right of the image.",JLabel.CENTER));
        
        add(new JLabel("Roll/bank left - Middle click on the left side of the image.",JLabel.CENTER));
        add(new JLabel("Backward - Left click on the bottom side of the image.",JLabel.CENTER));
        add(new JLabel("Roll/bank right - Middle click on the right side.",JLabel.CENTER));
        
        add(new JLabel("Increase altitude - Right click on the top side.",JLabel.CENTER));
        add(new JLabel("Decrease altitude - Right click on the bottom side.",JLabel.CENTER));
        add(new JLabel("Strafe Left - Right click on left side.",JLabel.CENTER));
        add(new JLabel("Strafe Right - Right click on right side.",JLabel.CENTER));
        add(new JLabel("Dive - Middle click on top side.",JLabel.CENTER));
        add(new JLabel("Climb - Middle click on bottom side.",JLabel.CENTER));
        add(new JLabel("",JLabel.CENTER));
        
        for(int i = 0; i < getComponentCount(); i++)
            getComponent(i).setFont(bf);
    }
    
}
