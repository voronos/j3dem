package edu.umn.d.windows;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.awt.image.BufferedImage;
import edu.umn.d.fileformats.JpegImagesToMovie;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import javax.media.MediaLocator;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsContext3D;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Raster;
import javax.vecmath.Point3f;

/** Class CapturingCanvas3D, using the instructions from the Java3D
 * FAQ pages on how to capture a still image in jpeg format.
 *
 * A capture button would call a method that looks like
 *
 *
 * public static void captureImage(CapturingCanvas3D MyCanvas3D) {
 * MyCanvas3D.writeJPEG_ = true;
 * MyCanvas3D.repaint();
 * }
 *
 *
 * Peter Z. Kunszt
 * Johns Hopkins University
 * Dept of Physics and Astronomy
 * Baltimore MD
 */

public class CapturingCanvas3D extends Canvas3D  {
    
    /**
     * If true, the next render pass is converted to jpeg format and written to the home directory as captureN.jpg
     */
    public boolean writeJPEG_;
    public boolean startRecording;
    public Vector images;
    private int postSwapCount_;
    
    /**
     * Constructs a new CapturingCanvas3D
     * @param gc a valid GraphicsConfiguration object that will be used to create the canvas.
     * This object should not be null and should be created using a GraphicsConfigTemplate3D or
     * the getPreferredConfiguration() method of the SimpleUniverse utility.
     * For backward compatibility with earlier versions of Java 3D, a null or
     * default GraphicsConfiguration will still work when used to create a Canvas3D on the
     * default screen, but an error message will be printed. A NullPointerException or
     * IllegalArgumentException will be thrown in a subsequent release
     */
    public CapturingCanvas3D(GraphicsConfiguration gc) {
        super(gc);
        writeJPEG_ = false;
        startRecording = false;
        postSwapCount_ = 0;
        images = new Vector();
    }
    
    /**
     * Method that actually process the render into a jpg file.
     */
    public void postSwap() {
        if(writeJPEG_ || startRecording) {
            //System.out.println("Writing JPEG");
            GraphicsContext3D  ctx = getGraphicsContext3D();
            // The raster components need all be set!
            Dimension d = getSize();
            Raster ras = new Raster(
                    new Point3f(0.0f,0.0f,0.0f),
                    Raster.RASTER_COLOR,
                    0,0,
                    d.width,d.height,
                    new ImageComponent2D(
                    ImageComponent.FORMAT_RGB,
                    new BufferedImage(d.width,d.height,
                    BufferedImage.TYPE_INT_RGB)),
                    null);
            ctx.readRaster(ras);
            
            // Now strip out the image info
            BufferedImage img = ras.getImage().getImage();
            
            // write that to disk....
            try {
                FileOutputStream out = new FileOutputStream("Capture"+postSwapCount_+".jpg");
                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(img);
                param.setQuality(1.0f, true);
                encoder.setJPEGEncodeParam(param);
                encoder.encode(img);
                if(startRecording){
                    images.add("Capture"+postSwapCount_+".jpg");
                }
                writeJPEG_ = false;
                out.close();
            } catch ( IOException e ) {
                System.out.println("I/O exception!");
            }
            postSwapCount_++;
        }
    }
    
    public void stopRecording(){
        JpegImagesToMovie movieEncoder = new JpegImagesToMovie();
        // Generate the output media locators.
        MediaLocator oml;
        
        if ((oml = JpegImagesToMovie.createMediaLocator("movie" + (postSwapCount_++) + ".mov")) == null) {
            System.err.println("Cannot build standard media locator");
            System.exit(0);
        }
        movieEncoder.doIt(StereoFrame.SCREEN_WIDTH, StereoFrame.SCREEN_HEIGHT, 30, images, oml);
        Iterator i = images.iterator();
        while(i.hasNext()){
            File f = new File((String)i.next());
            f.delete();
        }
        startRecording = false;
    }
}
