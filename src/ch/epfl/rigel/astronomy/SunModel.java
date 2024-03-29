package ch.epfl.rigel.astronomy;

import ch.epfl.rigel.coordinates.EclipticCoordinates;
import ch.epfl.rigel.coordinates.EclipticToEquatorialConversion;
import ch.epfl.rigel.coordinates.EquatorialCoordinates;
import ch.epfl.rigel.math.Angle;
import ch.epfl.rigel.math.RightOpenInterval;

public enum SunModel implements CelestialObjectModel<Sun> {

    SUN();

    private static final double ANGULAR_SPEED = Angle.TAU / 365.242191;
    private static final double LON_J2010 = Angle.ofDeg( 279.557208 );
    private static final double LON_PERIGEE = Angle.ofDeg( 283.112438 );
    private static final double ECCENTRICITY = 0.016705;
    private static final double THETA = Angle.ofDeg( 0.533128 );

    private static final RightOpenInterval lonInterval = RightOpenInterval.of( 0, Angle.TAU );

    private static final double DELTA_LON = LON_J2010 - LON_PERIGEE;
    private static final double ECCENTRICITY_SQUARED = Math.pow( ECCENTRICITY, 2 );

    /**
     * @param daysSinceJ2010 : number of days after the J2010 (possibly negative)
     * @param eclipticToEquatorialConversion : Conversion used to get its equatorial coordinates from its ecliptic coordinates
     * @return returns the Sun modeled by SunModel for the (possibly negative) number of days after
     *         the given J2010 epoch, using the given conversion to obtain its equatorial coordinates from its ecliptic coordinates
     */
    @Override
    public Sun at(
            double daysSinceJ2010,
            EclipticToEquatorialConversion eclipticToEquatorialConversion )
    {
        double meanAnomaly = ANGULAR_SPEED * daysSinceJ2010 + DELTA_LON;
        double trueAnomaly = meanAnomaly + 2 * ECCENTRICITY * Math.sin( meanAnomaly );

        EclipticCoordinates eclipticPos = EclipticCoordinates.of( lonInterval.reduce(trueAnomaly + LON_PERIGEE ), 0 );
        EquatorialCoordinates equatorialPos = eclipticToEquatorialConversion.apply( eclipticPos );

        double angularSize = THETA * ( 1 + ECCENTRICITY * Math.cos( trueAnomaly ) ) / ( 1 - ECCENTRICITY_SQUARED );

        return new Sun( eclipticPos, equatorialPos, (float)angularSize, (float)meanAnomaly );
    }
}
