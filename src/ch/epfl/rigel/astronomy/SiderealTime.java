package ch.epfl.rigel.astronomy;

import ch.epfl.rigel.coordinates.GeographicCoordinates;
import ch.epfl.rigel.math.Angle;
import ch.epfl.rigel.math.Polynomial;
import ch.epfl.rigel.math.RightOpenInterval;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Contains two static methods used to calculate the sidereal time
 */
public final class SiderealTime
{
    private final static RightOpenInterval HOURS_INTERVAL = RightOpenInterval.of( 0, 24 );
    // a polynomial to calculate the delta between a ZonedDateTime and J2000
    private final static Polynomial CENTURY_POLY = Polynomial.of( 0.000025862, 2400.051336, 6.697374558 );
    private final static Polynomial HOURS_POLY = Polynomial.of( 1.002737909, 0 );

    private SiderealTime() {}

    /**
     * @param when: the actual time date and hour
     * @return : the Greenwish Sidereal time in radians for a precise date and hour (when)
     */
    public static double greenwich( ZonedDateTime when )
    {
        ZonedDateTime dayStart = when.withZoneSameInstant( ZoneOffset.UTC )
                .truncatedTo( ChronoUnit.DAYS );

        double T = Epoch.J2000.julianCenturiesUntil( dayStart );
        double t = dayStart.until( when, ChronoUnit.MILLIS );
        t = t / ( 1000 * 3600 );

        double deltaJulianCentury = HOURS_INTERVAL.reduce( CENTURY_POLY.at( T ) );
        double hoursStartOfDay = HOURS_POLY.at( t );

        double greenwichSiderealTime = deltaJulianCentury + hoursStartOfDay;

        return Angle.normalizePositive( Angle.ofHr( greenwichSiderealTime ) );
    }

    /**
     * @param when: the actual time date and hour
     * @param where: a position
     * @return: the local Sidereal time in radians for a precise date and hour (when) and a position (where)
     */
    public static double local( ZonedDateTime when, GeographicCoordinates where )
    {
        return Angle.normalizePositive( greenwich( when ) + where.lon() );
    }
}
