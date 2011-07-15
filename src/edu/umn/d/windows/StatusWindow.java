package edu.umn.d.windows;
import javax.swing.*;
import java.awt.*;
/**
 *
 *   This class provides a simple 2 line status window to display progress
 * information while loading data from files. A modeless JDialog is
 * used as the basis for the window
 *
 * @author  Mark Pendergast
 * @version 1.0 February 2003
 *
 */
public class StatusWindow extends JDialog {
    JLabel label1;
    JLabel label2;
    Font labelFont = new Font("Dialog",Font.BOLD,10);
    /**
     *   Create the status window
     *
     *  @param owner owning frame
     */
    public StatusWindow(JFrame owner) {
        super(owner,false);
        setSize(350,100);
        setTitle("Loading .....");
        label1 = new JLabel("Initializing graphics data",JLabel.CENTER);
        label2  = new JLabel("",JLabel.CENTER);
        label1.setFont(labelFont);
        label2.setFont(labelFont);
        add("North", label1);
        add("South", label2);
    }
    
    public StatusWindow(){
        setSize(350, 100);
        setTitle("Loading ....");
        label1 = new JLabel("Initializing graphics data",JLabel.CENTER);
        label2  = new JLabel("",JLabel.CENTER);
        label1.setFont(labelFont);
        label2.setFont(labelFont);
        add("North", label1);
        add("South", label2);
    }
    /**
     *   Set the top line text
     *
     *  @param value string to display on the upper line
     */
    public void setLabel1(String value) {
        label1.setText(value);
    }
    /**
     *   Set the bottom line text
     *
     *  @param value string to display on the upper line
     */
    public void setLabel2(String value) {
        label2.setText(value);
    }
    /**
     *   Set the bottom line text to read :
     *    value x of y
     *
     *  @param value string to display on the upper line
     *  @param x work completed
     *  @param y total work
     */
    
    public void setLabel2(String value,int x, int y) {
        label2.setText(value+" "+x+" of "+y);
    }
    
}