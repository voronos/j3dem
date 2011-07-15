package edu.umn.d.geometry;
/**
 * This class is used to store the ground coordinates of a terrain data segment
 * the latitude and longitude of each corner of the terrain area are stored in
 * arc-second units
 *
 */
public class GroundCoordinates {

  /**
   * latitude and longitude of south west coordinate.
   */
  public double sw[] = new double[2]; // [0] = longitude [1] = latitude in arc-seconds
  /**
   * latitude and longitude of north west coordinate.
   */
  public double nw[] = new double[2];
  /**
   * latitude and longitude of north east coordinate.
   */
  public double ne[] = new double[2];
  /**
   * latitude and longitude of south east coordinate.
   */
  public double se[] = new double[2];

  /**
   * index of longitude part of coordinate.
   */
  public static final int LONGITUDE = 0;
  /**
   * index of latitude part of coordinate.
   */
  public  static final int LATITUDE = 1;

  /**
   * Number of seconds in a nautical mile.
   */
  public static final double SECONDS_PER_NAUTICAL_MILE= 60.0;
  /**
   * Number of meters in a nautical mile.
   */
  public static final double METERS_PER_NAUTICAL_MILE =1854.4;
  /**
   * Number of meters in a nautical second.
   */
  public static final double METERS_PER_NAUTICAL_SECOND =30.91;
  /**
   * Number of nautical seconds in a meter.
   */
  public static final double NAUTICAL_SECONDS_PER_METER =.0324;
/**
 * create ground coordinate object with all values set to null
 */
  public GroundCoordinates()
  {

  }
  /**
  *  calculate the length (east to west) of the segment
  * @return the east-west distance in meters
  */
  public float lengthMeters()
  {
	  return (float)(Math.abs(sw[LONGITUDE]-se[LONGITUDE])*METERS_PER_NAUTICAL_SECOND);
  }

  /**
  *  calculate the width (south to north ) of the segment
  * @return the south to north distance in meters
  */
  public float widthMeters()
  {
	  return (float)(Math.abs(nw[LATITUDE]-sw[LATITUDE])*METERS_PER_NAUTICAL_SECOND);
  }
  /**
  *  calculate the length (east to west) of the segment
  * @return the east-west distance in seconds
  */

  public float lengthSeconds()
  {
	  return (float)Math.abs(sw[LONGITUDE]-se[LONGITUDE]);
  }

  /**
  *  calculate the width (south to north ) of the segment
  * @return the south to north distance in seconds
  */
  public float widthSeconds()
  {
	  return (float)Math.abs(nw[LATITUDE]-sw[LATITUDE]);
  }

/**
 *  convert to string for display purposes
 * @return string representation of the coordinates
 */
  public String toString()
  {
   return "nw ("+nw[LATITUDE]+","+nw[LONGITUDE]+"). "+
              "ne ("+ne[LATITUDE]+","+ne[LONGITUDE]+"). "+
              "\nsw ("+sw[LATITUDE]+","+sw[LONGITUDE]+"). "+
              "se ("+sw[LATITUDE]+","+se[LONGITUDE]+")";
 }

}

