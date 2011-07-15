package edu.umn.d.behaviors;

import edu.umn.d.geometry.ElevationModel;
import java.awt.AWTEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Enumeration;
import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOr;


/**
 * Used to have the mouse wheel up and mouse wheel down to change the elevation exageration of the models.
 * @author nels2426
 */
public class MouseWheelBehavior extends Behavior{
    private ElevationModel model;
    private WakeupOr conditions;
    /**
     * Constructs a new instance of MouseWheelBehavior.
     * @param model The model this behavior will affect.
     */
    public MouseWheelBehavior(ElevationModel model){
        this.model = model;
        System.out.println("MouseWheelBehavior created");
    }
    
    /**
     * Initializes the behavior. NOTE: Applications should not call this method. It is called by the Java 3D behavior scheduler.
     */
    public void initialize(){
        WakeupOnAWTEvent e = new WakeupOnAWTEvent(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        WakeupOnAWTEvent e2 = new WakeupOnAWTEvent(AWTEvent.MOUSE_MOTION_EVENT_MASK);
        //WakeupOnAWTEvent e3 = new WakeupOnAWTEvent(AWTEvent.MOUSE_EVENT_MASK);
        conditions = new WakeupOr( new WakeupCriterion[] { e, e2 } );
        wakeupOn(e);
    }
    
    /**
     * Process a stimulus meant for this behavior. This method is invoked if the Behavior's wakeup criteria are satisfied and an active ViewPlatform's activation volume intersects with the Behavior's scheduling region. Classes that extend Behavior must provide their own processStimulus method.
     * NOTE: Applications should not call this method. It is called by the Java 3D behavior scheduler.
     * @param e an enumeration of triggered wakeup criteria for this behavior
     */
    public void processStimulus(Enumeration e) {
        System.out.print("stimulus received for MouseWheelBehavior: ");
        while(e.hasMoreElements()) {
            WakeupCondition wakeup = (WakeupCondition)e.nextElement();
            AWTEvent[] event = ((WakeupOnAWTEvent)wakeup).getAWTEvent();
            for(int i = 0; i < event.length; i++){
                System.out.println("event was " + event[i]);
                if(event[i] instanceof MouseWheelEvent){
                    float amount = ((MouseWheelEvent)event[i]).getWheelRotation();
                    System.out.println("stimulus was mouse wheel event");
                    amount = model.exageration + (amount * -1f);
                    if(amount > 0){
                        model.changeExageration(amount);
                        System.out.println("Model exageration should be changed.");
                    }
                }
            }
        }
        wakeupOn(conditions);
    }
}
