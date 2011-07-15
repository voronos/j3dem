package edu.umn.d.fileformats;

import edu.umn.d.geometry.GroundCoordinates;
import java.net.MalformedURLException;
import java.net.URL;
/**
 * ElevationFile is an abstract base class used to define the interface
 * between  files holding terrain data and the JAVA3D classes that
 * convert the terrain data into geometric primitives.
 *
 * @author  Mark Pendergast
 * @version 1.0 February 2003
 * Known subclasses @see DemFile
 */
public class ElevationFile{
    /**
     * Path to the file, not including the file name.
     */
    public String filePath;
    /** data file name */
    public String fileName;
    /** geographic name or title of the data */
    public String quadrangleName = "";
    /** minimum elevation in meters */
    public float minElevation = 0;
    /** maximum elevation in meters */
    public float maxElevation = 0;
    /** two dimensional array of elevation data in meters, array represents equally spaced data points across the
     * groundCoordinates. The first dimension is the row, the second the column*/
    public float[][] elevations = null; // array of raw elevation data
    /** number of data rows */
    public int nRows;
    /** number of data columns */
    public int nColumns;
    /** holds ground coordinates of the 4 corners in arc seconds */
    public GroundCoordinates groundCoordinates = new GroundCoordinates();
    /**
     * The overall resolution of the file.  Typically will be 24k for a USGS SDTS DEM.
     */
    protected float resolution;
    
    public ElevationFile(URL filename){
        String[] splitFile = filename.getFile().split("/");
        filePath = filename.getProtocol() + "://" + filename.getHost();
        //System.out.println("ElevationFile: filePath before rebuild is " + filePath);
        for(int i = 0; i < splitFile.length - 1; i++){
            filePath += splitFile[i] + "/";
        }
        
        fileName = splitFile[splitFile.length - 1];
        //System.out.println("ElevationFile: filePath is " + filePath + "\n\tfileName is " + fileName);
    }
    
    /**
     * Accessor for the resolution field of the ElevationFile.
     * @return The resolution.
     */
    public float getResolution(){
        return resolution;
    }
}

