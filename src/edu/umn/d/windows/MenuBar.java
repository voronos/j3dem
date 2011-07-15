/*
 * MenuBar.java
 *
 * Created on February 19, 2006, 9:33 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package edu.umn.d.windows;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

/**
 *
 * @author Nigel
 */
public class MenuBar extends JMenuBar{
    JMenu menu, removeMenu;
    JMenuItem menuItem;
    JRadioButtonMenuItem fly, orbit;
    ControlWindow cw;
    
    /** 
     * Adds main menu commands to this MenuBar.
     */
    public void addMainCommands(){
        removeMenu = new JMenu("Remove");
        menu = new JMenu("DEM");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription("Main file menu");
        
        menuItem = new JMenuItem("Add DEM");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Add");
        menuItem.addActionListener(cw);
        menu.add(menuItem);
        
        menu.add(removeMenu);
        
        menuItem = new JMenuItem("Quit");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(cw);
        menu.add(menuItem);
        
        add(menu);
    }
    
    /**
     * Adds options for controlling the texture and style of the model.
     */
    public void addTextureCommands(){
        menu = new JMenu("Texture");
        menuItem = new JMenuItem("Apply Image");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(cw);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Remove Image");
        menuItem.addActionListener(cw);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Change Color");
        menuItem.addActionListener(cw);
        menu.add(menuItem);
        
        menu.addSeparator();
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem radioItem = new JRadioButtonMenuItem("Blend Mode");
        radioItem.addActionListener(cw);
        menu.add(radioItem);
        group.add(radioItem);
        
        radioItem = new JRadioButtonMenuItem("Modulate Mode");
        radioItem.addActionListener(cw);
        radioItem.setSelected(true);
        menu.add(radioItem);
        group.add(radioItem);
        
        radioItem = new JRadioButtonMenuItem("Decal Mode");
        radioItem.addActionListener(cw);
        menu.add(radioItem);
        group.add(radioItem);
        
        add(menu);
    }
    
    /** 
     * Adds commands for controlling the camera to this MenuBar.
     */
    public void addCameraCommands(){
        menu = new JMenu("View");
        ButtonGroup group = new ButtonGroup();
        fly = new JRadioButtonMenuItem("Fly");
        fly.addActionListener(cw);
        menu.add(fly);
        group.add(fly);
        
        orbit = new JRadioButtonMenuItem("Orbit");
        orbit.addActionListener(cw);
        orbit.setSelected(true);
        menu.add(orbit);
        group.add(orbit);
        
        menu.addSeparator();
        group = new ButtonGroup();
        JRadioButtonMenuItem radioItem = new JRadioButtonMenuItem("Normal");
        radioItem.addActionListener(cw);
        radioItem.setSelected(true);
        group.add(radioItem);
        menu.add(radioItem);
        
        radioItem = new JRadioButtonMenuItem("Stereo");
        radioItem.addActionListener(cw);
        group.add(radioItem);
        menu.add(radioItem);
        
        add(menu);
    }
    
    /**
     * Adds the help menu to this MenuBar.
     */
    public void addHelp(){
        menuItem = new JMenuItem("Help");
        menuItem.addActionListener(cw);
        add(menuItem);
    }
    /** Creates a new instance of MenuBar */
    public MenuBar(ControlWindow controlWindow) {
        cw = controlWindow;
    }
    
    public JRadioButtonMenuItem getFly(){
        return fly;
    }
    
    public JRadioButtonMenuItem getOrbit(){
        return orbit;
    }
    
    public JMenu getRemoveMenu(){
        return removeMenu;
    }
    
    public void addToRemoveMenu(JMenuItem item){
        removeMenu.add(item);
    }
    
    public void removeFromRemoveMenu(JMenuItem item){
        removeMenu.remove(item);
    }
}
