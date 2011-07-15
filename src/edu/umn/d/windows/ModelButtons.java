/*
 * ModelChangeButtons.java
 *
 * Created on February 19, 2006, 10:13 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package edu.umn.d.windows;

import java.awt.GridLayout;
import java.util.Enumeration;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Nigel
 */
public class ModelButtons implements ChangeListener{
    ControlWindow cw;
    JSlider exagerationSlider;
    JPanel texturePanel, modelButtonPanel;
    ButtonGroup modelGroup;
    JLabel exagerationText;
    
    /** Creates a new instance of ModelChangeButtons */
    public ModelButtons(ControlWindow controlWindow) {
        cw = controlWindow;
        texturePanel = new JPanel();
        texturePanel.setName("texture panel");
        texturePanel.setLayout(new BoxLayout(texturePanel, BoxLayout.Y_AXIS));
        JLabel textureTitle = new JLabel("Images\n");
        texturePanel.add(textureTitle);
        
        exagerationSlider = new JSlider(1, 30, 3);
        exagerationSlider.setPaintTicks(true);
        exagerationSlider.setMajorTickSpacing(5);
        exagerationSlider.setMinorTickSpacing(1);
        exagerationSlider.addChangeListener(this);
        exagerationSlider.setSize(cw.BUTTON_SIZE);
        exagerationText = new JLabel("Exageration: " + String.valueOf(exagerationSlider.getValue()));
        
        modelGroup = new ButtonGroup();
        modelButtonPanel = new JPanel();
        modelButtonPanel.setLayout(new BoxLayout(modelButtonPanel, BoxLayout.Y_AXIS));
        modelButtonPanel.add(new JLabel("Current Models"));
        
    }
    
    public int getExageration(){
        return exagerationSlider.getValue();
    }
    
    public JPanel getExagerationSlider(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(exagerationText);
        panel.add(exagerationSlider);
        panel.setSize(180, 30);
        return panel;
    }
    
    public JPanel getTexturePanel(){
        return texturePanel;
    }
    
    public JPanel getModelButtonPanel(){
        return modelButtonPanel;
    }
    
    public void addTextureButton(JRadioButton button){
        texturePanel.add(button);
    }
    
    public void clearTexturePanel(){
        texturePanel.removeAll();
        texturePanel.add(new JLabel("Images"));
    }
    
    public void addModel(String name){
        modelGroup.setSelected(modelGroup.getSelection(), false);
        JRadioButton model = new JRadioButton(name);
        modelGroup.add(model);
        model.setActionCommand(name);
        model.addActionListener(cw);
        model.setSelected(true);
        modelButtonPanel.add(model);
    }
    
    public void removeModel(String name){
        Enumeration e = modelGroup.getElements();
        while(e.hasMoreElements()){
            JRadioButton a = (JRadioButton)e.nextElement();
            if(a.getText().equals(name)){
                modelGroup.remove(a);
                modelButtonPanel.remove(a);
            }
        }
    }
    
    public String getSelectedModelName(){
        return modelGroup.getSelection().getActionCommand();
    }
    
    public Enumeration getModels(){
        return modelGroup.getElements();
    }
    
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            //exageration = (int)source.getValue();
            cw.changeExageration();
            exagerationText.setText("Exageration: " + String.valueOf(source.getValue()));
        }
    }
}
