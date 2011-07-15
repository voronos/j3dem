package edu.umn.d.windows;
import com.sun.j3d.utils.image.TextureLoader;
import edu.umn.d.geometry.ElevationModel;
import edu.umn.d.windows.StereoFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.swing.*;


/**
 * Window that contains all of the controls for the program.  I chose to use 2 windows rather than one
 * because the drop down menus were hidden behind the Canvas3D.
 * @author nels2426
 */
public class ControlWindow extends JFrame implements ActionListener{
    public View3DPanel viewWindow;
    public JPanel settingsPanel;
    public LightPanel lightPanel;
    public StereoFrame sf;
    public StatusWindow stat;
    public static final Dimension BUTTON_SIZE = new Dimension(200, 25);
    private static RecordButtons rb;
    private static CameraButtons cb;
    private static MenuBar menuBar;
    private static ModelButtons mcb;
    /**
     * Creates a new instance of ControlWindow
     */
    public ControlWindow() {
        stat = new StatusWindow();
        initialize();
    }
    
    public ControlWindow(StereoFrame sf){
        this.sf = sf;
        stat = new StatusWindow();
        stat.setLocation(StereoFrame.SCREEN_WIDTH / 3, StereoFrame.SCREEN_HEIGHT / 3);
        initialize();
        setName("control window");
    }
    
    private void initialize(){
        settingsPanel = new JPanel();
        ((FlowLayout)settingsPanel.getLayout()).setAlignment(FlowLayout.LEFT);
        //BoxLayout box = new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS);
        //settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setTitle("jdemview");
        setSize(200,650);
        addButtons();
        
        //settingsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        add(settingsPanel);
        createMenuBar();
        setVisible(true);
    }
    
    /**
     * The View3DPanel and ControlWindow classes are rather tightly coupled at the moment.
     * This is needed as a reference back to the View3DPanel.
     * @param vp The View3DPanel that this ControlWindow affects.
     */
    public void setViewWindow(View3DPanel vp){
        viewWindow = vp;
        if(lightPanel != null){
            lightPanel.setViewWindow(vp);
        }
    }
    
    /**
     * Accessor that returns the name of the ElevationModel currently selected in the radio buttons at the bottom of the ControlWindow.
     * @return The name of the ElevationModel.
     */
    public String getSelectedModel(){
        return mcb.getSelectedModelName();
    }
    
    /**
     * Adds a new radio button to the set at the bottom of the ControlWindow.
     * This should be called whenever a new ElevationModel is added.
     * Also adds a new menu item to the remove menu.
     * @param name The name of the ElevationModel.
     */
    public void addModelButton(String name){
        mcb.addModel(name);
        refresh();
        
        JMenuItem modelItem = new JMenuItem(viewWindow.getModel().name);
        modelItem.addActionListener(this);
        menuBar.addToRemoveMenu(modelItem);
        viewWindow.setRendering(true);
    }
    
    /**
     * Removes a radio button from the ControlWindow.  This should be called whenever an ElevationModel is removed.
     * @param name The name of the ElevationModel that was removed.
     */
    public void removeModelButton(String name){
        mcb.removeModel(name);
        refresh();
    }
    
    private void addButtons(){
        //Dimension standard = new Dimension(250, 25);
        
        rb = new RecordButtons(this);
        cb = new CameraButtons(this);
        mcb = new ModelButtons(this);
        JLabel title = new JLabel("Exageration");
        settingsPanel.add(mcb.getModelButtonPanel());
        settingsPanel.add(mcb.getTexturePanel());
        
        //Component space = Box.createRigidArea(new Dimension(0, 10));
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        //JLabel exag = new JLabel("Exageration");
        //exag.setPreferredSize(new Dimension(200, 10));
        //settingsPanel.add(exag);
        settingsPanel.add(mcb.getExagerationSlider());
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        settingsPanel.add(rb.getScreenshotButton());
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        settingsPanel.add(rb.getStartButton());
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        settingsPanel.add(rb.getStopButton());
        settingsPanel.add(Box.createRigidArea(new Dimension(0,20)));
        JLabel camera = new JLabel("Camera Behavior");
        camera.setPreferredSize(new Dimension(200, 10));
        settingsPanel.add(camera);
        settingsPanel.add(cb.getFly());
        settingsPanel.add(cb.getOrbit());
        
        settingsPanel.add(Box.createRigidArea(new Dimension(0,20)));
        lightPanel = new LightPanel();
        settingsPanel.add(lightPanel);
        settingsPanel.setName("settings panel");
        
        
    }
    
    private void createMenuBar(){
        menuBar = new MenuBar(this);
        menuBar.addMainCommands();
        menuBar.addTextureCommands();
        menuBar.addCameraCommands();
        menuBar.addHelp();
        setJMenuBar(menuBar);
        
    }
    
    /**
     * Handles all of the actions from the buttons and menu items on the ControlWindow.
     * @param e The triggering event.
     */
    public void actionPerformed(ActionEvent e){
        if(e.getSource() instanceof JRadioButton){
            radioButtonAction((JRadioButton)e.getSource());
        }
        if(e.getSource() instanceof JButton){
            buttonAction((JButton)e.getSource());
            
        } else if(e.getSource() instanceof JMenuItem){
            menuAction((JMenuItem)e.getSource());
        }
    }
    
    private void applyImage(){
        FileDialog dialog = new FileDialog(this, "Open Image file", FileDialog.LOAD);
        dialog.setDirectory(".");
        dialog.setVisible(true);
        if(dialog.getFile() != null){
            String fileName = dialog.getDirectory()+dialog.getFile();
            try{
                TextureLoader texget = new TextureLoader(new java.net.URL("file:///" + fileName),null);
                Texture2D tex = (Texture2D) texget.getTexture();
                viewWindow.setTexture(tex);
            } catch(java.net.MalformedURLException exception){
                System.err.println("error loading textures");
                exception.printStackTrace();
            }
        }
    }
    
    public View3DPanel getViewWindow(){
        return viewWindow;
    }
    
    public StatusWindow getStatusWindow(){
        return stat;
    }
    
    public void addTextureButtons(JRadioButton[] buttons){
        if(buttons != null){
            removeTextureButtons();
            for(int i = 0; i < buttons.length; i++){
                mcb.addTextureButton(buttons[i]);
            }
            refresh();
        }
    }
    
    private void addDEM(){
        FileDialog dialog = new FileDialog(this, "Open Dem file", FileDialog.LOAD);
        dialog.setDirectory(".");
        dialog.setVisible(true);
        if(dialog.getFile() != null){ // did the user select a file
            StatusWindow stat = new StatusWindow();
            stat.setVisible(true);
            //stat.setLocation(StereoFrame.WIDTH / 3, StereoFrame.HEIGHT / 3);
            String fileName = dialog.getDirectory()+dialog.getFile(); // create a full file name
            try{
                stat.setLabel1(fileName);
                URL u = new URL("file:///" + fileName.replace("\\", "/"));
                viewWindow.addModel(u,stat);
            } catch(IllegalArgumentException exception) {
                JOptionPane.showMessageDialog(this,exception.getMessage(),
                        "Dem file load error",JOptionPane.ERROR_MESSAGE);  // popup an error message
            } catch (MalformedURLException e){
                e.printStackTrace();
                System.exit(0);
            }
            stat.dispose();
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    private void removeTextureButtons(){
        mcb.clearTexturePanel();
        refresh();
    }
    
    private void refresh(){
        Dimension d = getSize();
        setSize(d.width+1,d.height+1);
        setSize(d);
        validate();
        repaint();
    }
    
    private void radioButtonAction(JRadioButton source){
        
        String action = source.getActionCommand();
        String s = source.getText();
        if(s.equals("Fly")){
            viewWindow.setFlyBehavior();
            if(menuBar.getFly() != null){
                menuBar.getFly().setSelected(true);
            }
        } else if(s.equals("Orbit")){
            viewWindow.setOrbitBehavior();
            if(menuBar.getOrbit() != null){
                menuBar.getOrbit().setSelected(true);
            }
        }else {
            ElevationModel m = viewWindow.getModel(s);
            addTextureButtons(m.getTextureButtons());
        }
    }
    
    private void buttonAction(JButton source){
        String s = source.getText();
        if(s.equals("Start recording")){
            viewWindow.startRecording();
        }else if(s.equals("Stop recording")){
            viewWindow.stopRecording();
        }else if(s.equals("Take screenshot")){
            viewWindow.takeScreenShot();
        //}else if(s.equals("Change Exageration")){
        //    changeExageration();
        } else if(s.equals("Remove Image")){
            viewWindow.removeTexture();
        //} else if(s.equals("Apply Image")){
        //    applyImage();
        }
    }
    
    private void menuAction(JMenuItem source){
        String s = source.getText();
        System.out.println(s);
        if(s.equals("Change Color")){
            JColorChooser colorChooser = new JColorChooser();
            Color color = JColorChooser.showDialog(this, "Color chooser", Color.white);
            viewWindow.setModelColor(color);
        }else if(s.equals("Apply Image")){
            applyImage();
        }else if(s.equals("Remove Image")){
            viewWindow.removeTexture();
        }else if(s.equals("Add DEM")){
            addDEM();
        } else if(s.equals("Fly")){
            viewWindow.setFlyBehavior();
            cb.getFly().setSelected(true);
            //flyButton.setSelected(true);
        } else if(s.equals("Orbit")){
            viewWindow.setOrbitBehavior();
            cb.getOrbit().setSelected(true);
        } else if(s.equals("Blend Mode")){
            viewWindow.setModelTextureMode(TextureAttributes.BLEND);
        } else if(s.equals("Decal Mode")){
            viewWindow.setModelTextureMode(TextureAttributes.DECAL);
        } else if(s.equals("Modulate Mode")){
            viewWindow.setModelTextureMode(TextureAttributes.MODULATE);
        } else if(s.equals("Stereo")){
            sf.showRightPanel();
        } else if(s.equals("Normal")){
            sf.hideRightPanel();
        } else if(s.equals("Quit")){
            System.exit(0);
        } else if(s.equals("Help")){
            JOptionPane.showMessageDialog(this, "Left turn - Left click on the left side of the image.\n" +
                    "Right turn - Left click on the right of the image.\n" +
                    "Forward - Left click on the top side of the image.\n" +
                    "Backward - Left click on the bottom side of the image.\n\n" +
                    "Roll/bank left - Middle click on the left side of the image.\n" +
                    "Roll/bank right - Middle click on the right side.\n" +
                    "Dive - Middle click on top side.\n" +
                    "Climb - Middle click on bottom side.\n\n" +
                    "Increase altitude - Right click on the top side.\n" +
                    "Decrease altitude - Right click on the bottom side.\n" +
                    "Strafe Left - Right click on left side.\n" +
                    "Strafe Right - Right click on right side.\n\n");
            
        } else{
            Enumeration e = mcb.getModels();
            while(e.hasMoreElements()){
                JRadioButton b = (JRadioButton)e.nextElement();
                if(b.getActionCommand().equals(s)){
                    removeTextureButtons();
                }
            }
            viewWindow.removeModel(s);
            menuBar.removeFromRemoveMenu(source);
        }
    }
    
    public static float getExageration(){
        if(mcb != null){
            return mcb.getExageration();
        } else{
            return 3;
        }
    }
    
    public void changeExageration(){
        float f = getExageration();
        viewWindow.changeExageration(f);
    }
};

