package edu.umn.d.geometry;
import java.awt.Color;
import javax.vecmath.*;
import javax.media.j3d.*;

/**
 * ElevationSegment is a specialization of Shape3d. It creates a
 * 3D map of terrain data using an interleaved triangle strip array.
 *  Terrain data is passed into ElevationSegment as a two dimensional
 *  array of elevations.  These are converted into a series of triangle
 * strips to represent their geometry.
 *
 * @author  Mark Pendergast
 * @version 1.0 February 2003
 */

public class ElevationSegment extends Shape3D {
    
    /** ground coordinates for the model, contains the lat/long of the four corners in arc seconds */
    /** number of rows of vertex data stored */
    private int dRows;
    /** number of columns of vertex data stored */
    private int dColumns;
    /** meters between columns of data */
    private float deltaX=0;
    /** meters between rows of data */
    private float deltaZ=0;
    /** first column logical coordinate*/
    private float xStart=0;
    /** first row logical coordinate*/
    private float zStart=0;
    protected InterleavedTriangleStripArray tstrip;
    protected static  final int FLOATSPERVERTEX= 8;
    protected static final int TEXT_OFFSET = 0;
    //protected static final int COLOR_OFFSET = 2;
    protected static final int NORMAL_OFFSET = 2;
    protected  static final int COORD_OFFSET = 5;
    protected float[] vertexData;
    protected Color3f matColor;
    private Material mat;
    private Appearance app;
    private float minElevation;
    private float maxElevation;
    private TextureAttributes texatt;
    private final static int VERTICES = 3;
    
    /**
     * This will currently loop through the entire hashtable and add that to the vertexData.
     * I need to figure out some way to use the start and stop rows, but that can wait for a minute or two.
     */
    public ElevationSegment(Point3f[] elevations, int startRow, int stopRow, int startColumn, int stopColumn, float minEl, float maxEl, float exageration, float minX, float maxX, float minZ, float maxZ, int resolution, float modelWidth, float modelLength){
        setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        setupAppearance();
        
        // process the 2D elevation array
        dRows = (int)Math.ceil((stopRow-startRow)/(double)resolution);
        dColumns = (int)Math.ceil((stopColumn-startColumn)/(double)resolution);
        System.out.println("ElevationSegment: dRows = " + dRows);
        System.out.println("ElevationSegment: dColumns = " + dColumns);
        xStart = minX;
        zStart = minZ;
        deltaX = (maxX-minX)/(stopColumn-startColumn);
        deltaZ = (maxZ-minZ)/(stopRow-startRow);
        minElevation = minEl;
        maxElevation = maxEl;
        
        
        // first create an interleaved array of colors,  normals, and points
        //vertexData = new float[FLOATSPERVERTEX*(stopRow)*2*(stopColumn)];
        vertexData = new float[FLOATSPERVERTEX*(elevations.length - (elevations.length % VERTICES))];
        System.out.println("ElevationSegment: vertexData.length " + vertexData.length);
        if(vertexData == null) {
            System.out.println("Elevation segment: memory allocation failure");
            return;
        }
        // populate vertexData a strip at a time
        //int row, col; // used as indices into the elevations array
        //int i; // used as an index into vertexData
        //double lenUnit;
        //double widUnit;
        
        //float startX, startY, curX, curY;
        
        //lenUnit = 1.0 / (elevations[0].length / resolution );// numSegments);
        //widUnit = 1.0 / (elevations.length / resolution );// numSegments);
        //curX = (float) -(lowX / modelLength);  // needs to be negative because otherwise it builds the texture backwards for some reason.
        //curY = (float) (lowZ / modelWidth);
        //startY = (float) (lowZ / modelWidth);
        //int i = 0;
        System.out.println("ElevationSegment: elevations.length = " + (elevations.length));
        /*for(int i = 0, current=0; current < vertexData.length / FLOATSPERVERTEX; current++, i+= FLOATSPERVERTEX){
            // y and z are switched because the planes are oriented differently.
            //System.out.println("ElevationSegment: coordinates " + current + ": " + elevations[current]);
            setCoordinate(elevations[current].x, elevations[current].z, elevations[current].y, i+COORD_OFFSET, exageration);
            setTextureCoordinates(i+TEXT_OFFSET, elevations[current].x, elevations[current].y);
            //i += FLOATSPERVERTEX;
        }*/
        int startOfRow = 0;
        int numInRow = 1;
        int endOfRow = 0;
        for(int j = startOfRow; elevations[j].x != elevations[startOfRow].x; j++){
            numInRow++;
            endOfRow = j;
        }
        for(int i = 0, current = 0; i < vertexData.length; current++){
            if(elevations[current].x != elevations[startOfRow].x){
                startOfRow = current;
                numInRow = 1;
                endOfRow = current;
                for(int j = startOfRow; elevations[j].x != elevations[startOfRow].x; j++){
                    numInRow++;
                    endOfRow = j;
                }
            }
            if(elevations[current] == elevations[endOfRow]){
                setCoordinate(elevations[current].x, elevations[current].z,  elevations[current].y, i + COORD_OFFSET, exageration);
                i+=FLOATSPERVERTEX;
                setCoordinate(elevations[current+numInRow+1].x, elevations[current+numInRow+1].z, elevations[current+numInRow+1].y, i+COORD_OFFSET, exageration);
                i+=FLOATSPERVERTEX;
                setCoordinate(elevations[current+numInRow].x, elevations[current+numInRow].z, elevations[current+numInRow].y, i+COORD_OFFSET, exageration);
                i+=FLOATSPERVERTEX;
            }else{
                setCoordinate(elevations[current].x, elevations[current].z,  elevations[current].y, i + COORD_OFFSET, exageration);
                
                //setCoordinate(elevations[current+1].x, elevations[current+1].z, elevations[current+1].y, i+COORD_OFFSET, exageration);
                i+=FLOATSPERVERTEX;
                setCoordinate(elevations[current+numInRow].x, elevations[current+numInRow].z, elevations[current+numInRow].y, i+COORD_OFFSET, exageration);
                i+=FLOATSPERVERTEX;
            }
        }
        // create a stripCount array showing the number of vertices in each strip.
        /*int[] stripCounts = new int[vertexData.length / FLOATSPERVERTEX / VERTICES];
        for(int strip = 0; strip < stripCounts.length; strip++){
            stripCounts[strip] = VERTICES;
        }*/
        int[] stripCounts = {vertexData.length/FLOATSPERVERTEX};
        // Create and set the geometry
        /*tstrip = new InterleavedTriangleStripArray(vertexData.length/FLOATSPERVERTEX, GeometryArray.TEXTURE_COORDINATE_2|GeometryArray.COORDINATES|GeometryArray.NORMALS|GeometryArray.BY_REFERENCE|GeometryArray.INTERLEAVED,stripCounts);
        tstrip.setInterleavedVertices(vertexData);
        tstrip.generateNormals(true);
        tstrip.setCapability(InterleavedTriangleStripArray.ALLOW_INTERSECT);
        setGeometry(tstrip);*/
        //GeometryArray shape = new TriangleArray(elevations.length - (elevations.length % 3), GeometryArray.COORDINATES);
        //GeometryArray shape = new PointArray(vertexData.length/FLOATSPERVERTEX, GeometryArray.TEXTURE_COORDINATE_2|GeometryArray.COORDINATES|GeometryArray.NORMALS|GeometryArray.BY_REFERENCE|GeometryArray.INTERLEAVED);
        tstrip = new InterleavedTriangleStripArray(vertexData.length/FLOATSPERVERTEX, GeometryArray.TEXTURE_COORDINATE_2|GeometryArray.COORDINATES|GeometryArray.NORMALS|GeometryArray.BY_REFERENCE|GeometryArray.INTERLEAVED,stripCounts);
        tstrip.setInterleavedVertices(vertexData);
        tstrip.generateNormals(true);
        tstrip.setCapability(InterleavedTriangleStripArray.ALLOW_INTERSECT);
        setGeometry(tstrip);
        //shape.setInterleavedVertices(vertexData);
        /*Point3f[] temp = new Point3f[elevations.length - (elevations.length % 3)];
        for(int i = 0; i < temp.length; i++){
            temp[i] = elevations[i];
        }
        shape.setCoordinates(0, temp);*/
        System.out.println("ElevationSegment: number of vertices: " + vertexData.length/FLOATSPERVERTEX);
        
        //setGeometry(shape);
        System.gc();
        
    }
    /**
     * Constructor is reponsible for setting up the appearance/material,
     * computing the triangle strip values (colors, normals, coordinates)
     * stored in vertexData, then creating the actual JAVA 3D geometry.
     * @param modelWidth the width of the ElevationModel that this segment belongs to.
     * @param modelLength The Length of the ElevationModel to which this segment belongs.
     * @param elevations two dimensional array of terrain elevation data in meters
     * @param startRow first row of data to use from elevations
     * @param startColumn first column of data to use from elevations
     * @param stopRow last row of data to use from elevations
     * @param stopColumn last column of data to use from elevations
     * @param minEl minimum elevation in elevations
     * @param maxEl maximum elevation in elevations
     * @param exageration amount to exagerate(multiply) elevations by
     * @param lowX starting x coordinate in meters
     * @param highX stopping x coordinate in meters
     * @param lowZ starting z coordinate in meters
     * @param highZ stopping z coordinate in meters
     * @param resolution number of rows/columns of elevation data to skip when mapping. A resolution
     *  of 1 implies mapping every elevation point, a resolution of 10 implies mapping every 10th point.
     */
    public ElevationSegment( float elevations[][],   int startRow, int startColumn, int stopRow, int stopColumn,             float exageration, float lowX, float highX, float lowZ, float highZ, int resolution, float modelWidth, float modelLength) {
        setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        setupAppearance();
        
        // process the 2D elevation array
        dRows = (int)Math.ceil((stopRow-startRow+1)/(double)resolution);
        dColumns = (int)Math.ceil((stopColumn-startColumn+1)/(double)resolution);
        xStart = lowX;
        zStart = lowZ;
        deltaX = (highX-lowX)/(stopColumn-startColumn);
        deltaZ = (highZ-lowZ)/(stopRow-startRow);
        
        
        // first create an interleaved array of colors,  normals, and points
        vertexData = new float[FLOATSPERVERTEX*(dRows)*2*(dColumns-1)];
        if(vertexData == null) {
            System.out.println("Elevation segment: memory allocation failure");
            return;
        }
        // populate vertexData a strip at a time
        int row, col; // used as indices into the elevations array
        int i; // used as an index into vertexData
        double lenUnit;
        double widUnit;
        
        float startX, startY, curX, curY;
        
        lenUnit = 1.0 / (elevations[0].length / resolution );// numSegments);
        widUnit = 1.0 / (elevations.length / resolution );// numSegments);
        curX = (float) -(lowX / modelLength);  // needs to be negative because otherwise it builds the texture backwards for some reason.
        curY = (float) (lowZ / modelWidth);
        startY = (float) (lowZ / modelWidth);
        
        
        for( col = startColumn, i = 0; col <= stopColumn-resolution; col += resolution) {
            for(row = startRow; row <= stopRow; row += resolution) {
                if(row+resolution > stopRow) // always use last data line to prevent seams
                    row = stopRow;
                
                //setColor(i+COLOR_OFFSET,elevations[row][col],minElevation,maxElevation);
                //setColor(i+COLOR_OFFSET);
                setCoordinate(elevations, i+COORD_OFFSET,row,col,startRow,startColumn,exageration);
                setTextureCoordinates(i+TEXT_OFFSET, curX, curY);
                i += FLOATSPERVERTEX;
                
                int c = col;
                if(c+resolution > stopColumn-resolution) // always use last data line to prevent seams
                    c = stopColumn-resolution;
                //setColor(i+COLOR_OFFSET,elevations[row][c+resolution],minElevation,maxElevation);
                //setColor(i+COLOR_OFFSET);
                //try{
                setCoordinate(elevations, i+COORD_OFFSET,row,c+resolution,startRow,startColumn,exageration);
                //}catch(ArrayIndexOutOfBoundsException e){
                //    setCoordinate(elevations, i+COORD_OFFSET, row, c, startRow, startColumn, exageration);
                //}
                setTextureCoordinates(i+TEXT_OFFSET,curX+lenUnit, curY);
                i += FLOATSPERVERTEX;
                curY += widUnit;
            }
            curX += lenUnit;
            
            curY = startY;
        }
        
        // create a stripCount array showing the number of vertices in each strip.
        int[] stripCounts = new int[dColumns-1];
        for(int strip = 0; strip < dColumns-1; strip++){
            stripCounts[strip] = (dRows)*2;
        }
        // Create and set the geometry
        /*tstrip = new InterleavedTriangleStripArray(vertexData.length/FLOATSPERVERTEX,
                GeometryArray.TEXTURE_COORDINATE_2|GeometryArray.COORDINATES|GeometryArray.COLOR_3|GeometryArray.NORMALS|GeometryArray.BY_REFERENCE|GeometryArray.INTERLEAVED,
                stripCounts);*/
        tstrip = new InterleavedTriangleStripArray(vertexData.length/FLOATSPERVERTEX, GeometryArray.TEXTURE_COORDINATE_2|GeometryArray.COORDINATES|GeometryArray.NORMALS|GeometryArray.BY_REFERENCE|GeometryArray.INTERLEAVED,stripCounts);
        tstrip.setInterleavedVertices(vertexData);
        tstrip.generateNormals(true);
        tstrip.setCapability(InterleavedTriangleStripArray.ALLOW_INTERSECT);
        setGeometry(tstrip);
        System.gc();
        
    }
    /**
     *  setup the material properties and coloring attributes
     */
    protected void setupAppearance() {
        app = new Appearance(); // create an appeance
        app.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        mat = new Material(); // create a material
        mat.setCapability(Material.ALLOW_COMPONENT_WRITE);
        matColor = new Color3f(.9f,.7f,.2f);
        //ColoringAttributes ca = new ColoringAttributes(matColor,ColoringAttributes.SHADE_GOURAUD); // selecting shading
        //app.setColoringAttributes(ca); // add coloring attributes to the appearance
        
        mat.setLightingEnable(true); // allow lighting
        mat.setDiffuseColor(matColor); // set diffuse color (used by directional lights)
        mat.setAmbientColor(matColor); // set ambient color (used by ambient lights)
        mat.setSpecularColor(0f,0f,0f); // no specular color
        mat.setShininess(1.0f); // no shininess
        
        texatt = new TextureAttributes(TextureAttributes.MODULATE, new Transform3D(), new Color4f(1f,1f,1f, 1f),  TextureAttributes.NICEST);
        texatt.setCapability(TextureAttributes.ALLOW_MODE_WRITE);
        app.setTextureAttributes(texatt);
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        
        app.setMaterial(mat); // add the material to the appearance
        setAppearance(app); // add the appearance to the object
        setCapability(Shape3D.ALLOW_GEOMETRY_WRITE|Shape3D.ALLOW_GEOMETRY_READ); // allows calls to setgeometry
        setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    }
    /**
     * store coordinate data into vertex data array
     * @param startColumn first column used in elevations
     *
     * @param elevations array of elevations
     * @param i index into vertexData to store coordinate
     * @param row elevation row
     * @param col elevation column
     * @param startRow first row used in elevations
     * @param exageration elevation exageration factor
     */
    public void setCoordinate(float[][] elevations, int i, int row, int col, int startRow, int startColumn,float exageration) {
        try{vertexData[i] = (float)(((col-startColumn)*deltaX)+xStart);
        vertexData[i+1] = elevations[row][col]*exageration;
        vertexData[i+2] = (float)(zStart+((row-startRow)*deltaZ));
        }catch(ArrayIndexOutOfBoundsException e){
            
        }
    }
    
    public void setCoordinate(float x, float y, float z, int i, float exageration){
        try{
            vertexData[i] = x;
            vertexData[i+1] = y * exageration;
            vertexData[i+2] = z;
        }catch(ArrayIndexOutOfBoundsException e){}
    }
    /**
     * store color data into vertex data array, compute color based
     * on the elevation's distance between min and max elevations
     *
     *  @param i index into vertexData to store coordinate
     *  @param elevation  vertex elevation (no exageration)
     *  @param minElevation minimum elevation in model
     *  @param maxElevation maximum elevation in model
     */
    
    public void setColor(int i, int elevation,int minElevation, int maxElevation) {
        float ratio = ((float)elevation)/((float)maxElevation-minElevation);
        
        vertexData[i] = matColor.x*ratio; // set red
        vertexData[i+1] = matColor.y*ratio; // set green
        vertexData[i+2] = (float)(1-ratio);
        //vertexData[i+2] = matColor.z*ratio;// trick to bring blue for the lowest elevations
    }
    
    /**
     * store color data into vertex data array.  This is a flat color, as opposed to the other
     * setColor method which changes color based on elevation.
     * @param i The index into the vertexData array.
     */
    public void setColor(int i){
        vertexData[i] = matColor.x;
        vertexData[i+1] = matColor.y;
        vertexData[i+2] = matColor.z;
    }
    
    public void setColor(Color color){
        matColor.set(color);
        mat.setAmbientColor(matColor);
        mat.setDiffuseColor(matColor);
        app.setMaterial(mat);
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
    public void stitchRight(ElevationSegment other) {
        //
        // average the normals of the first left side column of the other segment
        // to the right side of the last column of this segment
        //
        int j = 0;    // j starts  on left side of first column
        int i = FLOATSPERVERTEX*2*dRows*(dColumns-2)+FLOATSPERVERTEX; // right side of last column
        for(int row = 0; row < dRows; row++) {
            //if(vertexData[i+COORD_OFFSET] == other.vertexData[j+COORD_OFFSET] &&
            //      vertexData[i+COORD_OFFSET+1] == other.vertexData[j+COORD_OFFSET+1] &&
            //    vertexData[i+COORD_OFFSET+2] == other.vertexData[j+COORD_OFFSET+2]) {
            vertexData[i+NORMAL_OFFSET] = (vertexData[i+NORMAL_OFFSET]+other.vertexData[j+NORMAL_OFFSET])/2.0f;
            vertexData[i+NORMAL_OFFSET+1] = (vertexData[i+NORMAL_OFFSET+1]+other.vertexData[j+NORMAL_OFFSET+1])/2.0f;
            vertexData[i+NORMAL_OFFSET+2] = (vertexData[i+NORMAL_OFFSET+2]+other.vertexData[j+NORMAL_OFFSET+2])/2.0f;
            tstrip.normalize(vertexData,i);
            other.vertexData[j+NORMAL_OFFSET] = vertexData[i+NORMAL_OFFSET];
            other.vertexData[j+NORMAL_OFFSET+1] = vertexData[i+NORMAL_OFFSET+1];
            other.vertexData[j+NORMAL_OFFSET+2] = vertexData[i+NORMAL_OFFSET+2];
            //}
            i+= FLOATSPERVERTEX*2; // increment to next row
            j+= FLOATSPERVERTEX*2; // increment to next row
        } // end for rows
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
    public void stitchTop(ElevationSegment other) {
        int i = FLOATSPERVERTEX*2*(dRows-1);    // i starts  on right side top of first strip
        int j = 0; // right side bottom of first strip
        for(int strip = 0; strip < dColumns-1; strip++) {
            // do right side of strip
            //if(vertexData[i+COORD_OFFSET] == other.vertexData[j+COORD_OFFSET] &&
            //      vertexData[i+COORD_OFFSET+1] == other.vertexData[j+COORD_OFFSET+1] &&
            //    vertexData[i+COORD_OFFSET+2] == other.vertexData[j+COORD_OFFSET+2]) {
            vertexData[i+NORMAL_OFFSET] = (vertexData[i+NORMAL_OFFSET]+other.vertexData[j+NORMAL_OFFSET])/2.0f;
            vertexData[i+NORMAL_OFFSET+1] = (vertexData[i+NORMAL_OFFSET+1]+other.vertexData[j+NORMAL_OFFSET+1])/2.0f;
            vertexData[i+NORMAL_OFFSET+2] = (vertexData[i+NORMAL_OFFSET+2]+other.vertexData[j+NORMAL_OFFSET+2])/2.0f;
            tstrip.normalize(vertexData,i);
            other.vertexData[j+NORMAL_OFFSET] = vertexData[i+NORMAL_OFFSET];
            other.vertexData[j+NORMAL_OFFSET+1] = vertexData[i+NORMAL_OFFSET+1];
            other.vertexData[j+NORMAL_OFFSET+2] = vertexData[i+NORMAL_OFFSET+2];
            //}
            
            // do left side of strip
            //if(vertexData[i+COORD_OFFSET+FLOATSPERVERTEX] == other.vertexData[j+COORD_OFFSET+FLOATSPERVERTEX] &&
            //      vertexData[i+COORD_OFFSET+1+FLOATSPERVERTEX] == other.vertexData[j+COORD_OFFSET+1+FLOATSPERVERTEX] &&
            //    vertexData[i+COORD_OFFSET+2+FLOATSPERVERTEX] == other.vertexData[j+COORD_OFFSET+2+FLOATSPERVERTEX]) {
            vertexData[i+NORMAL_OFFSET+FLOATSPERVERTEX] = (vertexData[i+NORMAL_OFFSET+FLOATSPERVERTEX]+other.vertexData[j+NORMAL_OFFSET+FLOATSPERVERTEX])/2.0f;
            vertexData[i+NORMAL_OFFSET+1+FLOATSPERVERTEX] = (vertexData[i+NORMAL_OFFSET+1+FLOATSPERVERTEX]+other.vertexData[j+NORMAL_OFFSET+1+FLOATSPERVERTEX])/2.0f;
            vertexData[i+NORMAL_OFFSET+2+FLOATSPERVERTEX] = (vertexData[i+NORMAL_OFFSET+2+FLOATSPERVERTEX]+other.vertexData[j+NORMAL_OFFSET+2+FLOATSPERVERTEX])/2.0f;
            tstrip.normalize(vertexData,i+FLOATSPERVERTEX);
            other.vertexData[j+NORMAL_OFFSET+FLOATSPERVERTEX] = vertexData[i+NORMAL_OFFSET+FLOATSPERVERTEX];
            other.vertexData[j+NORMAL_OFFSET+1+FLOATSPERVERTEX] = vertexData[i+NORMAL_OFFSET+1+FLOATSPERVERTEX];
            other.vertexData[j+NORMAL_OFFSET+2+FLOATSPERVERTEX] = vertexData[i+NORMAL_OFFSET+2+FLOATSPERVERTEX];
            //}
            
            i+= FLOATSPERVERTEX*2*dRows; // increment to next strip
            j+= FLOATSPERVERTEX*2*dRows; // increment to next strip
        } // end for rows
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
        
        
        int col= (int)Math.abs((x-xStart)/deltaX);
        int row = (int)Math.abs((z-zStart)/deltaZ);
        
        row = Math.min(row,dRows-1);
        col = Math.min(col, dColumns-2);
        row = Math.max(row,0);
        col = Math.max(col, 0);
        
        int i = (FLOATSPERVERTEX*2*dRows)*col + (FLOATSPERVERTEX*2)*row;
        return vertexData[i+COORD_OFFSET+1];
    }
    
    /**
     * Method to get at the triangle strip's change exageration method.
     * @param oldAmount The old value of the exageration.  Used to reset to natural elevation.
     * @param newAmount The new exageration to change to.
     */
    
    public void changeExageration(float oldAmount, float newAmount){
        tstrip.changeExageration(oldAmount, newAmount);
    }
    
    /**
     * Stores texture coordinates into vertex data array.
     * @param i index into vertexData to store coordinate
     * @param x x component of the texture coordinates
     * @param y y component of the texture coordinates
     */
    protected void setTextureCoordinates(int i, double x, double y){
        vertexData[i] = (float)x;
        vertexData[i+1] = (float)y;
    }
    
    /**
     * Change the texture currently being used.
     * @param texture The new Texture2D to use.
     */
    public void setTexture(Texture2D texture){
        getAppearance().setTexture(texture);
    }
    
    /**
     * Recalculates the normal values so that the lighting effects change when the elevation exageration change.
     */
    public void recalcNormals(){
        tstrip.generateNormals(true);
    }
    
    public void setTextureMode(int mode){
        texatt.setTextureMode(mode);
    }
}
