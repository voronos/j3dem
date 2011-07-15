package edu.umn.d.behaviors;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.text.*;

import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.behaviors.vp.*;
import edu.umn.d.geometry.ElevationModelInterface;
/**
 * This class is a specialization of ViewPlatformAWTBehavior.  Its purpose
 * is to provide control to the ViewPlatform using three mechanisms,
 * keyboard, mouse, and popupmenu with control dialog.
 *
 * @author  Mark Pendergast
 * @version 1.0 February 2003
 */
public class FlyingPlatform extends ViewPlatformAWTBehavior implements ItemListener,ActionListener {
    
    /** base XAxis attitude determines whether you are climbing or diving */
    private static final int HOME_XANGLE = 0;
    /** base YAxis attitude determines whether you are flying east/west/north/south */
    private static final int HOME_YANGLE = 0;
    /** base ZAxis attitude determines whether you are banking left or right */
    private static final int HOME_ZANGLE = 0;
    /** home X location */
    private   float HOME_X = -19987;
    /** home Y location */
    private   float HOME_Y = 0;
    /** home Z location */
    private   float HOME_Z = 9634;
    
    /** holds view platform location*/
    private Vector3f platformVect;
    /** holds current X axis attitude */
    private float xAngle = HOME_XANGLE; // degrees
    /** holds current Y axis attitude */
    private float yAngle = HOME_YANGLE; // degrees
    /** holds current Z axis attitude */
    private float zAngle = HOME_ZANGLE; // degrees
    
    private int oldx = -1, oldy = -1;
    private static int sensitivity = 3;
    
    /** amount to move (in meters) on each operation */
    private float moveAmt = 1;
    /** amount to turn(in degrees) on each operation */
    private float turnAmt = 10.0f; // degrees to turn
    
    private static  float  INITIAL_TERRAIN_FOLLOW_ALTITUDE = 100;
    private static  float MINIMUM_ALTITUDE = 0;
    private boolean followTerrain = false; // terrain following enabled/disabled
    private float terrainFollowAltitude; // how high to maintain platform
//
//  popup menu controls
//
    private PopupMenu popupMenu = new PopupMenu();
    private CheckboxMenuItem terrainFollowMenu = new CheckboxMenuItem("Terrain following");
    private MenuItem settingsMenu = new MenuItem("Navigation control panel ...");
    private MenuItem homeBaseMenu = new MenuItem("Return to home Base");
    private MenuItem levelOffMenu = new MenuItem("Level off");
    private MenuItem aerialViewMenu = new MenuItem("Aerial view");
    
    private ElevationModelInterface model; // call back to get terrain information
    private Canvas3D canvas;  // canvas object
    
    private SettingsDialog settingsDialog; // pop settings dialog
    /**
     *  Create the flying platform
     * @param aModel ElevationModel this platform flies over.
     * 
     * @param aCanvas Canvas3D object that is used to display the world
     */
    public FlyingPlatform(Canvas3D aCanvas, ElevationModelInterface aModel) {
        
        super(aCanvas,MOUSE_MOTION_LISTENER|MOUSE_LISTENER|KEY_LISTENER);
        
        aCanvas.requestFocus();  // get the focus to the Canvas, allows keyboard inputs
        model = aModel;
        canvas = aCanvas;
        HOME_Y = model.getElevationAt(0,0)+INITIAL_TERRAIN_FOLLOW_ALTITUDE;
        moveAmt = Math.round(model.getModelLength()/100);
        moveAmt = Math.max(moveAmt%10, moveAmt-(moveAmt%10));
        platformVect = new Vector3f(HOME_X,HOME_Y,HOME_Z);
        
        Container c = canvas.getParent();
        while(c.getParent() != null)
            c= c.getParent();
        //settingsDialog = new SettingsDialog((Frame)c);
        
        popupMenu.add(settingsMenu);
        popupMenu.add(levelOffMenu);
        popupMenu.add(terrainFollowMenu);
        popupMenu.addSeparator();
        popupMenu.add(aerialViewMenu);
        popupMenu.add(homeBaseMenu);
        canvas.add(popupMenu);
        
        terrainFollowMenu.addItemListener(this);
        settingsMenu.addActionListener(this);
        homeBaseMenu.addActionListener(this);
        levelOffMenu.addActionListener(this);
        aerialViewMenu.addActionListener(this);
    }
    /**
     *  reset the viewplatform transformation based on
     * the x,y,z rotation and location information.
     *
     */
    
    protected void integrateTransforms() {
        
        
        Transform3D tVect = new Transform3D();
        Transform3D tXRot = new Transform3D();
        Transform3D tYRot = new Transform3D();
        Transform3D tZRot = new Transform3D();
        
        tVect.set(platformVect);
        tXRot.set(new AxisAngle4d(1.0,0.0,0.0,Math.toRadians(xAngle)));
        tYRot.set(new AxisAngle4d(0.0,1.0,0.0,Math.toRadians(yAngle)));
        tZRot.set(new AxisAngle4d(0.0,0.0,1.0,Math.toRadians(zAngle)));
        tVect.mul(tYRot);
        tVect.mul(tXRot);
        tVect.mul(tZRot);
        
        targetTransform = tVect;
        vp.getViewPlatformTransform().setTransform(tVect);
        
    }
    
    /**
     *  process popup menu input
     *  @param e ActionEvent object
     */
    public void actionPerformed(ActionEvent e) {
        
        if(e.getSource() == homeBaseMenu)
            goHome();
        
        if(e.getSource() == levelOffMenu)
            levelOff();
        
        if(e.getSource() == settingsMenu)
            settingsDialog.setVisible(true);
        
        if(e.getSource() == aerialViewMenu)
            aerialView();
    }
    /**
     *  Cause the viewplatform to return to level flight, x, z angles all set to 0
     *
     */
    public void levelOff() {
        
        xAngle = 0;
        zAngle = 0;
        integrateTransforms();
    }
    /**
     *  Moves the viewplatform to the default home position and
     *  turns off terrain following.
     */
    public void goHome() {
        
        xAngle = HOME_XANGLE;
        yAngle = HOME_YANGLE;
        zAngle = HOME_ZANGLE;
        platformVect.x = HOME_X;
        platformVect.y = HOME_Y;
        platformVect.z = HOME_Z;
        terrainFollowMenu.setState(false);
        followTerrain = false;
        integrateTransforms();
        
    }
    /**
     *  Moves the viewplatform to give an aerial view of the terrain and
     *  turns off terrain following.
     */
    public void aerialView() {
        xAngle = -90;
        zAngle = 0;
        yAngle = 0;
        platformVect.x = 0;
        platformVect.z = 0;
        platformVect.y =(float)( model.getMaxElevation()+(model.getModelLength())/ Math.tan(getView().getFieldOfView()/2.0));
        terrainFollowMenu.setState(false);
        followTerrain = false;
        integrateTransforms();
        
    }
    
    /**
     * processes the terrain following checkmenu item. Turns on/off
     * terrain following. When terrain following is turned on, the platform
     * is set to level flight and altitude set to a default elevation above the
     * scene
     * @param e The ItemEvent to process.
     */
    public void itemStateChanged(ItemEvent e) {
        if(terrainFollowMenu.getState()) {
            xAngle = 0;
            zAngle = 0;
            platformVect.y = model.getElevationAt(platformVect.x, platformVect.z) + INITIAL_TERRAIN_FOLLOW_ALTITUDE;
            terrainFollowAltitude = INITIAL_TERRAIN_FOLLOW_ALTITUDE;
            integrateTransforms();
            followTerrain = true;
        } else {
            followTerrain = false;
        }
        
    }
    /**
     *  updates the amount of space (in meters) that the platform is advanced
     *  with each mouse move/arrow key event
     *  @param amt number of meters to move with each operation
     *
     */
    public void setStepSize(float amt) {
        moveAmt = amt;
    }
    /**
     * returns the setsize
     * @return moveAmt in meters
     */
    public float getStepSize() {
        return moveAmt;
    }
    /**
     *  updates the amount of rotation (in degrees) that the platform is rotated
     *  with each mouse move/arrow key event
     *  @param amt number of degrees to rotate with each operation
     *
     */
    
    public void setRotateAmt(float amt) {
        turnAmt = amt;
    }
    /**
     * returns the rotation amount
     * @return turnAmt
     */
    public float getRotateAmount() {
        return turnAmt;
    }
    
    /**
     * If processStimulus gets an AWT event, it is passed here for processing.
     * @param events Array of events that processStimulus received.
     */
    protected void processAWTEvents(java.awt.AWTEvent[] events) {
        // required as part of parent class, but all events are procesed elsewhere
    }
    
    /**
     * process keyboard input
     *
     *   up arrow - move forward
     *   down arrow - move backward
     *   left arrow - turn left
     *   right arrow - turn right
     *   U, u - increase altitude
     *   D, d - decrease altitude
     * @param e  keyboard event
     */
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_UP: // move forward
                moveForward(moveAmt);
                break;
            case KeyEvent.VK_DOWN: // move backward
                moveForward(-moveAmt);
                break;
            case KeyEvent.VK_LEFT: // turn left
                increaseYRotate(turnAmt);
                break;
            case KeyEvent.VK_RIGHT: // turn right
                increaseYRotate(-turnAmt);
                break;
            case KeyEvent.VK_U:        // increase altitude
                increaseY(moveAmt);
                break;
            case KeyEvent.VK_D:        // decrease altitude
                increaseY(-moveAmt);
                break;
            case KeyEvent.VK_HOME:
                goHome();
                break;
            case KeyEvent.VK_END:
                aerialView();
                break;
                
        }
    }
    /**
     * process mouse clicked event, check if it is the right button, if so, bring
     * up the popup menu.
     * @param e mouse event
     */
    public void mouseClicked(MouseEvent e) {
        int mods = e.getModifiersEx();
        boolean alt = (mods & MouseEvent.ALT_DOWN_MASK) != 0;
        boolean ctrl = (mods & MouseEvent.CTRL_DOWN_MASK) != 0;
        //
        // on a right click, pop a control menu
        //
        if(e.getButton() == MouseEvent.BUTTON3)
            popupMenu.show(canvas,e.getX(),e.getY());
        
    }
    /**
     * process mouse moved event, just reset old mouse locations
     *
     * @param e mouse event
     */
    public void mouseMoved(MouseEvent e) {
        oldx = -1;
        oldy = -1;
    }
    /**
     * process mouseDragged event.  determine which buttons are down and
     * move the view platform accordingly.
     *  left button down, mouse moved up - go forward
     * left button down, mouse moved down - go backward
     * left button down, mouse moved right - turn right
     * left button down, mouse moved left - turn left
     * right button down, mouse moved up - increase altitude
     * right button down, mouse moved down - decrease altitude
     * right button down, mouse moved left - bank left
     * right button down, mouse moved right - bank right
     * both buttons down, move up - climb
     * both buttons down, move down - dive
     * @param e The event to process.
     */
    public void mouseDragged(MouseEvent e) {
        int mods = e.getModifiersEx();
        
        int x = e.getX();
        int y = e.getY();
        
        if(oldx < 0 || oldy < 0) {
            oldx = x;
            oldy = y;
            return;
        }
//
// skip the event if it moved just a little
//
        if(Math.abs(y-oldy) < sensitivity &&
                Math.abs(x-oldx) < sensitivity)
            return;
//
// first check to see if both left and right buttons are down.
//
        if((mods & MouseEvent.BUTTON1_DOWN_MASK) != 0
                && (mods & MouseEvent.BUTTON3_DOWN_MASK) != 0) {
            if(y > oldy+sensitivity)
                increaseXRotate(1);
            
            
            if(y < oldy-sensitivity)
                increaseXRotate(-1);
            
            return;
        }
//
// process left only down
//
        if((mods & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
            if(y > oldy+sensitivity) //mouse moves down screen
                moveForward(-moveAmt);
            
            if(y < oldy-sensitivity) // mouse moves up the screen
                moveForward(moveAmt);
            
            if(x > oldx+sensitivity)
                increaseYRotate(-turnAmt);
            
            if(x < oldx-sensitivity)
                increaseYRotate(turnAmt);
        }
//
// process middle down only
//
        if((mods & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
            if(y > oldy+sensitivity) //mouse moves down screen
                increaseXRotate(-2);
            
            if(y < oldy-sensitivity) // mouse moves up the screen
                increaseXRotate(2);
            
            if(x > oldx+sensitivity)
                increaseYRotate(-turnAmt);
            
            if(x < oldx-sensitivity)
                increaseYRotate(turnAmt);
        }
//
// process right button down
//
        if((mods & MouseEvent.BUTTON3_DOWN_MASK) != 0) {
            
            if(y > oldy+sensitivity) // mouse moves down the screen
                increaseY(-moveAmt);
            
            if(y < oldy-sensitivity) // mouse moves up the screen
                increaseY(moveAmt);
            
            if(x > oldx+sensitivity)
                strafe(moveAmt);//increaseZRotate(turnAmt);
            
            if(x < oldx-sensitivity)
                strafe(-moveAmt);//increaseZRotate(-turnAmt);
        }
        
        oldx = x;  // save for comparison on next mouse move
        oldy = y;
    }
    
    /**
     * move the viewplatform forward by desired number of meters
     * forward implies in the direction that it is currently pointed.
     * if terrain following is enabled, then keep the altitude a steady
     * amount above the ground.
     * @param amt number of meters to move forward
     */
    public void moveForward(float amt) {
        //
        //  Calculate x,y,z movement
        //
        // Setup Transforms
        Transform3D tTemp = new Transform3D();
        Transform3D tXRot = new Transform3D();
        Transform3D tYRot = new Transform3D();
        Transform3D tZRot = new Transform3D();
        
        tXRot.set(new AxisAngle4d(1.0,0.0,0.0,Math.toRadians(xAngle)));
        tYRot.set(new AxisAngle4d(0.0,1.0,0.0,Math.toRadians(yAngle)));
        tZRot.set(new AxisAngle4d(0.0,0.0,1.0,Math.toRadians(zAngle)));
        tTemp.mul(tYRot);
        tTemp.mul(tXRot);
        tTemp.mul(tZRot);
        // move forward in z direction
        // this implies decreasing z since we are looking at the origin from the pos z
        Vector3f tv = new Vector3f(0,0,-amt);
        tTemp.transform(tv);  // translates z movement into x,y,z movement based on heading
        
        //
        //  set new values for the platform location vector
        // if terrain following is on, then find the terrain elevation at the new x,z
        // coordinate and base the new altitude on that. Else use the computed altitude.
        //
        if(followTerrain) {
            platformVect.y = model.getElevationAt(platformVect.x+tv.x, platformVect.z+tv.z)+terrainFollowAltitude;
        } else
            platformVect.y += tv.y;
        
        platformVect.x += tv.x;
        platformVect.z += tv.z;
        
        integrateTransforms();  // apply transformations
    }
    /**
     *  Increase the Y axis rotation. This effects the heading of the platform
     *  value is clamped to 0-359.
     *  @param amt number of degrees to change the heading
     *
     */
    public void increaseYRotate(float amt) {
        yAngle += amt;
        if(yAngle >= 360)
            yAngle -= 360;
        if(yAngle < 0)
            yAngle += 360;
        
        integrateTransforms();
    }
    
    /**
     *  Increase the X axis rotation. This effects the pitch (nose up/down) of the platform
     *  value is clamped to -360 to 360.
     *  @param amt number of degrees to change the pitch
     *
     */
    public void increaseXRotate(float amt) {
        xAngle += amt;
        if(xAngle >= 360)
            xAngle -= 360;
        if(xAngle <= -360)
            xAngle += 360;
        
        integrateTransforms();
    }
    /**
     *  Increase the Z axis rotation. This effects the bank/roll of the platform
     *  value is clamped to -360-360.
     *  @param amt number of degrees to change the bank
     *
     */
    public void increaseZRotate(float amt) {
        zAngle += amt;
        if(zAngle >= 360)
            zAngle -= 360;
        if(zAngle <= -360)
            zAngle += 360;
        
        integrateTransforms();
    }
    
    /**
     *  Increase the Y location. This effects the altitude of the platform.
     *
     *  @param amt number of degrees to change the altitude
     *
     */
    public void increaseY(float amt) {
        platformVect.y += amt;
        integrateTransforms();
        if(followTerrain) {
            terrainFollowAltitude += amt;
            terrainFollowAltitude = Math.max(MINIMUM_ALTITUDE,terrainFollowAltitude);
        }
    }
    private void strafe(float amount){
        //
        //  Calculate x,y,z movement
        //
        // Setup Transforms
        Transform3D tTemp = new Transform3D();
        Transform3D tXRot = new Transform3D();
        Transform3D tYRot = new Transform3D();
        Transform3D tZRot = new Transform3D();
        
        tXRot.set(new AxisAngle4d(1.0,0.0,0.0,Math.toRadians(xAngle)));
        tYRot.set(new AxisAngle4d(0.0,1.0,0.0,Math.toRadians(yAngle)));
        tZRot.set(new AxisAngle4d(0.0,0.0,1.0,Math.toRadians(zAngle)));
        tTemp.mul(tYRot);
        tTemp.mul(tXRot);
        tTemp.mul(tZRot);
        
        Vector3f tv = new Vector3f(amount,0,0);
        tTemp.transform(tv);  // translates z movement into x,y,z movement based on heading
        
        //
        //  set new values for the platform location vector
        // if terrain following is on, then find the terrain elevation at the new x,z
        // coordinate and base the new altitude on that. Else use the computed altitude.
        //
        if(followTerrain) {
            platformVect.y = model.getElevationAt(platformVect.x+tv.x, platformVect.z+tv.z)+terrainFollowAltitude;
        } else
            platformVect.y += tv.y;
        
        platformVect.x += tv.x;
        platformVect.z += tv.z;
        
        integrateTransforms();  // apply transformations
    }
    
    /**
     *
     *  Inner class, popup dialog used to allow settings to be controlled and navigation
     * to be done with buttons
     *
     */
    class SettingsDialog extends JDialog implements ActionListener, FocusListener{
        private JButton upB = new JButton("+");
        private JButton downB = new JButton("-");
        private JButton leftB = new JButton("<");
        private JButton rightB = new JButton(">");
        private JButton forwardB = new JButton(">>");
        private JButton backwardB = new JButton("<<");
        private JButton turnLeftB = new JButton("<");
        private JButton turnRightB = new JButton(">");
        private JButton turnUpB = new JButton("+");
        private JButton turnDownB = new JButton("-");
        private JButton levelOffB = new JButton("Level off ");
        private JButton homeBaseB = new JButton("Go home");
        private JButton aerialViewB = new JButton("Aerial view");
        private Font bf = new Font("Dialog",Font.BOLD,9);
        private JPanel p1 = new JPanel(new GridLayout(2,1));
        private JPanel p11 = new JPanel(new BorderLayout());
        private JPanel p12 = new JPanel(new GridLayout(1,3));
        private JPanel p2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        private JPanel pControls = new JPanel(new GridLayout(3,1));
        private JPanel pPitch = new JPanel(new FlowLayout(FlowLayout.CENTER));
        private JPanel pRoll = new JPanel(new FlowLayout(FlowLayout.CENTER));
        private JPanel pAlt = new JPanel(new FlowLayout(FlowLayout.CENTER));
        private JPanel pHeading= new JPanel(new FlowLayout(FlowLayout.CENTER));
        private JPanel pLocation= new JPanel(new FlowLayout(FlowLayout.CENTER));
        private JLabel stepLabel = new JLabel("Step size (meters)");
        private JLabel rotateLabel = new JLabel("Rotate amount (degrees)");
        private JTextField stepField = new JTextField("0",6);
        private JTextField rotateField = new JTextField("0",4);
        private JLabel altField = new JLabel("0");
        private JLabel rollField = new JLabel("0");
        private JLabel pitchField = new JLabel("0");
        private JLabel headingField = new JLabel("0");
        private JLabel locationField = new JLabel("0, 0");
        
        private DecimalFormat df = new DecimalFormat("#####0");
        
        /**
         * initize the Settings dialog.
         */
        SettingsDialog(Frame parent) {
            
            
            super(parent,false);  // call the super class constructor, set title and modal
            
            setSize(350,160);                    // set the size
            setLocation(100,100);
            setTitle("Navigation Control Panel");
            //
            // add panels to the dialog
            //
            getContentPane().add("Center",p1);
            getContentPane().add("South",p2);
            
            p1.add(p11);
            p1.add(p12);
            //
            // add the buttons to the panels
            //
            pPitch.add(turnDownB);
            pPitch.add(pitchField);
            pPitch.add(turnUpB);
            pPitch.setBorder(BorderFactory.createTitledBorder("Pitch"));
            ((TitledBorder)pPitch.getBorder()).setTitleFont(bf);
            
            pAlt.add(downB);
            pAlt.add(altField);
            pAlt.add(upB);
            pAlt.setBorder(BorderFactory.createTitledBorder("Altitude"));
            ((TitledBorder)pAlt.getBorder()).setTitleFont(bf);
            
            pHeading.add(turnLeftB);
            pHeading.add(headingField);
            pHeading.add(turnRightB);
            pHeading.setBorder(BorderFactory.createTitledBorder("Heading"));
            ((TitledBorder)pHeading.getBorder()).setTitleFont(bf);
            
            pRoll.add(leftB);
            pRoll.add(rollField);
            pRoll.add(rightB);
            pRoll.setBorder(BorderFactory.createTitledBorder("Roll"));
            ((TitledBorder)pRoll.getBorder()).setTitleFont(bf);
            
            pLocation.add(backwardB);
            pLocation.add(locationField);
            pLocation.add(forwardB);
            pLocation.setBorder(BorderFactory.createTitledBorder("Location"));
            ((TitledBorder)pLocation.getBorder()).setTitleFont(bf);
            
            pControls.add(homeBaseB);
            pControls.add(levelOffB);
            pControls.add(aerialViewB);
            
            p11.add("West",pAlt);
            p11.add("East",pHeading);
            p11.add("Center",pLocation);
            
            p12.add(pRoll);
            p12.add(pPitch);
            p12.add(pControls);
            
            p2.add(stepLabel);
            p2.add(stepField);
            p2.add(rotateLabel);
            p2.add(rotateField);
            
            //
            // add focus listeners
            //
            rotateField.addFocusListener(this);  // check for focus lost to record changes
            stepField.addFocusListener(this);
            //
            // setup the buttons fonts, insets, and actionlisteners
            //
            setupButton(upB);
            setupButton(downB);
            setupButton(leftB);
            setupButton(rightB);
            setupButton(forwardB);
            setupButton(backwardB);
            setupButton(turnLeftB);
            setupButton(turnRightB);
            setupButton(turnUpB);
            setupButton(turnDownB);
            setupButton(homeBaseB);
            setupButton(aerialViewB);
            setupButton(levelOffB);
            // set some fonts and colors
            stepLabel.setFont(bf);
            rotateLabel.setFont(bf);
            
            setupLabel(rollField);
            setupLabel(pitchField);
            setupLabel(altField);
            setupLabel(locationField);
            setupLabel(headingField);
            
            updateTextFields();
            
        }
        /**
         *  Initialize font, spacing for buttons
         *  @param b reference to a JButton object
         */
        private void setupButton(JButton b) {
            Insets in = new Insets(2,2,2,2);
            b.addActionListener(this);
            b.setFont(bf);
            b.setMargin(in);
        }
        /**
         *  Initialize color, font, opaquenss for labels
         * @param l reference to a JLabel object
         */
        private void setupLabel(JLabel l) {
            l.setBackground(Color.black);
            l.setForeground(Color.green);
            l.setOpaque(true);
            l.setFont(bf);
        }
        /**
         *
         *  Initialize/update text fields
         */
        private void updateTextFields() {
            stepField.setText("  "+df.format(moveAmt)+"  ");
            rotateField.setText("  "+df.format(turnAmt)+"  ");
            rollField.setText("  "+df.format(zAngle)+"  ");
            pitchField.setText("  "+df.format(xAngle)+"  ");
            if(yAngle != 0)  // since right hand rule is in effect, then pos y rotation goes left
                headingField.setText("  "+df.format(360-yAngle)+"  "); // convert to compass direction
            else
                headingField.setText("  0  "); // convert to compass direction, use 0 instead of 360 for North
            
            locationField.setText(" "+df.format(platformVect.x)+","+df.format(platformVect.z)+" ");
            altField.setText(" "+df.format(platformVect.y)+" ");
            
        }
        /**
         * retrieve step size, movement amount, if there is an error, set it to 1.0
         *
         *  @return value from stepsize field
         */
        public float getStepSize() {
            try{
                float d = Float.parseFloat(stepField.getText());
                return d;
            } catch(NumberFormatException e) {
                stepField.setText("1");
                return 1.0f;
            }
            
        }
        /**
         * retrieve step size, movement amount, if there is an error, set it to 5.0
         *
         * @return value from rotate field
         */
        public float getRotateAngle() {
            try{
                float d = Float.parseFloat(rotateField.getText());
                if(d < 0)
                    d = 0;
                if(d > 359)
                    d= 359;
                rotateField.setText(""+d);
                return d;
            } catch(NumberFormatException e) {
                rotateField.setText("5");
                return 5.0f;
            }
            
        }
        
        /**
         * process button press events, moving the platform accordingly
         *
         * @param e ActionEvent generated by button presses
         */
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == forwardB)
                moveForward(getStepSize());
            
            if(e.getSource() == backwardB)
                moveForward(-getStepSize());
            
            if(e.getSource() == leftB)
                increaseZRotate(-getRotateAngle());
            
            if(e.getSource() == rightB)
                increaseZRotate(getRotateAngle());
            
            if(e.getSource() == upB)
                increaseY(getStepSize());
            
            if(e.getSource() == downB)
                increaseY(-getStepSize());
            
            if(e.getSource() == turnUpB)
                increaseXRotate(getRotateAngle());
            
            if(e.getSource() == turnDownB)
                increaseXRotate(-getRotateAngle());
            
            if(e.getSource() == turnRightB)
                increaseYRotate(-getRotateAngle());
            
            if(e.getSource() == turnLeftB)
                increaseYRotate(getRotateAngle());
            
            if(e.getSource() == levelOffB)
                levelOff();
            
            if(e.getSource() == homeBaseB)
                goHome();
            
            if(e.getSource() == aerialViewB)
                aerialView();
            
            updateTextFields();
            
        }
        
        public void focusGained(FocusEvent e) {
            
        }
        
        /**
         * process focus event, whenever the stepfield/rotatefield loses focus, retrieve the
         * value that the user entered.
         * @param e focus event.
         */
        public void focusLost(FocusEvent e) {
            if(e.getSource() == stepField) {
                moveAmt = getStepSize();
            }
            if(e.getSource() == rotateField) {
                turnAmt= getRotateAngle();
            }
        }
        
        /**
         * Override setvisible to insure fields have latest values
         * @param b shows/hides dialog
         */
        public void setVisible(boolean b) {
            if(b)
                updateTextFields();
            super.setVisible(b);
        }
    } // end inner class
}
