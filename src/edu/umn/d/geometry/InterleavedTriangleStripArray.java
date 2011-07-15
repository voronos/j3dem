package edu.umn.d.geometry;
import javax.media.j3d.*;
/**
 *
 *  This class is a specialization of the TriangleStripArray created
 * to enhance performance of generating triangle strips.  This
 * is done by adding a generateNormals method to calculate, in
 * place the normals for each vertex. This saves the overhead
 * in processor time and memory required for using the NormalsGenerator
 * object.
 *
 *
 * @author  Mark Pendergast
 * @version 1.0 February 2003
 */
public class InterleavedTriangleStripArray extends TriangleStripArray {
    private int FLOATSPERVERTEX= 0;
    private int TEXT_OFFSET = 0;
    private int COLOR_OFFSET = 0;
    private int NORMAL_OFFSET = 0;
    private int COORD_OFFSET = 0;
    /**
     * Setups the offset of the data based on the vertex format passed in.
     * Currently not in use, as the offsets are hard coded.
     * @param vertexCount the number of vertex elements in this array
     * @param vertexFormat a mask indicating which components are present in each vertex.
     *           This is specified as one or more individual flags that are bitwise "OR"ed together to
     *         describe the per-vertex data. The flags include: COORDINATES, to signal the
     *         inclusion of vertex positions--always present; NORMALS, to signal the inclusion of
     *          per vertex normals; one of COLOR_3, COLOR_4, to signal the inclusion of per
     *          vertex colors (without or with color information); and one of
     *          TEXTURE_COORDINATE_2 or TEXTURE_COORDINATE_3, to signal the
     *          inclusion of per-vertex texture coordinates 2D or 3D.
     * @param stripVertexCounts - array that specifies the count of the number of vertices for
     *         each separate strip. The length of this array is the number of separate strips.
     */
    public InterleavedTriangleStripArray(int vertexCount, int vertexFormat,
            int[] stripVertexCounts) {
        super(vertexCount, vertexFormat, stripVertexCounts);
        setCapability(ALLOW_REF_DATA_READ);
        setCapability(ALLOW_REF_DATA_WRITE);
        setCapability(ALLOW_COORDINATE_READ);
        setCapability(ALLOW_COORDINATE_WRITE);
        setCapability(ALLOW_FORMAT_READ);
        setCapability(ALLOW_COUNT_READ);
        if( (vertexFormat & BY_REFERENCE) == 0 ||
                (vertexFormat & INTERLEAVED) == 0)
            throw new IllegalArgumentException("INTERLEAVED and/or BY_REFERENCE not specified in vertexFormat");
        
        if((vertexFormat & TEXTURE_COORDINATE_2) == TEXTURE_COORDINATE_2) {
            COLOR_OFFSET += 2;
            NORMAL_OFFSET += 2;
            COORD_OFFSET += 2;
            FLOATSPERVERTEX += 2;
        }
        if((vertexFormat & TEXTURE_COORDINATE_3) == TEXTURE_COORDINATE_3) {
            COLOR_OFFSET += 3;
            NORMAL_OFFSET += 3;
            COORD_OFFSET += 3;
            FLOATSPERVERTEX += 3;
        }
        if((vertexFormat & COLOR_4) == COLOR_4)   // COLOR_4 has a bit for COLOR_3 not a true mask
        {
            NORMAL_OFFSET += 4;
            COORD_OFFSET += 4;
            FLOATSPERVERTEX += 4;
        } else
            if((vertexFormat & COLOR_3) != 0) {
            NORMAL_OFFSET += 3;
            COORD_OFFSET += 3;
            FLOATSPERVERTEX += 3;
            }
        if((vertexFormat & NORMALS) != 0) {
            COORD_OFFSET += 3;
            FLOATSPERVERTEX += 3;
        }
        if((vertexFormat & COORDINATES) != 0) {
            FLOATSPERVERTEX += 3;
        }
        
        /*System.out.println("Texture offset is " + TEXT_OFFSET +
                "\nColor offset is " + COLOR_OFFSET +
                "\nNormal offset is " + NORMAL_OFFSET +
                "\nCoord offset is " + COORD_OFFSET +
                "\nFloatspervertex = " + FLOATSPERVERTEX);*/
        
    }
    
    
    
    
    /**
     *  Traverse interleaved vertex data and generate normal
     *  values.
     *
     *  @param averageAdjacentColumns if true, then the algorithm assumes
     *  that the strips comprise a grid whereby adjacent strips have common
     * vertices. The normals of the vertices are averaged to eliminate stripes.
     */
    public void generateNormals(boolean averageAdjacentColumns) {
        double norms[][]  = new double[3][3];
        double avgNorm[] = new double[3];
        int nNormals = 0;
        //
        //  bail out if no normals
        //
        if((getVertexFormat() & NORMALS) == 0) {
            throw new IllegalArgumentException("NORMALS not specified in vertexFormat");
        }
        
//
        float vertexData[] = getInterleavedVertices();
        int nStrips = getNumStrips();
        int stripCounts[] = new int[nStrips];
        
        getStripVertexCounts(stripCounts);
        
//
// work a strip at a time
//
        int vertex=0, strip=0, i=0,prev=0;
        try {
            for(strip= 0, i = 0; strip < nStrips; strip++) {
                //
                //  each vertex could participate in as many as 3 or as few
                // as one triangles based on its position.  Calculate a normal
                // for each triangle, then average them, finally normalize the average
                //
                for( vertex = 0; vertex < stripCounts[strip]; vertex++) {
                    //
                    // Counter Clockwise, right-side, up to three normals for this strip
                    //
                    if(vertex % 2 ==1) {
                        nNormals = 0;
                        if(vertex > 2) // if not on bottom row
                            calcNormalCCW(norms[nNormals++],vertexData,i+COORD_OFFSET-FLOATSPERVERTEX*2, i+COORD_OFFSET, i+COORD_OFFSET-FLOATSPERVERTEX);
                        
                        if(vertex < stripCounts[strip]-2) // only if not last vertex in strip
                            calcNormalCCW(norms[nNormals++],vertexData,i+COORD_OFFSET-FLOATSPERVERTEX, i+COORD_OFFSET, i+COORD_OFFSET+FLOATSPERVERTEX);
                        
                        if(vertex < stripCounts[strip]-2) // only if not next to last vertex in strip
                            calcNormalCCW(norms[nNormals++],vertexData,i+COORD_OFFSET+FLOATSPERVERTEX, i+COORD_OFFSET, i+COORD_OFFSET+FLOATSPERVERTEX*2);//
                        
                        averageNormals(norms, nNormals, avgNorm);
                        vertexData[i+NORMAL_OFFSET] = (float)avgNorm[0];
                        vertexData[i+NORMAL_OFFSET+1] = (float)avgNorm[1];
                        vertexData[i+NORMAL_OFFSET+2] = (float)avgNorm[2];
                        normalize(vertexData,i);
                    }
                    //
                    // CounterClockwis, left-side, up to three normals for this strip
                    //
                    else {
                        nNormals = 0;
                        if(vertex > 0) {
                            calcNormalCCW(norms[nNormals++],vertexData,i+COORD_OFFSET-FLOATSPERVERTEX, i+COORD_OFFSET, i+COORD_OFFSET-FLOATSPERVERTEX*2);//
                            if(vertex < stripCounts[strip]-1) // not if last one in strip
                                calcNormalCCW(norms[nNormals++],vertexData,i+COORD_OFFSET+FLOATSPERVERTEX, i+COORD_OFFSET, i+COORD_OFFSET-FLOATSPERVERTEX);//
                        }
                        if(vertex < stripCounts[strip]-2) // not if next to the last one in strip
                            calcNormalCCW(norms[nNormals++],vertexData,i+COORD_OFFSET+FLOATSPERVERTEX*2, i+COORD_OFFSET, i+COORD_OFFSET+FLOATSPERVERTEX);
                        
                        averageNormals(norms, nNormals, avgNorm);
                        vertexData[i+NORMAL_OFFSET] = (float)avgNorm[0];
                        vertexData[i+NORMAL_OFFSET+1] = (float)avgNorm[1];
                        vertexData[i+NORMAL_OFFSET+2] = (float)avgNorm[2];
                        normalize(vertexData,i);
                        //
                        //  look back to previous strip and average normal with the shared vertex
                        //
                        if(averageAdjacentColumns && strip > 0)  // don't do this on the first column as there is no previous strip
                        {
                            prev = i - stripCounts[strip-1]*FLOATSPERVERTEX + FLOATSPERVERTEX; // index of vertex with same coordinates in previous strip
                            avgNorm[0] = (vertexData[i+NORMAL_OFFSET] + vertexData[prev+NORMAL_OFFSET])/2.0d;
                            avgNorm[1] = (vertexData[i+NORMAL_OFFSET+1] + vertexData[prev+NORMAL_OFFSET+1])/2.0d;
                            avgNorm[2] = (vertexData[i+NORMAL_OFFSET+2] + vertexData[prev+NORMAL_OFFSET+2])/2.0d;
                            vertexData[i+NORMAL_OFFSET] = (float)avgNorm[0];
                            vertexData[i+NORMAL_OFFSET+1] = (float)avgNorm[1];
                            vertexData[i+NORMAL_OFFSET+2] = (float)avgNorm[2];
                            vertexData[prev+NORMAL_OFFSET] = (float)avgNorm[0];
                            vertexData[prev+NORMAL_OFFSET+1] = (float)avgNorm[1];
                            vertexData[prev+NORMAL_OFFSET+2] = (float)avgNorm[2];
                            normalize(vertexData,i);
                            normalize(vertexData,prev);
                        }
                        
                    }
//
                    i+= FLOATSPERVERTEX;
                } // end for vertices
            } // end for columns
        } catch(ArrayIndexOutOfBoundsException e) {
            System.out.println(vertex+" "+strip+" "+prev);
        }
    }
    /**
     *  Calculate normal, assumes clockwise order of vertices
     * @param vertexData the interleaved data.
     *
     * @param norm reference to a double[3] array to receive the result
     * @param v0 index of first vertex in interleaved data
     * @param v1 index of middle vertex in interleaved data
     * @param v2 index of last vertex in interleaved data
     */
    public void calcNormalCW(double norm[], float vertexData[], int v0, int v1, int v2) {
        // original = calcNormalCCW( norm, vertexData,  v2, v1,  v1);
        calcNormalCCW(norm, vertexData, v2, v1, v0);
    }
    /**
     *  Calculate normal, assumes counter-clockwise order of vertices
     * @param vertexData the interleaved data.
     *
     * @param normal reference to a double[3] array to receive the result
     * @param v0 index of first coordinates in interleaved data
     * @param v1 index of middle coordinates in interleaved data
     * @param v2 index of last coordinates in interleaved data
     */
    
    public void calcNormalCCW(double normal[], float vertexData[], int v0, int v1, int v2) {
        //
        // use double for intermiate calculations
        //
        
        double[]  a = new double[3], b = new double[3];
        double r;
        
        //
        // create two vectors a(v1,v2) and b(v1,v0), move them to the origin, giving
        // vectors (0,0,0) (ax, ay, az) and (0,0,0) (bx, by, bz)
        //
        a[0] = vertexData[v2] - vertexData[v1];
        a[1] = vertexData[v2+1] - vertexData[v1+1];
        a[2] = vertexData[v2+2] - vertexData[v1+2];
        
        b[0] = vertexData[v0+0] - vertexData[v1+0];
        b[1] = vertexData[v0+1] - vertexData[v1+1];
        b[2] = vertexData[v0+2] - vertexData[v1+2];
        
//
// calculate the cross product (right hand rule) of a and b
//
        normal[0] = a[1]*b[2] - a[2]*b[1];
        normal[1] = a[2]*b[0] - a[0]*b[2];
        normal[2] = a[0]*b[1] - a[1]*b[0];
        
//
// normalize the cross product, make its length 1(from origin)
//
        r = Math.sqrt(normal[0]*normal[0] + normal[1]*normal[1] + normal[2]*normal[2]);
        normal[0] /= r;
        normal[1] /= r;
        normal[2] /= r;
    }
    /**
     * This method calculates the average of a set of normals stored in an
     * array of doubles.
     *
     * @param normals a an N by 3 double array holding vector normals to be averaged
     * @param nNormals number of normals to average
     * @param avgNormal destination for the averaged normal
     */
    public static void averageNormals(double normals[][],int nNormals, double[] avgNormal) {
//
// clear out destination array
//
        for(int i = 0; i < avgNormal.length; i++)
            avgNormal[i] = 0.0;
//
// average each normal
//
        for(int i = 0; i < nNormals; i++) {
            for(int j = 0; j < normals[i].length; j++) {
                avgNormal[j] += normals[i][j]/nNormals;
            }
        }
    }
    /**
     *  normalize (make length equal 1) vector from origin to normal
     *
     *  @param vertexData interleaved array of floats
     *  @param v index into interleaved data of vertex data to be normalized.
     */
    public  void normalize(float[] vertexData, int v) {
        double r;
        //
        //
        r = Math.sqrt(vertexData[v+NORMAL_OFFSET]*vertexData[v+NORMAL_OFFSET] + vertexData[v+NORMAL_OFFSET+1]*vertexData[v+NORMAL_OFFSET+1] + vertexData[v+NORMAL_OFFSET+2]*vertexData[v+NORMAL_OFFSET+2]);
        vertexData[v+NORMAL_OFFSET] /= r;
        vertexData[v+NORMAL_OFFSET+1] /= r;
        vertexData[v+NORMAL_OFFSET+2] /= r;
        
    }
    
    /**
     * Resets to exageration of 1, then applies the new value.
     * @param oldAmount last known exageration value.
     * @param newAmount desired exageration value.
     */
    public void changeExageration(float oldAmount, float newAmount){
        float[] vertexData = getInterleavedVertices();
        for(int i = 0; i < vertexData.length; i+= FLOATSPERVERTEX){
            vertexData[i+COORD_OFFSET+1] /= oldAmount;
            vertexData[i+COORD_OFFSET+1] *= newAmount;
        }
        setInterleavedVertices(vertexData);
        generateNormals(true);
        
    }
    
}