package ch.epfl.rigel.astronomy;

import ch.epfl.rigel.coordinates.EclipticCoordinates;
import ch.epfl.rigel.coordinates.EclipticToEquatorialConversion;
import ch.epfl.rigel.coordinates.EquatorialCoordinates;
import ch.epfl.rigel.math.Angle;
import ch.epfl.rigel.math.RightOpenInterval;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a Model for the eight planets of the Solar System
 */
public enum PlanetModel implements CelestialObjectModel<Planet> {


    MERCURY("Mercure", 0.24085, 75.5671, 77.612, 0.205627,
            0.387098, 7.0051, 48.449, 6.74, -0.42 ),
    VENUS("Vénus", 0.615207, 272.30044, 131.54, 0.006812,
            0.723329, 3.3947, 76.769, 16.92, -4.40 ),
    EARTH("Terre", 0.999996, 99.556772, 103.2055, 0.016671,
            0.999985, 0, 0, 0, 0 ),
    MARS("Mars", 1.880765, 109.09646, 336.217, 0.093348,
            1.523689, 1.8497, 49.632, 9.36, -1.52 ),
    JUPITER("Jupiter", 11.857911, 337.917132, 14.6633, 0.048907,
            5.20278, 1.3035, 100.595, 196.74, -9.40 ),
    SATURN("Saturne", 29.310579, 172.398316, 89.567, 0.053853,
            9.51134, 2.4873, 113.752, 165.60, -8.88 ),
    URANUS("Uranus", 84.039492, 356.135400, 172.884833, 0.046321,
            19.21814, 0.773059, 73.926961, 65.80, -7.19 ),
    NEPTUNE("Neptune", 165.84539, 326.895127, 23.07, 0.010483,
            30.1985, 1.7673, 131.879, 62.20, -6.87 );


    private static final double ANGULAR_SPEED = Angle.TAU / 365.242191;
    private static final RightOpenInterval lonInterval = RightOpenInterval.of( 0, Angle.TAU );

    public static final List<PlanetModel> ALL = Arrays.asList(
            MERCURY, VENUS, EARTH, MARS, JUPITER, SATURN, URANUS, NEPTUNE );

    private final String name;
    private final double revolutionPeriod; // revolution period
    private final double lonJ2010; // longitude at J2010
    private final double lonPerigee; // longitude at perigee
    private final double orbitEccentricity;
    private final double halfOrbitMajorAxis;
    private final double inclinationOrbit; // inclination orbit to the ecliptic
    private final double lonAscendingNode; // longitude of the ascending node
    private final double angularSize;
    private final double magnitude;

    private final double deltaLon; // a delta between lonJ2010 and lonPerigee
    private final double eccentricitySquared; // eccentricity raised to the power of 2


    PlanetModel(
            String name, double revolutionPeriod, double lonJ2010,
            double lonPerigee, double orbitEccentricity, double halfOrbitMajorAxis,
            double inclinationOrbit, double lonAscendingNode, double angularSize,
            double magnitude )
    {
        this.name = name;
        this.revolutionPeriod = revolutionPeriod;
        this.lonJ2010 = Angle.ofDeg(lonJ2010);
        this.lonPerigee = Angle.ofDeg(lonPerigee);
        this.orbitEccentricity = orbitEccentricity;
        this.halfOrbitMajorAxis = halfOrbitMajorAxis;
        this.inclinationOrbit = Angle.ofDeg(inclinationOrbit);
        this.lonAscendingNode = Angle.ofDeg(lonAscendingNode);
        this.angularSize = Angle.ofArcsec(angularSize);
        this.magnitude = magnitude;

        this.deltaLon = this.lonJ2010 - this.lonPerigee;
        this.eccentricitySquared = Math.pow( orbitEccentricity, 2 );
    }

    /**
     * @param daysSinceJ2010 : number of days after the J2010 (possibly negative)
     * @param eclipticToEquatorialConversion : Conversion used to get its equatorial coordinates from its ecliptic coordinates
     * @return returns the object modeled by the model for the (possibly negative) number of days after
     *         the given J2010 epoch, using the given conversion to obtain its equatorial coordinates from its ecliptic coordinates
     */
    @Override
    public Planet at(
            double daysSinceJ2010,
            EclipticToEquatorialConversion eclipticToEquatorialConversion )
    {
        double meanAnomaly = ANGULAR_SPEED * ( daysSinceJ2010 / revolutionPeriod ) + deltaLon;
        double trueAnomaly = meanAnomaly + 2 * orbitEccentricity * Math.sin( meanAnomaly );


        double radius = ( halfOrbitMajorAxis * ( 1 - eccentricitySquared ) )  /  ( 1 + orbitEccentricity * Math.cos( trueAnomaly ) );
        double longitude = trueAnomaly + lonPerigee;

        double deltaLonSin = Math.sin( longitude - lonAscendingNode );
        double deltaLonCos = Math.cos( longitude - lonAscendingNode );
        double inclinationCos = Math.cos( inclinationOrbit );
        double inclinationSin = Math.sin( inclinationOrbit );

        double psi = Math.asin( deltaLonSin * inclinationSin );
        double cosPsi = Math.cos( psi );

        double projectedRadius = radius * cosPsi;
        double projectedLongitude = lonInterval.reduce(
                Math.atan2( deltaLonSin * inclinationCos, deltaLonCos ) + lonAscendingNode
        );

        /* Earth constants -> 0: earthRad, 1: earthLon */
        double[] earthConstants = getEarthConstants( daysSinceJ2010 );
        double earthRadius = earthConstants[ 0 ];
        double earthLongitude = earthConstants[ 1 ];

        /* EQUATORIAL POS */
        double eclipticLon;
        if ( this == MERCURY || this == VENUS )
        {
            eclipticLon = getInnerLon( projectedRadius, projectedLongitude, earthRadius, earthLongitude );
        } else {
            eclipticLon = getOuterLon( projectedRadius, projectedLongitude, earthRadius, earthLongitude );
        }
        eclipticLon = lonInterval.reduce( eclipticLon );

        double eclipticLat = Math.atan(
                ( projectedRadius * Math.tan( psi ) * Math.sin( eclipticLon - projectedLongitude ) ) /
                ( earthRadius * Math.sin( projectedLongitude - earthLongitude ) )
        );

        EclipticCoordinates eclipticPos = EclipticCoordinates.of( eclipticLon, eclipticLat );
        EquatorialCoordinates equatorialPos = eclipticToEquatorialConversion.apply( eclipticPos );
        /* END OF EQUATORIAL POS */


        /* ANGULAR SIZE */
        double distance = Math.sqrt( Math.abs(
                Math.pow( earthRadius, 2 ) + Math.pow( radius, 2 ) - 2 * earthRadius * radius * Math.cos( longitude - earthLongitude ) * cosPsi
        ) );
        double planetAngularSize = angularSize / distance;
        /* END OF ANGULAR SIZE */


        /* MAGNITUDE */
        double phase = ( 1 + Math.cos( eclipticLon - longitude ) ) / 2;
        double planetMagnitude =  magnitude + 5 * Math.log10( radius * distance / Math.sqrt( phase ) );
        /* END OF MAGNITUDE */


        return new Planet( name, equatorialPos, (float) planetAngularSize, (float) planetMagnitude );
    }


    /**
     * Get the Ecliptic Geocentric Longitude for inner planets
     * @param planetRadius : planet radius
     * @param planetLon : planet longitude
     * @return ecliptic longitude
     */
    private double getInnerLon( double planetRadius, double planetLon, double earthRad, double earthLon )
    {
        double delta = earthLon - planetLon;
        return Math.PI + earthLon + Math.atan(
                ( planetRadius * Math.sin( delta ) )  /  ( earthRad - planetRadius * Math.cos( delta ) )
        );
    }

    /**
     * Get the Ecliptic Geocentric Longitude for outer planets
     * @param planetRadius : planet radius
     * @param planetLon : planet longitude
     * @return ecliptic longitude
     */
    private double getOuterLon( double planetRadius, double planetLon, double earthRad, double earthLon )
    {
        double delta = planetLon - earthLon;
        return planetLon + Math.atan2(
                ( earthRad * Math.sin( delta ) ), ( planetRadius - earthRad * Math.cos( delta ) )
        );
    }

    /**
     * @param daysSinceJ2010: the day since J2010
     * @return the Earth Constants, the radius and the longitude
     */
    private double[] getEarthConstants( double daysSinceJ2010 )
    {
        double earthMeanAnomaly = ANGULAR_SPEED * daysSinceJ2010 / EARTH.revolutionPeriod + EARTH.deltaLon;
        double earthTrueAnomaly = earthMeanAnomaly + 2 * EARTH.orbitEccentricity * Math.sin( earthMeanAnomaly );
        double earthRad = ( EARTH.halfOrbitMajorAxis * ( 1 - EARTH.eccentricitySquared ) )  /  ( 1 + EARTH.orbitEccentricity * Math.cos( earthTrueAnomaly ) );
        double earthLon = earthTrueAnomaly + EARTH.lonPerigee;
        return new double[] { earthRad, earthLon };
    }
}
