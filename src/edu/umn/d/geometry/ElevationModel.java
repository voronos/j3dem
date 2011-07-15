package edu.umn.d.geometry;
import edu.umn.d.behaviors.MouseWheelBehavior;
import edu.umn.d.fileformats.DDFFile;
import edu.umn.d.fileformats.DemFile;
import edu.umn.d.fileformats.ElevationFile;
import edu.umn.d.fileformats.GZDDFFile;
import edu.umn.d.fileformats.XYZFile;
import edu.umn.d.windows.ControlWindow;
import edu.umn.d.windows.StatusWindow;
import edu.umn.d.windows.TextureButtonActionListener;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Vector;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Texture2D;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.vecmath.Point3d;


/**
 * ElevationModel is a specialization of a {@link BranchGroup} that holds Java3D
 * geometry objects representing a terrain map. The map is divided  into segments
 * each segment contains a Level-of-Detail switch  containing the geometry
 * for the segment in three levels of detail. This allows for faster drawing and
 * navigation of the model as a whole.
 * @author Mark Pendergast
 * @version 1.0 February 2003
 * @see LODSegment
 */

public class ElevationModel extends BranchGroup implements ElevationModelInterface {
    
    private JRadioButton[] textureButtons;
    
    private Texture2D tex;
    /**
     * {@link GroundCoordinates} for the model, contains the lat/long of the four corners in arc seconds
     */
    public GroundCoordinates groundCoordinates;
    
    /** minimum X coordinate */
    public float west_X = 0;
    /** maximum X coordinate */
    public float east_X = 0;
    /** maximum Z  coordinate, note, positive Z axis is in the south due to Java3D */
    public  float south_Z = 0;
    /** minimum Z coordinate */
    public  float north_Z = 0;
    /** number of seconds high/wide of each segment is */
    public final static int SECONDS_PER_SEGMENT = 600;//600
    /** space in meters between each row of elevation data */
    protected float deltaRow;
    /** space in meters between each column of elevation data */
    protected float deltaCol;
    /**
     * Factor each elevation reading is multiplied by when drawing the model. Makes elevation differences stand out
     */
    public float exageration = ControlWindow.getExageration();
    /** minimum elevation in meters */
    public float minElevation ;
    /** maximum elevation in meters */
    public float maxElevation;
    /** geographic name or title of the data */
    public  String name;
    /** two dimensional array of segments comprising the model data */
    protected LODSegment[][] segments;
    /**
     * The ElevationFile that holds the data.
     */
    protected ElevationFile file;
    private int sRows;
    private int sColumns;
    /**
     * Empty constructor.
     */
    protected ElevationModel(){
    }
    
    /**
     * Sets up and compiles the ElevationModel.
     * @param aFileName full name of the file to use.
     * @param stat Reference to the status window to display loading status.
     * @throws java.io.IOException Thrown if the file does not exist.
     */
    
    public ElevationModel(URL fileURL,StatusWindow stat) throws IOException{
        String aFileName = fileURL.toString();
        //int sRows, sColumns;
        float length, width;
        setCapability(BranchGroup.ALLOW_DETACH);  // allow this model to be removed from the Universe
        
        // for now, just use one file and its ground coordinates
        if(stat != null)
            stat.setLabel2("Loading DEM data ");
        
        if(aFileName.contains(".tar.gz") || aFileName.contains(".TAR.GZ")){
            file = new GZDDFFile(fileURL);
        } else if(aFileName.contains(".ddf") || aFileName.contains(".DDF")){
            file = new DDFFile(fileURL);
        } else if(aFileName.contains(".dem") || aFileName.contains(".DEM")){
            file = new DemFile(fileURL);
        } else if(aFileName.contains(".xyz") || aFileName.contains(".XYZ")){
            file = new XYZFile(fileURL);
        }else{
            System.out.println("Sorry, wrong file type.");
            System.exit(0);
        }
        //System.out.println("ElevationModel: file.filePath is " + file.filePath);
        findImages(file.filePath);
        groundCoordinates = file.groundCoordinates;
        minElevation = file.minElevation;
        maxElevation = file.maxElevation;
        name = file.quadrangleName;
        
        if(file instanceof XYZFile){
            west_X = (float)(groundCoordinates.nw[GroundCoordinates.LONGITUDE]);// * GroundCoordinates.METERS_PER_NAUTICAL_SECOND);
            east_X = (float)(groundCoordinates.ne[GroundCoordinates.LONGITUDE]);// * GroundCoordinates.METERS_PER_NAUTICAL_SECOND);
            south_Z = (float)(groundCoordinates.sw[GroundCoordinates.LATITUDE]);// * GroundCoordinates.METERS_PER_NAUTICAL_SECOND);
            north_Z = (float)(groundCoordinates.nw[GroundCoordinates.LATITUDE]);// * GroundCoordinates.METERS_PER_NAUTICAL_SECOND);
            length = groundCoordinates.lengthSeconds();
        width = groundCoordinates.widthSeconds();
        } else{
            west_X = -(float)(groundCoordinates.nw[GroundCoordinates.LONGITUDE] * GroundCoordinates.METERS_PER_NAUTICAL_SECOND);
            east_X = -(float)(groundCoordinates.ne[GroundCoordinates.LONGITUDE] * GroundCoordinates.METERS_PER_NAUTICAL_SECOND);
            south_Z = (float)(groundCoordinates.sw[GroundCoordinates.LATITUDE] * GroundCoordinates.METERS_PER_NAUTICAL_SECOND);
            north_Z = (float)(groundCoordinates.nw[GroundCoordinates.LATITUDE] * GroundCoordinates.METERS_PER_NAUTICAL_SECOND);
            length = groundCoordinates.lengthMeters();
        width = groundCoordinates.widthMeters();
        }
        //   create LODSegments
        createLODSegments(stat);
        
        MouseWheelBehavior mb = new MouseWheelBehavior(this);
        mb.setSchedulingBounds(new BoundingSphere(new Point3d((west_X + east_X) / 2, (minElevation + maxElevation) / 2, (south_Z + north_Z) / 2), Double.MAX_VALUE));
        addChild(mb);
        if(stat != null)
            stat.setLabel2("Compiling/Optimizing the geometry");
        compile(); // compile the model
    }
    /**
     * Initializes the {@link LODSegment} array to a number of columns and rows based on the {@link GroundCoordinates}
     * length and width in seconds and the SECONDS_PER_SEGMENT.
     * Also stitches together the top and right sides to create a smooth mesh.
     */
    protected void createLODSegments(StatusWindow stat){
        sColumns = (int)Math.ceil(groundCoordinates.lengthSeconds()/SECONDS_PER_SEGMENT);
        sRows = (int)Math.ceil(groundCoordinates.widthSeconds()/SECONDS_PER_SEGMENT);
        segments = new LODSegment[sRows][sColumns];
        
        GroundCoordinates gc = new GroundCoordinates();
        
        int rowRatio = (int) (1.0d*file.nRows/sRows);
        int colRatio = (int) (1.0d*file.nColumns/sColumns);
        
        deltaRow = (north_Z-south_Z)/sRows;
        deltaCol = (east_X - west_X)/sColumns;
        if(file instanceof XYZFile){
            //segments[row][col] = new LODSegment(((XYZFile)file).elevationRows, startRow, stopRow,  minElevation, maxElevation, exageration, minX, maxX, minZ, maxZ, getModelWidth(), getModelLength());
            //addChild(segments[row][col]);
            float minX, maxX, minZ, maxZ;
            minX = (float)file.groundCoordinates.sw[GroundCoordinates.LONGITUDE];
            maxX = (float)file.groundCoordinates.ne[GroundCoordinates.LONGITUDE];
            minZ = (float)file.groundCoordinates.se[GroundCoordinates.LATITUDE];
            maxZ = (float)file.groundCoordinates.ne[GroundCoordinates.LATITUDE];
            addChild(new ElevationSegment(((XYZFile)file).elevationPoints, 0, file.nRows, 0, file.nColumns, minElevation, maxElevation,  exageration, minX, maxX, minZ, maxZ, 1, getModelWidth(), getModelLength()));
            return;
        }
        //System.out.println("deltaRow is " + deltaRow + "\ndeltaColumn is " + deltaCol);
        for(int row = 0; row < sRows; row++) {
            float minX, maxX, minZ, maxZ;
            int startRow, stopRow, startCol, stopCol;
            gc.sw[GroundCoordinates.LATITUDE] = groundCoordinates.sw[GroundCoordinates.LATITUDE] + row*SECONDS_PER_SEGMENT;
            gc.se[GroundCoordinates.LATITUDE] = groundCoordinates.sw[GroundCoordinates.LATITUDE]+ row*SECONDS_PER_SEGMENT;
            gc.nw[GroundCoordinates.LATITUDE] = groundCoordinates.sw[GroundCoordinates.LATITUDE] + (row+1)*SECONDS_PER_SEGMENT;
            gc.ne[GroundCoordinates.LATITUDE] = groundCoordinates.sw[GroundCoordinates.LATITUDE]+ (row+1)*SECONDS_PER_SEGMENT;
            minZ = south_Z + row*deltaRow;
            maxZ = south_Z +(row+1.0f)*deltaRow;
            startRow = row*(rowRatio);
            stopRow = (row+1)*(rowRatio);
            for(int col = 0 ; col < sColumns; col++) {
                if(stat != null)
                    stat.setLabel2("Creating geometry segment ",row*sColumns+col+1,sRows*sColumns);
                minX = west_X + col*deltaCol;
                maxX = west_X +(col+1.0f)*deltaCol;
                startCol = col*(colRatio);
                stopCol = (col+1)*(colRatio);
                gc.sw[GroundCoordinates.LONGITUDE] = groundCoordinates.sw[GroundCoordinates.LONGITUDE] - col*SECONDS_PER_SEGMENT;
                gc.nw[GroundCoordinates.LONGITUDE] = groundCoordinates.sw[GroundCoordinates.LONGITUDE]- col*SECONDS_PER_SEGMENT;
                gc.se[GroundCoordinates.LONGITUDE] = groundCoordinates.sw[GroundCoordinates.LONGITUDE] - (col+1)*SECONDS_PER_SEGMENT;
                gc.ne[GroundCoordinates.LONGITUDE] = groundCoordinates.sw[GroundCoordinates.LONGITUDE]- (col+1)*SECONDS_PER_SEGMENT;
                
                segments[row][col] = new LODSegment(file.elevations,  startRow,startCol, stopRow,stopCol,
                        minElevation, maxElevation, exageration,
                        minX, maxX, minZ, maxZ, getModelWidth(), getModelLength());
                addChild(segments[row][col]);
                
            }
        }
        stitchSegments();
    }
    
    private void stitchSegments(){
        //  Adjust normals on borders of each segment to remove variations
        //  Stitch right to left first
        for(int row = 0; row < sRows; row++) {
            for(int col = 0 ; col < sColumns; col++) {
                if(col < sColumns-1) // only if not last column
                    segments[row][col].stitchRight(segments[row][col+1]);
            }
        }
        //Now stitch tops to bottoms, note this must be done in a separate
        // loop so that the corners come out ok.
        for(int row = 0; row < sRows; row++) {
            for(int col = 0 ; col < sColumns; col++) {
                if(row < sRows-1) // only if not last row
                    segments[row][col].stitchTop(segments[row+1][col]);
            }
        }
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
        
        int row = (int)Math.abs((z-south_Z)/deltaRow);
        int col = (int)Math.abs((x-west_X)/deltaCol);
        
        row = Math.min(row,segments.length-1);
        col = Math.min(col, segments[0].length-1);
        row = Math.max(row,0);
        col = Math.max(col, 0);
        return segments[row][col].getElevationAt(x,z);
    }
    /**
     * retrieve the model length, distance in meters from west to east
     *  @return the length in meters
     */
    public float getModelLength() {
        
        return groundCoordinates.lengthMeters();
    }
    /**
     * retrieve the model width, distance in meters from south to north
     *  @return the width in meters
     */
    public float getModelWidth() {
        
        return groundCoordinates.widthMeters();
    }
    /**
     * retrieve the model maximum elevation, adjusted by the elevation exageration
     *  @return the adjusted maximum elevation in meters
     */
    public float getMaxElevation() {
        
        return maxElevation*exageration;
    }
    /**
     * retrieve the model minimum elevation, adjusted by the elevation exageration
     *  @return the adjusted minimum elevation in meters
     */
    public float getMinElevation() {
        
        return minElevation*exageration;
    }
    /**
     * Change the texture of the model.
     * @param tex The {@link Texture2D} to change to.
     */
    public void setTexture(Texture2D tex){
        for(int col = 0; col < segments.length; col++){
            for(int row = 0; row < segments[col].length; row++){
                segments[col][row].setTexture(tex);
            }
        }
    }
    
    /**
     * Change the exageration of the model.
     * @param amount The amount to change the exageration to.
     */
    public void changeExageration(float amount){
        for(int col = 0; col < segments.length;col++){
            for(int row = 0; row < segments[col].length;row++){
                segments[col][row].changeExageration(exageration, amount);
            }
        }
        exageration = amount;
        stitchSegments();
    }
    
    /**
     * Returns an LODSegment located at [row][col]
     * @param col the column of the LODSegment
     * @param row the row of the LODSegment
     * @return An LODSegment located at [row][col]
     */
    public LODSegment getSegment(int col, int row){
        return segments[col][row];
    }
    
    /**
     * Retrieve the resolution of the dem.  Typically will be 24k for an SDTS transfer.
     * @return The resolution of the quad data.
     */
    public float getQuadResolution(){
        return file.getResolution();
    }
    
    /**
     * Recalculates the normal values so that the lighting effects change when the elevation exageration change.
     */
    public void recalcNormals(){
        for(int col = 0; col < segments.length; col++){
            for(int row = 0; row < segments[col].length; row++){
                segments[col][row].recalcNormals();
            }
        }
    }
    
    /**
     * Changes the color of the model.
     * @param color Desired color to change to.
     */
    public void setColor(Color color){
        for(int col = 0; col < segments.length; col++){
            for(int row = 0; row < segments[col].length; row++){
                segments[col][row].setColor(color);
            }
        }
    }
    
    public void setTextureMode(int mode){
        for(int col = 0; col < segments.length; col++){
            for(int row = 0; row < segments[col].length; row++){
                segments[col][row].setTextureMode(mode);
            }
        }
    }
    
    private void findImages(String directory){
        try{
            URL dir = new URL(directory);
            URLConnection con = dir.openConnection();
            InputStream stream = dir.openStream();
            Vector imageNames = new Vector();
            //System.out.println("ElevationModel: content type = " + con.getContentType());
            
            while(stream.available() > 0){
                String line = "";
                char c;
                // this do while loop seems to add the newline to the end of the file string
                // but each entry? is separated by a new line so this gives me something to work with.
                do{
                    c = (char)stream.read();
                    line += c;
                }while(c != '\n');
                
                if(con.getContentType().contains("html") && (line.contains(".gif\">") || line.contains(".jpg\">"))){
                    //System.out.println("ElevationModel: We have an image line.");
                    line = line.split(".gif\">")[1];
                    int end = line.indexOf('<');
                    System.out.println(line);
                    String image = line.substring(0, end);
                    //System.out.println("ElevationModel: Image file is " + image);
                    imageNames.add(directory + image);
                    //System.out.println("ElevationModel: just added '" + directory + image + "' to the Vector");
                }else if(con.getContentType().contains("plain") && (line.contains(".gif") || line.contains(".jpg"))){
                    imageNames.add(directory +  line);
                    //System.out.println("ElevationModel: just added '" + directory + line + "' to the vector");
                }
            }
            textureButtons = new JRadioButton[imageNames.size() + 1];
            textureButtons[0] = new JRadioButton("None");
            textureButtons[0].addActionListener(new TextureButtonActionListener(this));
            textureButtons[0].setSelected(true);
            ButtonGroup group = new ButtonGroup();
            group.add(textureButtons[0]);
            int i = 1;
            Iterator j = imageNames.iterator();
            while(j.hasNext()){
                String text = (String)j.next();
                textureButtons[i] = new JRadioButton(text.substring(text.lastIndexOf('/')+1));
                textureButtons[i].setActionCommand(text);
                textureButtons[i].addActionListener(new TextureButtonActionListener(this));
                group.add(textureButtons[i]);
                i++;
            }
        }catch(Exception e){
            System.err.println("Error finding images: ");
            e.printStackTrace();
        }
    }
    
    public JRadioButton[] getTextureButtons(){
        //System.out.println("getting texture buttons");
        return textureButtons;
    }
};