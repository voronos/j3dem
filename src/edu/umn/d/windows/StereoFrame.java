package edu.umn.d.windows;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.View;
import javax.swing.*;
import javax.vecmath.Point3d;

/**
 * Main frame to be used in applications.
 * Holds the left and right viewPanels.
 * @author nels2426
 */
public class StereoFrame extends JFrame{
    private View3DPanel vp;
    private StatusWindow stat;
    /**
     * The width of the left and right frames.  Set to half of the current screen resolution width.
     */
    public static int SCREEN_WIDTH;
    
    /**
     * The height of the left and right frames.  Set so that it forms a 4:3 ratio with FRAME_WIDTH.
     */
    public static int SCREEN_HEIGHT;
    public final static int STANDARD_WIDTH = 640;
    public final static int STANDARD_HEIGHT = 480;
    public StereoFrame(URL file){
        super("Stereo Viewing");
        initialize();
        vp.addModel(file,null);
        vp.setRendering(true);
        pack();
        hideRightPanel();
        setVisible(true);
        System.gc();
    }
    /**
     * Creates a new instance of StereoFrame
     */
    public StereoFrame() {
        super("jdemview - View Window");
        initialize();
        FileDialog dialog = new FileDialog(this, "Open Dem file", FileDialog.LOAD);
        dialog.setDirectory(".");
        dialog.setVisible(true);
        if(dialog.getFile() != null){ // did the user select a file?
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            stat = new StatusWindow();
            stat.setLocation(SCREEN_WIDTH / 3, SCREEN_HEIGHT / 3);
            stat.setVisible(true);
            String fileName = dialog.getDirectory()+dialog.getFile(); // create a full file name
            
            //System.out.println("StereoFrame: fileName is " + fileName);
            try{
                stat.setLabel1(fileName);
                URL u = new URL("file:///" + fileName.replace("\\", "/"));
                vp.addModel(u,stat);
            } catch(IllegalArgumentException exception) {
                JOptionPane.showMessageDialog(this,exception.getMessage(),
                        "Dem file load error",JOptionPane.ERROR_MESSAGE);  // popup an error message
            }catch (MalformedURLException e){
                e.printStackTrace();
                System.exit(0);
            }
            stat.dispose();
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }//*/
        vp.setRendering(true);
        pack();
        hideRightPanel();
        setVisible(true);
        System.gc();
    }
    
    private void initialize(){
        GraphicsEnvironment ge = GraphicsEnvironment.
                getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle bounds = gc.getBounds();
        SCREEN_WIDTH = (int)bounds.getWidth();
        SCREEN_HEIGHT = (int)bounds.getHeight();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.X_AXIS));
        
        ControlWindow cw = new ControlWindow(this);
        vp = new View3DPanel(cw);
        cw.setViewWindow(vp);
        c.add(vp);
        //c.add(Box.createRigidArea(new Dimension(1,0)));
    }
    
    public StatusWindow getStatusWindow(){
        return stat;
    }
    public void hideRightPanel(){
        setSize(STANDARD_WIDTH, STANDARD_HEIGHT);
        setLocation(200, 0);
        vp.setStereo(false);
    }
    
    public void showRightPanel(){
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setLocation(0,0);
        vp.setStereo(true);
    }
    
}
