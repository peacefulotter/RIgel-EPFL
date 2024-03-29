package ch.epfl.rigel.coordinates;

import ch.epfl.rigel.math.Angle;
import ch.epfl.rigel.math.ClosedInterval;
import ch.epfl.rigel.math.RightOpenInterval;

import java.util.Locale;

public final class GeographicCoordinates extends SphericalCoordinates
{
    // Interval of the longitude in degrees
    private static final RightOpenInterval LON_DEG_INTERVAL = RightOpenInterval.of( -180, 180 );
    // Interval of the latitude in degrees
    private static final ClosedInterval LAT_DEG_INTERVAL = ClosedInterval.of( -90, 90 );


    private GeographicCoordinates( double lonDeg, double latDeg )
    {
        // store the longitude and latitude in radians
        super( Angle.ofDeg( lonDeg ), Angle.ofDeg( latDeg ) );
    }


    @Override
    public double lon() { return super.lon(); }

    @Override
    public double lonDeg() { return super.lonDeg(); }

    @Override
    public double lat() { return super.lat(); }

    @Override
    public double latDeg() { return super.latDeg(); }

    /**
     * Creates a Geographic Coordinate
     * @param lonDeg : longitude in degrees
     * @param latDeg : latitude in degrees
     * @throws IllegalArgumentException if the longitude or the latitude given is out of bound
     * @return the coordinate
     */
    public static GeographicCoordinates ofDeg( double lonDeg, double latDeg )
    {
        if ( !isValidLonDeg( lonDeg ) || !isValidLatDeg( latDeg ) )
        {
            throw new IllegalArgumentException( "Invalid longitude or latitude" );
        }
        return new GeographicCoordinates( lonDeg, latDeg );
    }

    /**
     * Check if the longitude is contained in the LON_DEG_INTERVAL
     * @param lonDeg : the longitude in degrees
     * @return true if contained, false if not
     */
    public static boolean isValidLonDeg( double lonDeg )
    {
        return LON_DEG_INTERVAL.contains( lonDeg );
    }

    /**
     * Check if the latitude is contained in the LAT_DEG_INTERVAL
     * @param latDeg : the latitude in degrees
     * @return true if contained, false if not
     */
    public static boolean isValidLatDeg( double latDeg )
    {
        return LAT_DEG_INTERVAL.contains( latDeg );
    }

    @Override
    public String toString()
    {
        return String.format( Locale.ROOT, "(lon=%.4f°, lat=%.4f°)", lonDeg(), latDeg() );
    }
}
