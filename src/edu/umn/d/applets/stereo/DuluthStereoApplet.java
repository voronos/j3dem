/*
 * DuluthStereoApplet.java
 *
 * Created on August 14, 2005, 9:21 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package edu.umn.d.applets.stereo;
import java.awt.Container;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;


/**
 * Loads the Duluth data for stereo viewing.
 * @author nels2426
 */
public class DuluthStereoApplet extends JApplet {
    
    /** Creates a new instance of DuluthStereoApplet */
    public DuluthStereoApplet() {
    }
    /**
     * sets size to (hopefully) fullscreen and loads the Duluth data.
     */
    /*
    public void init(){
        try{
            setSize(LeftPanel.WIDTH*2, LeftPanel.HEIGHT);
            URL file = new URL("http://www.d.umn.edu/~nels2426/DuluthSDTS/7172iden.ddf");
            
            DEMViewManager vm = new DEMViewManager(file);
            DEMViewerRightPanel rightVP = new DEMViewerRightPanel(vm, vm.getModel());
            DEMViewerLeftPanel leftVP = new DEMViewerLeftPanel(vm, rightVP.getCamera2TG(), vm.getModel());
            
            Container c = getContentPane();
            c.setLayout(new BoxLayout(c, BoxLayout.X_AXIS));
            c.add(leftVP);
            
            Box vertBox = Box.createVerticalBox();
            vertBox.add(rightVP);
            c.add(vertBox);
        } catch(MalformedURLException e){
            e.printStackTrace();
            System.exit(0);
        }
    }*/
}
