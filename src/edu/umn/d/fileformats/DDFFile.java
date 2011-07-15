package edu.umn.d.fileformats;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * DDFFile.java
 *
 * Created on July 1, 2005, 11:28 AM
 *
 * Extends {@link ElevationFile} to handle data from USGS SDTS transfers.  These data files have a 4 number
 * identifier followed by a word, and end in a ddf extension.  Not all of the files from a transfer
 * are necessary to build the model.  The only ones this class currently uses is cel0.ddf, ddom.ddf,
 * iden.ddf, and ldef.ddf.
 * Many thanks to Bill Allen and his very helpful website www.3dartist.com/WP/sdts/sdtsnotes.htm
 * for decoding the ddf files.
 *
 */
public class DDFFile extends DDFFileAbstract {
    /**
     * Constructs a new DDFFile.
     * @param aFileName Should be the full path to the file.
     */
    public DDFFile(URL aFileName){
        super(aFileName);
        try{
            findMinMaxElevation();
            findRowColMax();
            findGroundCoordinates();
            findElevations();
            
        } catch (MalformedURLException e){
            e.printStackTrace();
            System.exit(0);
        } catch(IOException e){
            e.printStackTrace();
            System.exit(0);
        }
        System.gc();
    }

    private void findGroundCoordinates() throws MalformedURLException, IOException{
        String location = filePath + fileID + "iden.ddf";
        URL url;
        BufferedInputStream reader;
        try{
            url = new URL(location);
            reader = new BufferedInputStream(url.openStream());
        } catch (FileNotFoundException e){
            location = filePath + fileID + "IDEN.DDF";
            url = new URL(location);
            reader = new BufferedInputStream(url.openStream());
        }
        processGroundCoordinates(reader);
        reader.close();
    }
   
    private void findMinMaxElevation() throws MalformedURLException, IOException{
        String location = filePath + fileID + "ddom.ddf";
        //System.out.println("DDFFile: location is " + location);
        URL url;
        BufferedInputStream reader;
        try{
            url = new URL(location);
            //System.out.println("DDFFile: URL location is " + url.toString());
            reader = new BufferedInputStream(url.openStream());
        } catch (FileNotFoundException e){
            location = filePath + fileID + "DDOM.DDF";
            //System.out.println("DDFFile: location is " + location);
            url = new URL(location);
            //System.out.println("DDFFile: URL location is " + url.toString());
            reader = new BufferedInputStream(url.openStream());
        }
        processMinMaxElevation(reader);
        reader.close();
    }
    
    
    /**
     * Parses the elevation data out of the cel0 file.  Currently not as robust as I would like.
     * Two important assumptions are made.
     * 1. The data records all come in the same format.
     * 2. The elevations are recorded in two byte signed integers.
     * CAUTION! If an integer should fall outside of the minimum and maximum elevations, it is discarded.
     */
    protected void findElevations() throws IOException{
        String location = filePath + fileID + "cel0.ddf";
        URL url;
        BufferedInputStream bReader;
        try{
            url = new URL(location);
            bReader = new BufferedInputStream(url.openStream());
        } catch (FileNotFoundException e){
            location = filePath + fileID + "CEL0.DDF";
            url = new URL(location);
            bReader = new BufferedInputStream(url.openStream());
        }
        processElevations(bReader);
        
        bReader.close();
    }
    
    protected void findRowColMax() throws MalformedURLException, IOException{
        String location = filePath + fileID + "ldef.ddf";
        //System.out.println("location is " + location);
        URL url;
        BufferedInputStream reader;
        try{
            url = new URL(location);
            reader = new BufferedInputStream(url.openStream());
        } catch (FileNotFoundException e){
            location = filePath + fileID + "LDEF.DDF";
            url = new URL(location);
            reader = new BufferedInputStream(url.openStream());
        }
        processRowColMax(reader);
    }
    
}