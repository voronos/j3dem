package edu.umn.d.geometry;
import java.awt.Color;
import java.util.Hashtable;
import javax.vecmath.*;
import javax.media.j3d.*;
/**
 *
 *LOD segment is based on a TransformGroup. Each LODSegment is actually
 * comprised of three ElevationSegments, each created with a different
 * resolution. Switch and DistanceLOD objects are used to determine
 * which of the three to render (based on how far away it is from the
 * viewing platform).  This speeds up rendering and allows real-time
 * fly throughs of the scene.
 *
 * @author  Mark Pendergast
 * @version 1.0 February 2003
 */
public class LODSegment extends TransformGroup {
    /** Ground coordinates, lat, long, of the segment */
    //public GroundCoordinates groundCoordinates;
    /** resolutions of Elevation segments, 1 implies every point, 10 implies every 10th point */
    private int resolutions[] = {1,3,5};
    /** distance array used to determine which ElevationSegment to display. Computed based on model */
    private float[] distances;
    /** ElevationSegments, one for each resolution */
    private ElevationSegment[] segments;
    /** Switch node to control ElevationSegments*/
    private Switch switchNode = new Switch();
    /** DistanceLOD node to tell Switch which one to display */
    private DistanceLOD dLOD;
    
    /*public LODSegment(Hashtable elevations, int startRow, int stopRow, float minEl, float maxEl, float exageration, float minX, float maxX, float minZ, float maxZ, float modelWidth, float modelLength){
        super();
        setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        
        // initialize the switch node and create the child segments in varying resolutions
        switchNode.setCapability(Switch.ALLOW_SWITCH_WRITE);
        segments = new ElevationSegment[resolutions.length];
        for(int i = 0; i < resolutions.length; i++) {
            segments[i] = new ElevationSegment(elevations, startRow, stopRow,
                    minEl,maxEl,exageration,minX,maxX,minZ,maxZ,resolutions[i], modelWidth, modelLength);
            switchNode.addChild(segments[i]);
        }
        
        // set the position and bounds of the object
        Point3f position = new Point3f((float)((maxX+minX)/2),  maxEl*exageration,(float)((maxZ+minZ)/2));
        Bounds bounds = new BoundingSphere(new Point3d(0,0,0),Double.MAX_VALUE);
        
        //  calculate distances based on size of segment (east-west length)
        distances = new float[resolutions.length-1];
        for(int i=0; i < distances.length; i++)
            distances[i] = Math.abs((float)((i+1)*2*(maxX-minX)));

        //  create the distanceLOD object
        dLOD = new DistanceLOD(distances,position);
        dLOD.setSchedulingBounds(bounds);
        dLOD.addSwitch(switchNode);

        // add the switch and the distance lod to this object
        addChild(dLOD);
        addChild(switchNode);
    }*/
    /**
     * Constructor is for creating the series of objects that allow level of
     * detail based on distance displays.
     * @param modelWidth The width of the ElevationModel to which this segment belongs.
     * 
     * @param modelLength The length of the ElevationModel to which this segment belongs.
     * @param elevations two dimensional array of terrain elevation data in meters
     * @param startRow first row of data to use from elevations
     * @param startColumn first column of data to use from elevations
     * @param stopRow last row of data to use from elevations
     * @param stopColumn last column of data to use from elevations
     * @param minEl minimum elevation in elevations
     * @param maxEl maximum elevation in elevations
     * @param exageration amount to exagerate(multiply) elevations by
     * @param minX starting x coordinate in meters
     * @param maxX stopping x coordinate in meters
     * @param minZ starting z coordinate in meters
     * @param maxZ stopping z coordinate in meters
     */
    
    public LODSegment( float elevations[][], int startRow, int startColumn, int stopRow, int stopColumn,float minEl, float maxEl, float exageration,float minX, float maxX, float minZ, float maxZ, float modelWidth, float modelLength) {
        super();
        setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        
        // initialize the switch node and create the child segments in varying resolutions
        switchNode.setCapability(Switch.ALLOW_SWITCH_WRITE);
        segments = new ElevationSegment[resolutions.length];
        for(int i = 0; i < resolutions.length; i++) {
            segments[i] = new ElevationSegment(elevations, startRow,startColumn, stopRow,stopColumn,
exageration,minX,maxX,minZ,maxZ,resolutions[i], modelWidth, modelLength);
            switchNode.addChild(segments[i]);
        }
        
        // set the position and bounds of the object
        Point3f position = new Point3f((float)((maxX+minX)/2),  maxEl*exageration,(float)((maxZ+minZ)/2));
        Bounds bounds = new BoundingSphere(new Point3d(0,0,0),Double.MAX_VALUE);
        
        //  calculate distances based on size of segment (east-west length)
        distances = new float[resolutions.length-1];
        for(int i=0; i < distances.length; i++)
            distances[i] = Math.abs((float)((i+1)*3*(maxX-minX)));

        //  create the distanceLOD object
        dLOD = new DistanceLOD(distances,position);
        dLOD.setSchedulingBounds(bounds);
        dLOD.addSwitch(switchNode);

        // add the switch and the distance lod to this object
        addChild(dLOD);
        addChild(switchNode);
    }
    /**
     * retrieves an elevation segment object
     * @param i index of the segment desired
     * @return reference to the ith ElevationSegment object
     */
    public ElevationSegment getSegment(int i) {
        return segments[i];
    }
    
    /**
     *  stitch right of this segment to left of other segment, stitching
     *  consists of averaging the normals on the common boundary vertices
     * between segments. That is, since adjacent segments have the same
     * vertices in common (the left side of one is the same as the right side of
     * the other), then these normals should be the same as well to prevent
     *  visible seams.
     *
     *  @param other reference to the segment to the right of this one
     */
    public void stitchRight(LODSegment other) {
        for(int i = 0; i < resolutions.length; i++)
            segments[i].stitchRight(other.getSegment(i));
    }
    /**
     *  stitch top of this segment to bottom of other segment, stitching
     *  consists of averaging the normals on the common boundary vertices
     * between segments. That is, since adjacent segments have the same
     * vertices in common (the top of one is the same as the bottom of
     * the other), then these normals should be the same as well to prevent
     *  visible seams.
     *
     *  @param other reference to the segment to the right of this one
     */
    public void stitchTop(LODSegment other) {
        for(int i = 0; i < resolutions.length; i++)
            segments[i].stitchTop(other.getSegment(i));
    }
    /**
     * Fetches the elevation at a particular location on the terrain map given
     * the x,z coordinates.  X,Z coordinates represent the distance in meters
     * from the center of the terrain image
     * @param x x coordinate
     * @param z z coordinate
     * @return the elevation (y coordinate) in meters (adjusted for exageration)
     */
    public float getElevationAt(float x, float z) {
        return segments[0].getElevationAt(x,z);
    }
    
    /**
     * Loops through the ElevationSegments and calls their setTexture method.
     * @param tex The new texture to apply.
     */
    public void setTexture(Texture2D tex){
        for (int i = 0; i < segments.length;i++){
            segments[i].setTexture(tex);
        }
    }
    
    /**
     * Loops through the ElevationSegments and calls their changeExageration method.
     * @param oldAmount The old exageration value.  Used to reset to natural exageration.
     * @param newAmount The desired exageration amount.
     */
    
    public void changeExageration(float oldAmount, float newAmount){
        for(int i = 0; i < segments.length; i++){
            segments[i].changeExageration(oldAmount, newAmount);
        }
    }
    
    /**
     * Recalculates the normal values so that the lighting effects change when the elevation exageration change.
     */
    public void recalcNormals(){
        for(int i = 0; i < segments.length; i++){
            segments[i].recalcNormals();
        }
    }
    
    public void setColor(Color color){
        for(int i = 0; i < segments.length; i++){
            segments[i].setColor(color);
        }
    }
    
    public void setTextureMode(int mode){
        for(int i = 0; i < segments.length; i++){
            segments[i].setTextureMode(mode);
        }
    }
}// end of class
