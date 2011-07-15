package edu.umn.d.behaviors;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import edu.umn.d.geometry.ElevationModel;
import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.media.j3d.*;
import javax.vecmath.Vector3f;

/**
 * Simple extension of the OrbitBehavior class to allow use of the home key to go to an aerial view of the models.
 * @author nels2426
 */
public class MyOrbitBehavior extends OrbitBehavior{
    protected ArrayList modelList;
    /**
     * Creates a new instance of MyOrbitBehavior.
     * @param canvas The canvas3d to add the behavior to.
     * @param modelList All models currently in the scene.
     */
    public MyOrbitBehavior(Canvas3D canvas, ArrayList modelList) {
        super(canvas);
        this.modelList = modelList;
        setListenerFlags(MOUSE_LISTENER|MOUSE_MOTION_LISTENER|KEY_LISTENER);
    }
    
    /**
     * Provide a bird's eye view of the models.
     */
    public void aerialView(){
        int numModels = modelList.size();
        float westX = 0;
        float eastX = 0;
        float southZ = 0;
        float northZ = 0;
        float maxElevation = ((ElevationModel)modelList.get(0)).getMaxElevation();
        float modelLength = 0;
        for (int i = 0; i < numModels; i++){
            ElevationModel cur = (ElevationModel)modelList.get(i);
            System.out.println("MyOrbitBehavior: current ground coordinates " + cur.groundCoordinates);
            System.out.println("MyOrbitBehavior: west_X = " + cur.west_X);
            System.out.println("MyOrbitBehavior: east_X = " + cur.east_X);
            System.out.println("MyOrbitBehavior: south_Z = " + cur.south_Z);
            System.out.println("MyOrbitBehavior: north_Z = " + cur.north_Z);
            westX += cur.west_X;
            eastX += cur.east_X;
            southZ += cur.south_Z;
            northZ += cur.north_Z;
            modelLength += cur.getModelLength();
            if(cur.getMaxElevation() > maxElevation){
                maxElevation = cur.getMaxElevation();
            }
        }
        
        westX /= numModels;
        eastX /= numModels;
        southZ /= numModels;
        northZ /= numModels;
        
        Vector3f platformVect = new Vector3f();
        platformVect.x = (westX + eastX) / 2;
        platformVect.z = (southZ + northZ) / 2;
        
        platformVect.y =(float)(Math.abs(maxElevation) * 10);// + modelLength*2);
        System.out.println("camera at " + platformVect.x + ", " + platformVect.y + ", " + platformVect.z);
        System.out.println("max elevation is " + maxElevation);
        Transform3D tVect = new Transform3D();
        Transform3D tXRot = new Transform3D();
        Transform3D tYRot = new Transform3D();
        Transform3D tZRot = new Transform3D();
        tZRot.rotZ(Math.toRadians(180));
        
        tVect.set(platformVect);
        tXRot.rotX(Math.toRadians(-90));
        tVect.mul(tXRot);
        tVect.mul(tZRot);
        
        targetTG.setTransform(tVect);
    }
    
    /**
     * If processStimulus gets an AWT event, it is passed here for processing.
     * @param events All of the events to process.
     */
    public void processAWTEvents(AWTEvent[] events) {
        
        for( int i=0; i < events.length; i++) {
            if (events[i] instanceof KeyEvent) {
                KeyEvent event = (KeyEvent)events[i];
                int keyCode = event.getKeyCode();
                int id = event.getID();
                if(keyCode == KeyEvent.VK_HOME){
                    aerialView();
                }
            } else {
                super.processAWTEvents(events);
            }
        }
    }
}