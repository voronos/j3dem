package edu.umn.d.fileformats;
import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author nels2426
 */
public class ImageFileFilter implements FileFilter{
    /** Creates a new instance of ImageFileFilter */
    public ImageFileFilter() {
    }
    
    public boolean accept(File pathname){
        if(pathname.getName().contains(".png")||pathname.getName().contains(".jpg")||
                pathname.getName().contains(".gif")||pathname.getName().contains(".jpeg")){
            return true;
        } else{
            return false;
        }
    }
    
    
    
}
