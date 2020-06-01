package ch.epfl.rigel.astronomy;

import ch.epfl.rigel.coordinates.EclipticCoordinates;
import ch.epfl.rigel.coordinates.EquatorialCoordinates;

/**
 * Represents the Sun at a given moment
 */
public final class Sun extends CelestialObject
{
    // magnitude of the sun
    private static final float MAGNITUDE = -26.7f;

    // save the ecliptic position and the mean anomaly
    private final EclipticCoordinates eclipticPos;
    private final float meanAnomaly;

    /**
     * throws NullPointerException if the ecliptic position is null
     */
    public Sun( EclipticCoordinates eclipticPos, EquatorialCoordinates equatorialPos, float angularSize, float meanAnomaly )
    {
        super( "Soleil", equatorialPos, angularSize, MAGNITUDE );
        if ( eclipticPos == null )
        {
            throw new NullPointerException( "EclipticPos is NotNull" );
        }

        this.eclipticPos = eclipticPos;
        this.meanAnomaly = meanAnomaly;
    }

    protected void setInfo(SunInfo info) {
        super.setInfo("");
    }

    /**
     * @return the ecliptic position
     */
    public EclipticCoordinates eclipticPos() { return eclipticPos; }

    /**
     * @return the mean anomaly
     */
    public double meanAnomaly() { return meanAnomaly; }
}
