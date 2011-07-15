package edu.umn.d.fileformats;

import edu.umn.d.geometry.GroundCoordinates;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Provides basic methods to allow for tarballed DDF files or untarred DDF files.
 * @author nels2426
 */
public abstract class DDFFileAbstract extends ElevationFile{
    protected int numDirectoryElements = 0;
    protected int lengthOfFirstDirectoryElement = 0;
    protected int lengthOfNextDirectoryElement = 0;
    protected int lengthOfLastDirectoryElement = 0;
    protected int startingPointOfDDA = 0;
    protected String fileID;
    /** Creates a new instance of DDFFileAbstract */
    public DDFFileAbstract(URL filename) {
        super(filename);
        fileID = fileName.substring(0, 4);
    }
    /**
     * Reads a number of bytes from the {@link InputStream} and turns them into an integer.
     * @param reader The input stream to read from.
     * @param end The number of bytes to read
     * @return The integer parsed from the input stream.
     */
    private int readAndProcessInt(InputStream reader, int end) throws IOException{
        int n = (int)Math.pow(10, end - 1);
        int value = 0;
        for(int i = 0; i < end; i++, n /= 10){
            char j = (char)reader.read();
            if(Character.isDigit(j)){
                value += Character.digit(j,10) * n;
            }
            
        }
        return value;
    }
    
    
    /**
     * Reads and discards bytes from the {@link InputStream} until a unit terminator is encountered.
     * @param reader The InputStream to work with.
     * @return The number of bytes read.
     */
    private int discardUnit(InputStream reader) throws IOException{
        int n = 0;
        while(reader.available()>0){
            n++;
            if(reader.read() == 30){
                break;
            }
            
        }
        return n;
    }
    /**
     * Reads and discards bytes from the {@link InputStream} until a field terminator is encountered.
     * @param reader The {@link InputStream} to work with.
     * @return The number of bytes read.
     */
    private int discardField(InputStream reader) throws IOException{
        int n = 0;
        while(reader.available()>0){
            n++;
            if(reader.read() == 31){
                break;
            }
        }
        return n;
    }
    /**
     * Reads and discards bytes from the {@link InputStream} until field or unit terminator is encountered.
     * @param reader The {@link InputStream} to work with.
     * @return The number of bytes discarded.
     */
    private int discard(InputStream reader) throws IOException{
        int n = 0;
        while(reader.available()>0){
            int i = reader.read();
            n++;
            if(i == 31 || i == 30){
                break;
            }
        }
        return n;
    }
    
    /**
     * Parses the contents of a data record leader.  This may or may not be the record leader for the file.
     * Needed values are stored in the class variables, and the rest is discarded.  This should position the reader
     * at the first directory entry itself, which will usually be 0000;&ModuleName.
     * @param reader The {@link InputStream} to work with.
     * @return the number of bytes read.
     */
    protected int processLeader(InputStream reader)throws IOException, MalformedURLException{
        int bytesRead = 0;
        bytesRead += reader.skip(12);
        // compute the starting byte of the data descriptive area (DDA)
        startingPointOfDDA = readAndProcessInt(reader, 5);
        reader.skip(3);
        lengthOfNextDirectoryElement = readAndProcessInt(reader,1);
        lengthOfLastDirectoryElement = readAndProcessInt(reader,  1);
        reader.skip(1);
        lengthOfFirstDirectoryElement = readAndProcessInt(reader, 1);
        bytesRead += 12;
        
        try{
            numDirectoryElements = (startingPointOfDDA - 23) / (lengthOfFirstDirectoryElement + lengthOfNextDirectoryElement + lengthOfLastDirectoryElement);
        } catch(ArithmeticException e){
            e.printStackTrace();
        }
        
        // discard until the start of the last directory entry
        bytesRead += reader.skip((lengthOfFirstDirectoryElement + lengthOfNextDirectoryElement + lengthOfLastDirectoryElement) * (numDirectoryElements) + 1);
        //System.out.println();
        return bytesRead;
    }
    
    /**
     * Searches through the iden file of the ddf records to get the north and west latitude and longitude.
     * Values are stored such that 46 degrees, 45 minutes and 0 seconds becomes 467,500.0
     * Since the USGS SDTS DEMs are 7.5 minute DEMs, the south and east coordinates are found by subtracting 1250 from the
     * west and north coordinates
     * @return The number of bytes read.
     */
    protected int processGroundCoordinates(InputStream reader) throws IOException{
        int bytesRead = 0;
        bytesRead += processLeader(reader);
        bytesRead += discardUnit(reader);
        bytesRead += discardUnit(reader);
        bytesRead += discardField(reader);
        int[] pos = new int[2];
        int n = 1;
        int index = 0;
        byte[] b = new byte[5];
        do{
            bytesRead += reader.read(b, 0, 5);
            String s = new String(b);
            //System.out.println(s);
            n++;
            if(s.equals("TITL!")){
                pos[0] = n;
                //System.out.println("titl at " +pos[0]);
            } else if (s.equals("DAID!")){
                pos[1] = n;
                //System.out.println("daid at " + pos[1]);
            }
        }while(b[4] != 31);
        
        // discard fields until the first position
        for(int j = 0; j < pos[0]; j++){
            bytesRead += discardField(reader);
        }
        
        // get the title
        int[] tempBytes = new int[60];
        char[] tempChar = new char[60];
        int length = 0;
        while(reader.available() > 0){
            tempBytes[length] = reader.read();
            //System.out.println((char)tempBytes[length]);
            bytesRead++;
            if(tempBytes[length] == 31){
                break;
            }
            length++;
        }
        quadrangleName = new String(tempBytes, 0, length);
        
        // extract the resolution from the TITL!
        String r = quadrangleName.substring(quadrangleName.indexOf('-')+1);
        quadrangleName = quadrangleName.substring(0, quadrangleName.indexOf('-'));
        resolution = Float.parseFloat(r);
        
        int degrees;
        int minutes;
        int seconds;
        reader.skip(7);
        char negative = (char) reader.read();
        bytesRead += 8;
        if(negative == '-'){
            degrees = readAndProcessInt(reader, 2) * -10000;
            reader.skip(1);
            minutes = (readAndProcessInt(reader, 2) * -10000) / 60;
            reader.skip(1);
            seconds = (readAndProcessInt(reader, 2) * -10000) / 3600;
            bytesRead += 8;
        } else{
            degrees = readAndProcessInt(reader, 2) * 10000;
            reader.skip(1);
            minutes = (readAndProcessInt(reader, 2) * 10000) / 60;
            reader.skip(1);
            seconds = (readAndProcessInt(reader, 2) * 10000) / 3600;
            bytesRead += 8;
        }
        groundCoordinates.ne[GroundCoordinates.LATITUDE] = minutes + degrees + seconds;
        groundCoordinates.nw[GroundCoordinates.LATITUDE] = minutes + degrees + seconds;
        groundCoordinates.se[GroundCoordinates.LATITUDE] = minutes + degrees + seconds - 1250;
        groundCoordinates.sw[GroundCoordinates.LATITUDE] = minutes + degrees + seconds - 1250;
        reader.skip(16);
        negative = (char)reader.read();
        bytesRead += 17;
        if(negative == '-'){
            degrees = readAndProcessInt(reader, 2) * -10000;
            reader.skip(1);
            minutes = (readAndProcessInt(reader, 2) * -10000) / 60;
            reader.skip(1);
            seconds = (readAndProcessInt(reader, 2) * -10000) / 3600;
            bytesRead += 8;
        } else{
            degrees = readAndProcessInt(reader, 2) * 10000;
            reader.skip(1);
            minutes = (readAndProcessInt(reader, 2) * 10000) / 60;
            reader.skip(1);
            seconds = (readAndProcessInt(reader, 2) * 10000) / 3600;
            bytesRead += 8;
        }
        groundCoordinates.nw[GroundCoordinates.LONGITUDE] = minutes + degrees + seconds;
        groundCoordinates.sw[GroundCoordinates.LONGITUDE] = minutes + degrees + seconds;
        groundCoordinates.ne[GroundCoordinates.LONGITUDE] = minutes + degrees + seconds + 1250;
        groundCoordinates.se[GroundCoordinates.LONGITUDE] = minutes + degrees + seconds + 1250;
        
        //System.out.println(groundCoordinates);
        System.gc();
        return bytesRead;
    }
    /**
     * Searches through the ddom file of the ddf records to get the minimum and maximum elevation.
     * Takes advantage of the fact that the values are immediately preceded by the keywords MIN or MAX,
     * so we just look for those and then parse the set of integers that follows.
     * @return The number of bytes read.
     */
    protected int processMinMaxElevation(InputStream reader) throws IOException{
        int bytesRead = 0;
        boolean minFound = false;
        boolean minParsed = false;
        boolean maxFound = false;
        boolean maxParsed = false;
        while(!minParsed){
            int[] tempBytes = new int[60];
            int length = 0;
            
            while(reader.available() > 0){
                tempBytes[length] = reader.read();
                bytesRead++;
                if(tempBytes[length] == 30 || tempBytes[length] == 31){
                    if(minFound){
                        minElevation = Integer.parseInt(new String(tempBytes, 0, length));
                        //System.out.println("minElevation = " + minElevation);
                        minParsed = true;
                    }
                    
                    break;
                } else{
                    length++;
                }
            }
            String a = new String(tempBytes,0, length);
            if(a.equals("MIN")){
                minFound = true;
            }
            
        }
        bytesRead += discardUnit(reader);
        while(!maxParsed){
            int[] tempBytes = new int[60];
            int length = 0;
            
            while(reader.available() > 0){
                tempBytes[length] = reader.read();
                bytesRead++;
                if(tempBytes[length] == 30 || tempBytes[length] == 31){
                    if(maxFound){
                        maxElevation = Integer.parseInt(new String(tempBytes, 0, length));
                        //System.out.println("maxElevation = " + maxElevation);
                        maxParsed = true;
                    }
                    
                    break;
                } else{
                    length++;
                }
            }
            String a = new String(tempBytes,0, length);
            if(a.equals("MAX")){
                maxFound = true;
            }
            
        }
        return bytesRead;
    }
    
    protected int processElevations(InputStream reader)throws IOException{
        int bytesRead = 0;
        bytesRead += processLeader(reader);
        
        for(int i = 0; i < numDirectoryElements; i++){
            bytesRead += discardUnit(reader);
        }
        
        
        int recordLength = readAndProcessInt(reader,5);
        reader.skip(7);
        int startOfDataArea = readAndProcessInt(reader,5);
        reader.skip(3);
        int entryLengthLength = readAndProcessInt(reader,1); // how many bytes compose the length of the entry
        int startingPointLength = readAndProcessInt(reader,1);
        reader.read();
        int tagFieldLength = readAndProcessInt(reader,1);
        bytesRead += 24;
        int numDirectoryElements = (startOfDataArea - 23) / (entryLengthLength + tagFieldLength + startingPointLength);
        int[] tagFields = new int[numDirectoryElements];
        int[] entryLengths = new int[numDirectoryElements];
        int[] startingPoints = new int[numDirectoryElements];
        
        for(int i = 0; i < numDirectoryElements; i++){
            tagFields[i] = readAndProcessInt(reader,tagFieldLength);
            entryLengths[i] = readAndProcessInt(reader,entryLengthLength);
            startingPoints[i] = readAndProcessInt(reader,startingPointLength);
            bytesRead += tagFieldLength + entryLengthLength + startingPointLength;
        }
        reader.skip(1);
        bytesRead++;
        elevations = new float[nRows][nColumns];
        
        for(int currentRow = nRows - 1; currentRow >= 0; currentRow--){
            int recordID = readAndProcessInt(reader,entryLengths[0]-1); // subtract one because we do not want to process the unit terminator
            reader.skip(5); // skip the unit terminator and the module name
            bytesRead += entryLengths[0] + 4;
            
            int sdtsRecNum = readAndProcessInt(reader,(entryLengths[1]/3)-1);
            bytesRead += (entryLengths[1]/3) - 1;
            int row = readAndProcessInt(reader,(entryLengths[1]/3)-1);
            bytesRead += (entryLengths[1]/3) - 1;
            int col = readAndProcessInt(reader,(entryLengths[1]/3)-1);
            bytesRead += (entryLengths[1]/3);
            reader.read();
            for (int currentColumn = 0; currentColumn < nColumns; currentColumn++){
                ByteBuffer bb = ByteBuffer.allocate(2);
                bb.put((byte)reader.read());
                bb.put((byte)reader.read());
                bytesRead += 2;
                int data = bb.getChar(0);
                if((data <= maxElevation && data >= minElevation)){
                    elevations[currentRow][currentColumn] = data;
                }
                
            }
            
            bytesRead += discardUnit(reader);
            
            
        }
        return bytesRead;
    }
    /**
     * Searches the ldef file to find the number of rows and columns to expect for the elevation data.
     * Assumes that the number of columns comes directly after the number of rows.
     * @return The number of bytes read.
     */
    protected int processRowColMax(InputStream reader) throws IOException{
        int bytesRead = 0;
        bytesRead += processLeader(reader);
        
        // skip all but the last entries in the data descriptive area
        for(int i = 0; i < numDirectoryElements-1; i++){
            bytesRead += discardUnit(reader);
        }
        bytesRead += discardField(reader);
        
        // find the number of data descriptors
        int numDescriptors = 0;
        int rowPosition = 0;
        int colPosition = 0;
        while(reader.available() > 0){
            byte[] b = new byte[4];
            reader.read(b);
            bytesRead += 4;
            String id = new String(b);
            if(id.equals("NROW")){
                rowPosition = numDescriptors;
            }
            if(id.equals("NCOL")){
                colPosition = numDescriptors;
            }
            int n = reader.read();
            bytesRead++;
            numDescriptors++;
            if(n == 31){
                break;
            }
        }
        for(int i = 0; i < rowPosition; i++){
            bytesRead += discardField(reader);
        }
        int[] tempBytes = new int[10];
        int length = 0;
        while(reader.available()>0){
            tempBytes[length] = reader.read();
            bytesRead++;
            if(tempBytes[length] == 31){
                break;
            } else{
                length++;
            }
        }
        String rows = new String(tempBytes,0,length);
        nRows = Integer.parseInt(rows);
        //System.out.println("nRows = " + nRows);
        length = 0;
        while(reader.available()>0){
            tempBytes[length] = reader.read();
            bytesRead++;
            if(tempBytes[length] == 31){
                break;
            } else{
                length++;
            }
        }
        String cols = new String(tempBytes,0,length);
        nColumns = Integer.parseInt(cols);
        //System.out.println("nColumns = " + nColumns);
        return bytesRead;
    }
    
    
    
}
