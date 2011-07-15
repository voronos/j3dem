/*
 * XYZFile.java
 *
 * Created on March 4, 2006, 3:16 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package edu.umn.d.fileformats;

import edu.umn.d.geometry.GroundCoordinates;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.vecmath.Point3f;


/**
 *
 * @author Mark Nelson
 */
public class XYZFile extends ElevationFile{
    private BufferedInputStream reader;
    //public Hashtable elevationRows;
    private Vector dataPoints;
    public Point3f[] elevationPoints;
    
    /**
     * Since the data is not necesarrily arranged in a rectangular grid, we need to specify the
     * max number of columns that can be in a row for the model segments.  Not all of these columns
     * will be used for all rows.
     */
    
    /** Creates a new instance of XYZFile */
    public XYZFile(URL file) {
        super(file);
        dataPoints = new Vector();
        nRows = 0;
        nColumns = 0;
        //int lastColumns = 0;
        int curColumns = 1;
        float lastX = 0;
        float curX = 0;
        float maxX = 0;
        float minX = 0;
        float lastY = 0;
        float curY = 0;
        float minY = 0;
        float maxY = 0;
        float minZ = 0;
        float maxZ = 0;
        float curZ = 0;
        //Vector zValues = new Vector();
        // holds all z-values for the current column.
        //Vector column = new Vector();
        //only column will be added to rows.
        //Vector rows = new Vector();
        //elevationRows = new Hashtable();
        //Hashtable columns = new Hashtable();
        try{
            reader = new BufferedInputStream(file.openStream());
            String line = readLine();
            String pattern;
            if(line.contains(",")){
                pattern = ",";
            }else{
                pattern = "\\s";
            }
            String[] xyz = line.split(pattern);
            lastX = curX = maxX = minX = Float.parseFloat(xyz[0]);
            lastY = curY = maxY = minY = Float.parseFloat(xyz[1]);
            curZ = minZ = maxZ = Float.parseFloat(xyz[2]);
            dataPoints.add(new Point3f(curX, curY,  curZ));
            curColumns++;
            nRows++;
            while(reader.available() > 0){
                line = readLine();
                //System.out.println(line);
                xyz = line.split(pattern);
                lastX = curX;
                curX = Float.parseFloat(xyz[0]);
                curY = Float.parseFloat(xyz[1]);
                curZ = Float.parseFloat(xyz[2]);
                dataPoints.add(new Point3f(curX, curY,  curZ));
                // check to see if min/max X is still true
                if(curX > maxX){
                    maxX = curX;
                }else if(curX < minX){
                    minX = curX;
                }
                
                //check to see that we have the current min/max Y
                if(curY > maxY){
                    maxY = curY;
                }else if(curY < minY){
                    minY = curY;
                }
                
                //check to see that we have the current min/max Z
                if(curZ > maxZ){
                    maxZ = curZ;
                }else if(curZ < minZ){
                    minZ = curZ;
                }
                
                //if the X value has changed from the last value it was, a new row has started.
                if(curX != lastX){
                    nRows++;
                    //if the number of columns in this row is greater than the previous number of columns, update the number of columns
                    if(curColumns > nColumns){
                        nColumns = curColumns;
                    }
                    curColumns = 1;
                }
                curColumns++;
            }
            if(curColumns > nColumns){
                nColumns = curColumns;
            }
            groundCoordinates.ne[GroundCoordinates.LONGITUDE] = maxX;
            groundCoordinates.nw[GroundCoordinates.LONGITUDE] = minX;
            groundCoordinates.se[GroundCoordinates.LONGITUDE] = maxX;
            groundCoordinates.sw[GroundCoordinates.LONGITUDE] = minX;
            groundCoordinates.ne[GroundCoordinates.LATITUDE] = maxY;
            groundCoordinates.se[GroundCoordinates.LATITUDE] = minY;
            groundCoordinates.nw[GroundCoordinates.LATITUDE] = maxY;
            groundCoordinates.sw[GroundCoordinates.LATITUDE] = minY;
            minElevation = minZ;
            maxElevation = maxZ;
            quadrangleName = fileName;
            
            //System.out.println("XYZFile: number of rows passed in " + rowCount);
            //System.out.println("XYZFile: maximum number of columns is " + nColumns);
            //elevations = new float[nRows][nColumns];
            //System.out.println("XYZFile:  nRows = " + nRows + "\nXYZFile: nColumns = " + nColumns);
            
            elevationPoints = new Point3f[dataPoints.size()];
            dataPoints.toArray(elevationPoints);
        } catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    private String readLine(){
        String line = "";
        char letter;
        // read a line from the file.
        try{
            do{
                letter = (char)reader.read();
                line += letter;
            }while(letter != '\n');
        }catch (Exception e){
            e.printStackTrace();
        }
        return line;
    }
}
