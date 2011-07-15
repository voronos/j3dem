package edu.umn.d.behaviors;
import com.sun.j3d.utils.behaviors.vp.ViewPlatformBehavior;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.media.j3d.*;
import javax.swing.SwingUtilities;
import javax.vecmath.*;
/**
 *
 * FlyBehavior.java
 *
 * Created on July 13, 2005, 11:37 AM
 *
 * Basically, this is the same type of behavior available with Sun's java3d demo.
 * It is just condensed down into one file, rather than several inheritances.
 * Movement is as follows.
 * Left Click - move forward and backward, turn left and right
 * Middle Click - look up and down, and roll
 * Right Click - change elevation, and strafe left and right
 *
 */
public abstract class FlyBehavior extends ViewPlatformBehavior{
    /**
     * The TransformGroup this behavior acts on.
     */
    protected TransformGroup targetTG;
    /**
     * Mainly the mouse and keyboard conditions to wakeup on.
     */
    protected WakeupOr awtCondition;
    /**
     * Both awtCondition and frameElapsed conditions.
     */
    protected WakeupOr bothCondition;
    /**
     * The bounds of the canvas.  Used in calculating speed.
     */
    protected Rectangle canvasBounds;
    /**
     * Center of the canvas.  The farther the mouse is away from
     * this, the higher the speed.
     */
    protected Point2D.Float canvasCenter;
    /**
     * The Transform3D component of the targetTG.
     */
    protected Transform3D targetTransform;
    /**
     * The maximum turning angle for movement.
     */
    protected final float MAX_ANGLE = (float)Math.toRadians( 3 );
    /**
     * the maximum velocity for movement.
     */
    protected float maxVelocity = 100f;
    /**
     * Field indicating whether the behavior is in motion or not.
     */
    protected boolean motion = false;
    /**
     * Field indicating whether to ignore mouse motion or not.
     */
    protected boolean ignoreMouseMotion = false;
    /**
     * Size of the dead zone in the center of the canvas.
     */
    protected float deadXSize;
    /**
     * Size of the dead zone in the center of the canvas.
     */
    protected float deadYSize;
    /**
     * Transform3D that controls velocity.
     */
    protected Transform3D velocityTransform;
    /**
     * Transform3D that controls yaw.
     */
    protected Transform3D yawTransform;
    /**
     * Transform3D that controls roll.
     */
    protected Transform3D rollTransform;
    /**
     * Transform3D that controls pitch.
     */
    protected Transform3D pitchTransform;
    /**
     * The velocity of the behavior.
     */
    protected Vector3f velocity = new Vector3f();
    /**
     * Current angle of yaw.  Initially zero.
     */
    protected float yawAngle = 0f;
    /**
     * Current pitch angle.  Initially zero.
     */
    protected float pitchAngle = 0f;
    /**
     * Current roll angle. Initially zero.
     */
    protected float rollAngle = 0f;
    
    /**
     * Constructs a new FlyBehavior
     */
    public FlyBehavior(){
        init();
        canvasCenter = new Point2D.Float();
    }
    
    
    /**
     * Sets the TransformGroup that this behavior acts on.
     * @param targetTG New TransformGroup to use.
     */
    public void setTarget(TransformGroup targetTG){
        this.targetTG = targetTG;
    }
    
    /**
     * Returns the TransformGroup this behavior works on.
     * @return The TransformGroup this behavior works on
     */
    public TransformGroup getTarget(){
        return targetTG;
    }
    /**
     * Setup the initial wakeup conditions for this behavior.
     * These include mouse events, key events and frame elapsed events.
     * These conditions are not applied via a wakeupOn() call yet.
     */
    protected void init(){
        // For some reason having MOUSE_MOTION and MOUSE events in the same wakeup
        // results in MOUSE events being lost
        WakeupOnAWTEvent awt1 = new WakeupOnAWTEvent( AWTEvent.MOUSE_MOTION_EVENT_MASK |
                AWTEvent.COMPONENT_EVENT_MASK );
        WakeupOnAWTEvent awt2 = new WakeupOnAWTEvent( AWTEvent.MOUSE_EVENT_MASK );
        WakeupOnAWTEvent awt3 = new WakeupOnAWTEvent(  AWTEvent.KEY_EVENT_MASK );
        WakeupOnElapsedFrames frameCondition = new WakeupOnElapsedFrames( 0 );
        
        bothCondition = new WakeupOr( new WakeupCriterion[] { awt1, awt2, awt3, frameCondition } );
        awtCondition = new WakeupOr( new WakeupCriterion[] { awt1, awt2, awt3 } );
    }
    
    /**
     * Applies wake conditions via a wakeupOn() call.
     * Initializes rollTransform, pitchTransform, yawTransform, targetTransform and velocityTransform
     * to new Transform3D s
     */
    public void initialize() {
        wakeupOn( awtCondition );
        rollTransform = new Transform3D();
        pitchTransform = new Transform3D();
        yawTransform = new Transform3D();
        targetTransform = new Transform3D();
        velocityTransform = new Transform3D();
        
    }
    
    /**
     * Process a stimulus meant for this behavior. This method is invoked if the Behavior's wakeup criteria are satisfied and an active ViewPlatform's activation volume intersects with the Behavior's scheduling region. Classes that extend Behavior must provide their own processStimulus method.
     * NOTE: Applications should not call this method. It is called by the Java 3D behavior scheduler.
     * @param e an enumeration of triggered wakeup criteria for this behavior
     */
    public void processStimulus( java.util.Enumeration e ) {
        while(e.hasMoreElements()) {
            WakeupCondition wakeup = (WakeupCondition)e.nextElement();
            if (wakeup instanceof WakeupOnAWTEvent) {
                processAWTEvents( ((WakeupOnAWTEvent)wakeup).getAWTEvent() );
            } else if (wakeup instanceof WakeupOnElapsedFrames) {
                integrateTransforms();
            }
        }
        
        
        if (motion) {
            wakeupOn( bothCondition );
        } else {
            wakeupOn( awtCondition );
        }
    }
    
    /**
     * If processStimulus gets an AWT event, it is passed here for processing.
     * @param events Array of events that processStimulus received.
     */
    public void processAWTEvents( java.awt.AWTEvent[] events ) {
        
        for( int i=0; i<events.length; i++) {
            if (events[i] instanceof MouseEvent) {
                //System.out.println( events[i] );
                if ((events[i].getID()==MouseEvent.MOUSE_DRAGGED ||
                        events[i].getID()==MouseEvent.MOUSE_PRESSED ||
                        events[i].getID()==MouseEvent.MOUSE_RELEASED) &&
                        !ignoreMouseMotion ) {
                    processMouseEvent( (MouseEvent)events[i] );
                } else if (events[i].getID()==MouseEvent.MOUSE_ENTERED ) {
                    Component component = ((MouseEvent)events[i]).getComponent();
                    if (component instanceof javax.media.j3d.Canvas3D) {
                        canvasBounds = component.getBounds( canvasBounds );
                        canvasCenter.x = canvasBounds.width/2;
                        canvasCenter.y = canvasBounds.height/2;
                        deadXSize = 15f / canvasCenter.x;
                        deadYSize = 15f / canvasCenter.y;
                        if (!ignoreMouseMotion) {
                            component.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
                        }
                    }
                } else if (events[i].getID()==MouseEvent.MOUSE_EXITED ) {
                    Component component = ((MouseEvent)events[i]).getComponent();
                    if (component instanceof javax.media.j3d.Canvas3D) {
                        component.setCursor( Cursor.getDefaultCursor() );
                    }
                }
            } else if (events[i] instanceof KeyEvent) {
                processKeyEvent((KeyEvent)events[i]);
            } else if (events[i] instanceof ComponentEvent) {
                if (events[i].getID()==ComponentEvent.COMPONENT_RESIZED) {
                    canvasBounds = ((ComponentEvent)events[i]).getComponent().getBounds( canvasBounds );
                    canvasCenter.x = canvasBounds.width/2f;
                    canvasCenter.y = canvasBounds.height/2f;
                    deadXSize = 15f / canvasCenter.x;
                    deadYSize = 15f / canvasCenter.y;
                }
            }
        }
    }
    
    protected void processKeyEvent(KeyEvent evt){
        int keyCode = evt.getKeyCode();
        int id = evt.getID();
        if (keyCode == KeyEvent.VK_ESCAPE && id==KeyEvent.KEY_RELEASED) {
            ignoreMouseMotion = !ignoreMouseMotion;
            Component component = evt.getComponent();
            if (ignoreMouseMotion) {
                component.setCursor( Cursor.getDefaultCursor());
            } else {
                component.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
            }
        } else if(keyCode == KeyEvent.VK_HOME){
            aerialView();
        } else if(keyCode == KeyEvent.VK_END  && id==KeyEvent.KEY_RELEASED){
            levelOff();
        } else if(keyCode == KeyEvent.VK_UP){
            velocity.z = -maxVelocity * 5;
            integrateTransforms();
            velocity.z = 0;
        } else if(keyCode == KeyEvent.VK_DOWN){
            velocity.z = maxVelocity*5;
            integrateTransforms();
            velocity.z = 0;
        }  else if(keyCode == KeyEvent.VK_RIGHT){
            yawAngle = -MAX_ANGLE;
            integrateTransforms();
            yawAngle = 0;
        } else if(keyCode == KeyEvent.VK_LEFT){
            yawAngle = MAX_ANGLE;
            integrateTransforms();
            yawAngle = 0;
        } else if(keyCode == KeyEvent.VK_PAGE_DOWN){
            pitchAngle = -MAX_ANGLE;
            integrateTransforms();
            pitchAngle = 0;
        } else if(keyCode == KeyEvent.VK_PAGE_UP){
            pitchAngle = MAX_ANGLE;
            integrateTransforms();
            pitchAngle = 0;
        }
    }
    /**
     * If processAWTEvents gets a mouseEvent, this method is called
     * to deal with it.  Deals with drags, presses, and releases.
     * @param evt The MouseEvent to process.
     */
    protected void processMouseEvent( MouseEvent evt ) {
        
        float offsetX = (evt.getX() - canvasCenter.x) / canvasCenter.x;
        float offsetY = (evt.getY() - canvasCenter.y) / canvasCenter.y;
        float factorX=0f;
        float factorY=0f;
        
        if (Math.abs(offsetX)-deadXSize > 0f) {
            factorX = (float)Math.pow( Math.abs(offsetX)-deadXSize, 2f );
            motion = true;
        } else {
            factorX = 0f;
        }
        
        if (Math.abs(offsetY)-deadYSize > 0f) {
            factorY = (float)Math.pow( Math.abs(offsetY)-deadYSize, 2f );
            motion = true;
        } else {
            factorY = 0f;
        }
        
        if (offsetX>0f)
            factorX = -factorX;
        
        if (offsetY<0f)
            factorY = -factorY;
        
        if (SwingUtilities.isLeftMouseButton( evt ) ) {
            if (evt.getID()==MouseEvent.MOUSE_RELEASED) {
                yawAngle = 0;
                velocity.z = 0;
                motion = false;
            } else {
                //System.out.println( factorX +" "+factorY );
                yawAngle = MAX_ANGLE * factorX;
                velocity.z = maxVelocity * factorY * 5;
            }
        } else if ( SwingUtilities.isRightMouseButton( evt )) {
            if (evt.getID()==MouseEvent.MOUSE_RELEASED) {
                velocity.x = 0;
                velocity.y = 0;
                motion = false;
            } else {
                velocity.x = maxVelocity * factorX * -2;
                velocity.y = maxVelocity * factorY * -2;
            }
        } else if ( SwingUtilities.isMiddleMouseButton( evt )) {
            if (evt.getID()==MouseEvent.MOUSE_RELEASED) {
                pitchAngle = 0f;
                rollAngle = 0f;
                motion = false;
            } else {
                pitchAngle = MAX_ANGLE * factorY;
                rollAngle = MAX_ANGLE * factorX;
            }
        } else {
            yawAngle = 0f;
            pitchAngle = 0f;
            rollAngle = 0f;
            velocity.x = 0;
            velocity.z = 0;
            velocity.y = 0;
            motion = false;
        }
    }
    
    /**
     * Changes yaw, pitch, roll and velocity and sets the
     * Transform3D of targetTG to account for all of these.
     */
    protected void integrateTransforms() {
        yawTransform.rotY( yawAngle );
        pitchTransform.rotX( pitchAngle );
        rollTransform.rotZ( rollAngle );
        
        velocityTransform.set( velocity );
        velocityTransform.mul( yawTransform );
        velocityTransform.mul( pitchTransform );
        velocityTransform.mul( rollTransform );
        
        targetTG.getTransform( targetTransform );
        targetTransform.mul( velocityTransform );
        targetTG.setTransform( targetTransform );
    }
    
    
    
    /**
     * Resets the roll and pitch angles to give you a flat view of the models.
     */
    public void levelOff(){
        Transform3D baseTransform = new Transform3D();
        targetTG.getTransform(baseTransform);
        
        Vector3f platformVect = new Vector3f();
        baseTransform.get(platformVect);
        
        Transform3D tVect = new Transform3D();
        Transform3D tXRot = new Transform3D();
        Transform3D tZRot = new Transform3D();
        AxisAngle4f yAngle = new AxisAngle4f();
        Transform3D tYRot = new Transform3D();
        Matrix3f m = new Matrix3f();
        baseTransform.get(m);
        yAngle.set(m);
        tVect.set(platformVect);
        
        /*System.out.println(yAngle.x);
        System.out.println(yAngle.y);
        System.out.println(yAngle.z);
        System.out.println(Math.toDegrees(yAngle.angle));*/
        if(yAngle.y < 0){
            yAngle.angle = -yAngle.angle;
        }
        tYRot.rotY(yAngle.angle);
        tZRot.rotZ(Math.toRadians(0));
        
        tXRot.rotX(Math.toRadians(0));
        tVect.mul(tXRot);
        tVect.mul(tZRot);
        tVect.mul(tYRot);
        targetTG.setTransform(tVect);
    }
    /**
     * sets the maximum speed.
     * useful to set to low quantities when dealing with smaller models.
     * @param n new maximum speed. Must be greater than zero.
     */
    public void setMaxSpeed(float n){
        if(n > 0){
            maxVelocity = n;
        }
    }
    
    /**
     * Should change the view orientation and position to view a the target from the air.
     */
    public abstract void aerialView();
}
