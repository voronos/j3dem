/*
 * NewApplet.java
 *
 * Created on February 8, 2006, 3:32 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package edu.umn.d.applets;
import edu.umn.d.windows.AppletControlWindow;
import edu.umn.d.windows.CameraButtons;
import edu.umn.d.windows.View3DPanel;
import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Mark Nelson
 */
public class DuluthHeightsApplet extends java.applet.Applet {
    
    /** Initialization method that will be called after the applet is loaded
     *  into the browser.
     */
    public void init() {
        try{
            setLayout(new BorderLayout());
            URL u = new URL("http://www.d.umn.edu/~nels2426/DuluthHeightsSDTS/9245IDEN.DDF");
            AppletControlWindow cw = new AppletControlWindow();
            cw.setVisible(false);
            JPanel cameraPanel = new JPanel();
            CameraButtons cb = new CameraButtons(cw);
            cameraPanel.add(new JLabel("Camera Type"));
            cameraPanel.add(cb.getFly());
            cameraPanel.add(cb.getOrbit());
            add(BorderLayout.NORTH, cameraPanel);
            View3DPanel vp = new View3DPanel(cw);
            cw.setViewWindow(vp);
            add("Center", vp);
            vp.addModel(u, null);
        }catch (MalformedURLException e){
            e.printStackTrace();
            System.exit(0);
        }//*/
        
    }
    // TODO overwrite start(), stop() and destroy() methods
}
