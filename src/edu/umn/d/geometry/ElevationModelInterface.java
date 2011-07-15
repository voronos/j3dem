package edu.umn.d.geometry;
/**
 * Defines an generic inteface between the  ElevationModel and
 * other objects that require its characteristics for display purposes.
 *
 * @author  Mark Pendergast
 * @version 1.0 February 2003
 *
 */
public interface ElevationModelInterface
{
/**
* Fetches the elevation at a particular location on the terrain map given
* the x,z coordinates.  X,Z coordinates represent the distance in meters
* from the center of the terrain image.
* @param x x coordinate
* @param z z coordinate
* @return the elevation (y coordinate) in meters (adjusted for exageration)
*/
  public abstract float getElevationAt(float x, float z);
 /**
 * retrieve the model length, distance in meters from west to east
 *  @return the length in meters
 */
 public abstract float getModelLength();
/**
 * retrieve the model width, distance in meters from south to north
 *  @return the width in meters
 */
  public abstract float getModelWidth();
/**
 * retrieve the model maximum elevation, adjusted by the elevation exageration
 *  @return the adjusted maximum elevation in meters
 */
  public abstract float getMaxElevation();
/**
 * retrieve the model minimum elevation, adjusted by the elevation exageration
 *  @return the adjusted minimum elevation in meters
 */
  public abstract float getMinElevation();
  
}

