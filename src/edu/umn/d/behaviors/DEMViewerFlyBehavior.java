package edu.umn.d.behaviors;
import edu.umn.d.geometry.ElevationModel;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

/**
 * Basically, this is the same type of behavior available with Sun's java3d demo.
 * It is just condensed down into one file, rather than several inheritances.
 * Movement is as follows.
 * Left Click - move forward and backward, turn left and right
 * Middle Click - look up and down, and roll
 * Right Click - change elevation, and strafe left and right
 * @author nels2426
 */
public class DEMViewerFlyBehavior extends FlyBehavior{
    /**
     * a list of all the models currently in the scene.
     */
    protected ArrayList modelList;
    
    /**
     * Creates a new FlyBehavior
     * @param modelList A list of all the models that can be seen.  Used for the aerial view method to make sure that all models are viewable.
     * @param tg The transform group this behavior will act on.
     */
    public DEMViewerFlyBehavior(ArrayList modelList, TransformGroup tg){
        init();
        canvasCenter = new Point2D.Float();
        this.modelList = modelList;
        targetTG = tg;
    }
    /** Creates a new instance of DEMViewerFlyBehavior */
    public DEMViewerFlyBehavior() {
    }
    
    /**
     * Moves the targetTG to get an aerial view centered at the average coordinates of all the
     * ElevationModels currently renedered.
     */
    public void aerialView(){
        int numModels = modelList.size();
        float westX = 0;
        float eastX = 0;
        float southZ = 0;
        float northZ = 0;
        float maxElevation = 0;
        float modelLength = 0;
        for (int i = 0; i < numModels; i++){
            ElevationModel cur = (ElevationModel)modelList.get(i);
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
        
        platformVect.y =(float)(maxElevation + modelLength*2.0);
        System.out.println("camera at " + platformVect.x + "," + platformVect.y + "," + platformVect.z);
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
    
}