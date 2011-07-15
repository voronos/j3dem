package edu.umn.d.windows;

import javax.swing.*;
import java.awt.*;

import com.sun.j3d.utils.universe.*;
import edu.umn.d.behaviors.DEMViewerFlyBehavior;
import edu.umn.d.behaviors.MyOrbitBehavior;
import edu.umn.d.geometry.ElevationModel;
import edu.umn.d.windows.StereoFrame;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.media.j3d.*;
import javax.vecmath.*;
/**
 *  This class, based on JPanel, creates a Panel that displays a
 *  SimpleUniverse, complete with ambient and directional lights.
 *  View3DPanel also provides the capability to load and display
 *  DEM data, and view the data using a FlyingPlatform.
 *
 * @author  Mark Pendergast
 * @version 1.0 February 2003
 *
 */
public class View3DPanel extends JPanel{
    private boolean firstTime = true;
    private TransformGroup lights;
    /** degrees in the field of view */
    protected static final int FIELD_OF_VIEW = 45;
    /** background color */
    protected static final Color3f backgroundColor = new Color3f(0.0f,0.0f,0.8f);
    /** ambient light color */
    private Color3f ambientColor = new Color3f(.2f,.2f,.2f);
    /** directional light color */
    protected static final Color3f directionalColor = new Color3f(1f,1f,1f);
    
    /**
     * The SimpleUniverse.
     */
    public SimpleUniverse universe = null;
    /**
     * The canvases.
     */
    protected CapturingCanvas3D leftCanvas = null;
    protected CapturingCanvas3D rightCanvas = null;
    /**
     * Reference to the last added currentModel.
     */
    protected ElevationModel currentModel = null;
    
    /**
     * A list of all the models currently viewable.
     */
    protected ArrayList modelList = new ArrayList();
    /**
     * The branch group that contains the content.
     */
    protected BranchGroup world = null;
    /**
     * Infinite bounds to use for background and lights.
     */
    protected BoundingSphere infiniteBounds = new BoundingSphere(new Point3d(), Double.MAX_VALUE);
    protected ControlWindow controlWindow;
    protected MyOrbitBehavior orbitBehavior;
    protected DEMViewerFlyBehavior flyBehavior;
    private DirectionalLight dirLight;
    /**
     *
     *  This class, based on JPanel, creates a Panel that displays a
     *  SimpleUniverse complete with ambient and directional lights
     *  @param controlWindow The control window associated with this view.
     */
    public void setStereo(boolean enable){
        if(enable){
            leftCanvas.setMonoscopicViewPolicy(View.LEFT_EYE_VIEW);
            leftCanvas.setSize(StereoFrame.SCREEN_WIDTH / 2, StereoFrame.SCREEN_HEIGHT);
            rightCanvas.setSize(StereoFrame.SCREEN_WIDTH / 2, StereoFrame.SCREEN_HEIGHT);
            add(rightCanvas);
        } else{
            leftCanvas.setMonoscopicViewPolicy(View.CYCLOPEAN_EYE_VIEW);
            leftCanvas.setSize(StereoFrame.STANDARD_WIDTH, StereoFrame.STANDARD_HEIGHT);
            rightCanvas.setSize(StereoFrame.STANDARD_WIDTH, StereoFrame.STANDARD_HEIGHT);
            this.remove(rightCanvas);
        }
    }
    public View3DPanel(ControlWindow controlWindow) {
        this.controlWindow = controlWindow;
        // add a Canvas to the center of the panel
        //setLayout(new BorderLayout());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        GraphicsConfiguration config =
                SimpleUniverse.getPreferredConfiguration();
        leftCanvas = new CapturingCanvas3D(config);
        leftCanvas.stopRenderer();
        //leftCanvas.setSize(StereoFrame.SCREEN_WIDTH, StereoFrame.SCREEN_HEIGHT);
        leftCanvas.setMonoscopicViewPolicy(View.CYCLOPEAN_EYE_VIEW);
        rightCanvas = new CapturingCanvas3D(config);
        rightCanvas.stopRenderer();
        rightCanvas.setMonoscopicViewPolicy(View.RIGHT_EYE_VIEW);
        //rightCanvas.setSize(StereoFrame.SCREEN_WIDTH, StereoFrame.SCREEN_HEIGHT);
        add(leftCanvas);
        add(Box.createRigidArea(new Dimension(1, 1)));
        // Create the branch group to hold the world
        world = new BranchGroup();
        world.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        world.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        
        addLights();
        
        // Create the background
        Background bg = new Background(backgroundColor);
        bg.setApplicationBounds(infiniteBounds);
        world.addChild(bg);
        
        // Create a universe and attach the branch group
        universe = new SimpleUniverse(leftCanvas);
        View view = universe.getViewer().getView();
        view.addCanvas3D(rightCanvas);
        PhysicalBody body = view.getPhysicalBody();
        Point3d left = new Point3d();
        Point3d right = new Point3d();
        left.x = -0.006;
        right.x = 0.006;
        body.setLeftEyePosition(left);
        body.setRightEyePosition(right);
        universe.addBranchGraph(world);
    }
    
    
    /**
     * Adds a new model to the scene once the user is already viewing it.
     * @param fileName The full name of the file to add.
     * @param stat The status window.  May be null.
     */
    public void addModel(URL fileName, StatusWindow stat){
        
        //ElevationModel oldModel = currentModel;
        try{
            currentModel = new ElevationModel(fileName, stat);
        } catch(IOException e){
            e.printStackTrace();
            System.exit(0);
        }
        
        world.addChild(currentModel);
        modelList.add(currentModel);
        controlWindow.addModelButton(currentModel.name);
        controlWindow.addTextureButtons(currentModel.getTextureButtons());
        
        
        
        if(firstTime){
            //  Adjust the view based on the size of the currentModel
            View view = universe.getViewer().getView();
            view.setFrontClipDistance(1);  // allow user to get close to objects
            view.setBackClipDistance(currentModel.getModelLength() * 3);  // allow user to see far off objects
            view.setFieldOfView(Math.toRadians(FIELD_OF_VIEW));
            
            //  setup the viewing platform.
            ViewingPlatform vp = universe.getViewingPlatform();
            orbitBehavior = new MyOrbitBehavior(leftCanvas, modelList);
            orbitBehavior.setSchedulingBounds(infiniteBounds);
            float x = (currentModel.west_X + currentModel.east_X) / 2;
            float y = (currentModel.getMaxElevation() + currentModel.getMinElevation()) / 2;
            float z = (currentModel.north_Z + currentModel.south_Z) / 2;
            orbitBehavior.setRotationCenter(new Point3d(x,y,z));
            int factor = 5000;
            orbitBehavior.setTransFactors(factor, factor);
            orbitBehavior.setZoomFactor(factor);
            vp.setViewPlatformBehavior(orbitBehavior);
            orbitBehavior.aerialView();
            
            flyBehavior = new DEMViewerFlyBehavior(modelList, vp.getViewPlatformTransform());
            flyBehavior.setSchedulingBounds(infiniteBounds);
            firstTime = false;
        }
    }
    
    /**
     * removes the last added model from the scene.
     * @return The name of the model removed.  Used for removing it as an option from the remove menu.
     */
    public String removeModel(){
        String returnString = currentModel.name;
        world.removeChild(currentModel);
        modelList.remove(currentModel);
        if(modelList.size() > 0){
            currentModel = (ElevationModel)modelList.get(modelList.size()-1);
        } else{
            currentModel = null;
        }
        controlWindow.removeModelButton(returnString);
        return returnString;
    }
    
    /**
     * Remove the named model from the current scene.
     * @param modelName The name of the model to remove.
     */
    public void removeModel(String modelName){
        for(int i = 0; i < modelList.size(); i++){
            ElevationModel cur = (ElevationModel)modelList.get(i);
            if(cur.name.equals(modelName)){
                modelList.remove(i);
                world.removeChild(cur);
                controlWindow.removeModelButton(cur.name);
                if(modelList.size() > 0){
                    currentModel = (ElevationModel)modelList.get(modelList.size()-1);
                } else{
                    currentModel = null;
                }
                break;
            }
        }
    }
    /**
     *  retrieve the elevation currentModel that was last loaded.
     *  @return reference to ElevationModel object last loaded or null
     *    if a currentModel has never been loaded.
     */
    public ElevationModel getModel() {
        return currentModel;
    }
    
    /**
     * retrieve the name ElevationModel
     * @param name The name of the ElevationModel
     * @return The named ElevationModel or null if the name does not match any models currently displayed.
     */
    public ElevationModel getModel(String name){
        for(int i = 0; i < modelList.size(); i++){
            ElevationModel cur = (ElevationModel)modelList.get(i);
            System.out.print("Does " + name + " equal " + cur.name + "?");
            if(cur.name.equals(name)){
                System.out.println("  Yes");
                return cur;
            }else{
                System.out.println("  No");
            }
        }
        return null;
    }
    /**
     * starts and stops rendering
     * @param value if true, then start rendering, else stop rendering
     */
    public void setRendering(boolean value) {
        
        if(value){
            leftCanvas.startRenderer();
            rightCanvas.startRenderer();
        } else{
            leftCanvas.stopRenderer();
            rightCanvas.startRenderer();
        }
    }
    
    /**
     * Get the list of models currently viewable.
     * @return The list of models.
     */
    public ArrayList getModels(){
        return modelList;
    }
    
    /**
     * Returns the SimpleUniverse
     * @return
     */
   /* public SimpleUniverse getUniverse(){
        return universe;
    }*/
    
    /**
     * Changes the ViewPlatformBehavior to the FlyBehavior.
     */
    public void setFlyBehavior(){
        ViewingPlatform vp = universe.getViewingPlatform();
        vp.setViewPlatformBehavior(flyBehavior);
    }
    
    /**
     * Changes the the ViewPlatformBehavior to OrbitBehavior.
     */
    public void setOrbitBehavior(){
        ViewingPlatform vp = universe.getViewingPlatform();
        vp.setViewPlatformBehavior(orbitBehavior);
    }
    
    /**
     * Take a shot of the currently rendered screen.
     */
    public void takeScreenShot(){
        leftCanvas.writeJPEG_ = true;
        leftCanvas.repaint();
    }
    
    /**
     * Changes the elevation exageration of the ElevationModels.
     * @param newAmount The new amount of exageration desired.
     */
    public void changeExageration(float newAmount){
        for(int i = 0; i < modelList.size(); i++){
            ElevationModel m = (ElevationModel)modelList.get(i);
            m.changeExageration(newAmount);
        }
    }
    
    /**
     * Changes the texture of the selected ElevationModel.
     * @param tex The new texture desired.
     */
    public void setTexture(Texture2D tex){
        currentModel = getModel(controlWindow.getSelectedModel());
        setModelColor(Color.white);
        currentModel.setTexture(tex);
    }
    
    /**
     * Removes the current texture from the selected ElevationModel.
     */
    public void removeTexture(){
        currentModel = getModel(controlWindow.getSelectedModel());
        currentModel.setTexture(new Texture2D());
        setModelColor(Color.orange);
    }
    
    /**
     * changes the direction of the directional lights.
     * @param dir The direction of the lights.
     */
    public void setLightDirection(Vector3f dir){
        dirLight.setDirection(dir);
    }
    
    /**
     * Changes the color of the currently selected ElevationModel.
     * @param color The desired new color.
     */
    public void setModelColor(Color color){
        currentModel = getModel(controlWindow.getSelectedModel());
        currentModel.setColor(color);
    }
    
    /**
     * Changes the way the textures are applied to the selected ElevationModel.
     * Valid modes are TextureAttributes.BLEND, TextureAttributes.DECAL, TextureAttributes.MODULATE
     */
    public void setModelTextureMode(int mode){
        currentModel = getModel(controlWindow.getSelectedModel());
        currentModel.setTextureMode(mode);
    }
    
    private void addLights(){
        // create lights
        lights = new TransformGroup();
        AmbientLight ambLight = new AmbientLight(true,ambientColor);
        ambLight.setInfluencingBounds(infiniteBounds);
        ambLight.setCapability(Light.ALLOW_STATE_WRITE);
        ambLight.setEnable(true);
        lights.addChild(ambLight);
        
        // Create the directional lights
        Vector3f dir = new Vector3f(1, -1, 1);
        dirLight = new DirectionalLight(true,directionalColor, dir);
        dirLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
        dirLight.setCapability(Light.ALLOW_STATE_WRITE);
        dirLight.setCapability(Light.ALLOW_COLOR_WRITE);
        dirLight.setCapability(DirectionalLight.ALLOW_DIRECTION_WRITE);
        dirLight.setInfluencingBounds(infiniteBounds);
        dirLight.setEnable(true);
        lights.addChild(dirLight);
        world.addChild(lights);
    }
    
    public void startRecording(){
        leftCanvas.startRecording=true;
    }
    
    public void stopRecording(){
        leftCanvas.stopRecording();
    }
}
