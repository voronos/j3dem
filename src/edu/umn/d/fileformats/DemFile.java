package edu.umn.d.fileformats;
import edu.umn.d.geometry.GroundCoordinates;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
/**
 *  This class is a specialization of the {@link ElevationFile} class created
 * specifically to load DEM format data from the USGS archives.
 * @author Mark Pendergast
 * @version 1.0 February 2003
 * @see ElevationFile
 */
public class DemFile extends ElevationFile {
    
    /**
     * Length of the A record of a DEM file.
     */
    public static final int ARECORD_LENGTH = 1024;
    /**
     * Name of the quadrangle the dem file describes.
     */
    public static final int QUADRANGLE_NAME_LENGTH = 144;
    /**
     * Minimum number of A record tokens.
     */
    public static final int MIN_ARECORD_TOKENS = 39;
    
    /**
     *  Create DemFile object from data contained in specified file
     * @param aFileName name of the DEM file to load. File name should be a
     *  fully qualified file name.
     * @throws java.lang.IllegalArgumentException Thrown if the file does not meet standards for a dem file.
     */
    public DemFile(URL file) throws IllegalArgumentException {
        super(file);
        try{
            char[] Arecord = new char[ARECORD_LENGTH];
            URLConnection con = file.openConnection();
            InputStreamReader insread = new InputStreamReader(con.getInputStream());
            BufferedReader bReader = new BufferedReader(insread);

            // read and parse out A Record
            if(bReader.read(Arecord,0,ARECORD_LENGTH) == -1) {
                bReader.close();
                System.out.println("Invalid file format (bad arecord) : "+fileName);
                throw(new IllegalArgumentException("Invalid file format : "+ fileName));
            }
            
            quadrangleName = new String(Arecord, 0, QUADRANGLE_NAME_LENGTH);
            quadrangleName = quadrangleName.trim();
            
            minElevation = (int)parseDemDouble(new String(Arecord,738,24));
            maxElevation = (int)parseDemDouble(new String(Arecord,762,24));
            groundCoordinates.sw[GroundCoordinates.LONGITUDE] = (parseDemDouble(new String(Arecord,546,24)));
            groundCoordinates.sw[GroundCoordinates.LATITUDE] = (parseDemDouble(new String(Arecord,570,24)));
            groundCoordinates.nw[GroundCoordinates.LONGITUDE] = (parseDemDouble(new String(Arecord,594,24)));
            groundCoordinates.nw[GroundCoordinates.LATITUDE] = (parseDemDouble(new String(Arecord,618,24)));
            groundCoordinates.ne[GroundCoordinates.LONGITUDE] = (parseDemDouble(new String(Arecord,642,24)));
            groundCoordinates.ne[GroundCoordinates.LATITUDE] = (parseDemDouble(new String(Arecord,666,24)));
            groundCoordinates.se[GroundCoordinates.LONGITUDE] = (parseDemDouble(new String(Arecord,690,24)));
            groundCoordinates.se[GroundCoordinates.LATITUDE] = (parseDemDouble(new String(Arecord,714,24)));
            System.out.println(groundCoordinates);
            nColumns = (int)parseDemDouble(new String(Arecord,858,6));
            
            //  Use a streamtokenizer to parse B Records, one record for each column
            //  set the streamtokenizer to use a space a delimiter and convert all
            //  tokens to strings
            StreamTokenizer st = new StreamTokenizer(bReader); // stream already positioned to start of B Record
            st.resetSyntax();
            st.whitespaceChars(' ',' ');
            st.wordChars(' '+1,'z');
            for(int column = 0; column < nColumns; column++) {
                
                int ttype;
                double rowCoordinateLat, rowCoordinateLong;
                
                st.nextToken(); // skip row id
                st.nextToken(); // skip column id
                ttype = st.nextToken(); // number of rows
                nRows = (int)parseDemInt(st.sval);
                if(elevations == null){ // allocate array if necessary
                    nRows = (int)parseDemInt(st.sval);
                    elevations = new float[nRows][nColumns];
                }
                
                for(int i=0; i < 6; i++)  // skip 6 fields
                    st.nextToken();
                
                for(int row = 0; row<nRows; row++) // read in elevation data
                {
                    st.nextToken();
                    elevations[row][column]=  parseDemInt(st.sval);
                }
                
            }
            bReader.close();
            
        } // end try
        catch(IOException e){
            System.out.println("IE Exception when loading from [" + fileName + "] error: " + e.getMessage());
            throw new IllegalArgumentException("File I/O failure : "+fileName);
        } catch(NumberFormatException e){
            System.out.println("NumberFormat Exception when loading from [" + fileName + "] ");
            throw new IllegalArgumentException("Invalid file format : "+fileName);
        }
        
        System.gc();  // clean out memory
        
    }
    /**
     * This method parses a double from a string.  Note, DEM data uses
     * the old fortan notation for storing doubles using a 'D' intead of an
     * 'E'.
     * @param in  string to parse
     * @return double value from string
     * @exception NumberFormatException thrown when string is not a valid double
     */
    public double parseDemDouble(String in) throws NumberFormatException {
        String st = in.replace('D','E');  // convert fortran format to modern
        return Double.parseDouble(st.trim());
    }
    
    /**
     * This method parses an integer from a string.  Note, DEM data uses
     * the old fortan notation for storing doubles using a 'D' intead of an
     * 'E'.
     * @param in  string to parse
     * @return double value from string
     * @exception NumberFormatException thrown when string is not a valid double
     */
    public int parseDemInt(String in) throws NumberFormatException {
        String st = in.replace('D','E');  // convert fortran format to modern
        return Integer.parseInt(st.trim());
    }
    
    
}