package edu.umn.d.fileformats;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Class for handling a tarballed SDTS data set.
 * @author nels2426
 */
public class GZDDFFile extends DDFFileAbstract {
    private GZIPInputStream reader;
    private static int SIZE = 512;
    
    /**
     * Creates a new instance of GZDDFFile
     * @param aFileName The name of the file to read.
     * @throws java.io.IOException Thrown if file does not exist.
     */
    public GZDDFFile(URL aFileName) throws IOException{
        super(aFileName);
        URL url = null;
        try{
            url = aFileName;
            reader = new GZIPInputStream(url.openStream());
        } catch(FileNotFoundException e){
            e.printStackTrace();
            System.exit(0);
        } catch(MalformedURLException e2){
            e2.printStackTrace();
            System.exit(0);
        }
        
        int i = 0;
        while(reader.available() > 0){
            try{
                //System.out.println("\n\nBlock: " + i);
                i++;
                byte[] b = readBlock();
                String fileName = findName(b);
                if(fileName.length() > 12)
                    continue;
                else if (fileName.length() < 12){
                    SIZE -= fileName.length();
                    b = readBlock();
                    fileName += findName(b);
                    SIZE = 512;
                }
                //System.out.println("name = " + fileName);
                int fileSize = findSize(b);
                if(fileName.contains("iden.ddf")){
                    int bytesRead = processGroundCoordinates(reader);
                    reader.skip(fileSize - bytesRead);
                } else if(fileName.contains("cel0.ddf")){
                    int bytesRead = processElevations(reader);
                    reader.skip(fileSize - bytesRead);
                } else if(fileName.contains("ddom.ddf")){
                    int bytesRead = processMinMaxElevation(reader);
                    reader.skip(fileSize - bytesRead);
                } else if(fileName.contains("ldef.ddf")){
                    int bytesRead = processRowColMax(reader);
                    reader.skip(fileSize - bytesRead);
                } else{
                    reader.skip(fileSize);
                }
            } catch(NumberFormatException e){
                continue;
            }
        }
        
    }
    
    private String findName(byte[] b){
        String s = "";
        for(int i = 0; i < 99; i++){
            if(b[i] == 0)
                break;
            s += (char)b[i];
        }
        return s;
    }
    private int findSize(byte[] b) throws NumberFormatException{
        String s = "";
        for(int i = 124; i < 135; i++){
            s += (char)b[i];
        }
        int size = Integer.parseInt(s, 8);
        int f = 1;
        while(SIZE * f < size){
            f += 1;
        }
        size = (size % SIZE == 0) ? size : SIZE * f;
        return size;
    }
    private byte[] readBlock() throws IOException{
        byte[] b = new byte[SIZE];
        reader.read(b, 0, SIZE);
        return b;
    }
}
