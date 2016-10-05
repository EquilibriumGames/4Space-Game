package ebon.celestial;

import ebon.celestial.manager.*;
import flounder.maths.vectors.*;

import java.util.*;

/**
 * A realistic celestial object, like a planet / moon.
 */
public class Celestial implements Comparable<Celestial> {
	public static double EARTH_MASS = 5.9723e+24; // The earths mass (kg).
	public static double EARTH_RADIUS = 6378.137; // The earths radius (km).
	public static double EARTH_ESCAPE_VELOCITY = 11.186; // The earths escape velocity (km/s).
	public static double EARTH_DENSITY = 5.4950; // The earths density (g/cm^3).
	public static double EARTH_GRAVITY = 9.798; // The earths gravity (m/s/s).

	private String celestialType;

	private String planetName;
	private Vector3f position;
	private Vector3f rotation;

	private Star parentStar;
	private Celestial parentCelestial;
	private Orbit orbit;

	private List<Celestial> childObjects;

	private double earthMasses; // The planets earth mass.
	private double earthRadius; // The planets earth radius.
	private double density; // The planets density (g/cm^3).
	private double gravity; // The planets gravity (m/s^2).

	private double axialTilt; // How tilted over the planet is, rotates prograde 0<i<90, rotates retrograde 90<i<180.
	private double axialTropics; // The positions of the tropics, +/-.
	private double axialPolar; // The positions of the polar caps, +/-.

	private double minRingSpawns; // The ring rule min bounds (Earth radius) += 0.2.
	private double maxRingSpawns; // The ring rule max bounds (Earth radius) += 0.2.

	private double escapeVelocity; // The planets escape velocity (km/s).
	private double hillSphere; // The planets hill sphere (Planetary radii).

	/**
	 * Creates a new celestial object from earth masses and radius. Then calculates characteristics.
	 *
	 * @param celestialType The type of celestial body.
	 * @param planetName The celestial objects name.
	 * @param parentStar The celestial objects parent star.
	 * @param orbit The orbit for the celestial object to follow.
	 * @param earthMasses The mass of the object in earth masses.
	 * @param earthRadius The radius of the object in earth radius.
	 * @param axialTilt How tilted over the planet is, rotates prograde 0<i<90, rotates retrograde 90<i<180.
	 * @param childObjects The list of objects orbiting the star.
	 */
	public Celestial(String celestialType, String planetName, Star parentStar, Orbit orbit, double earthMasses, double earthRadius, double axialTilt, List<Celestial> childObjects) {
		this.parentStar = parentStar;
		init(celestialType, planetName, orbit, earthMasses, earthRadius, axialTilt, childObjects);
	}

	/**
	 * Creates a new celestial object from earth masses and radius. Then calculates characteristics.
	 *
	 * @param celestialType The type of celestial body.
	 * @param planetName The celestial objects name.
	 * @param parentCelestial The celestial objects parent celestial.
	 * @param orbit The orbit for the celestial object to follow.
	 * @param earthMasses The mass of the object in earth masses.
	 * @param earthRadius The radius of the object in earth radius.
	 * @param axialTilt How tilted over the planet is, rotates prograde 0<i<90, rotates retrograde 90<i<180.
	 * @param childObjects The list of objects orbiting the star.
	 */
	public Celestial(String celestialType, String planetName, Celestial parentCelestial, Orbit orbit, double earthMasses, double earthRadius, double axialTilt, List<Celestial> childObjects) {
		this.parentCelestial = parentCelestial;
		init(celestialType, planetName, orbit, earthMasses, earthRadius, axialTilt, childObjects);
	}

	private void init(String celestialType, String planetName, Orbit orbit, double earthMasses, double earthRadius, double axialTilt, List<Celestial> childObjects) {
		this.celestialType = celestialType;

		this.planetName = planetName;
		this.position = new Vector3f();
		this.rotation = new Vector3f();

		this.orbit = orbit;

		this.childObjects = childObjects;

		this.earthMasses = earthMasses;
		this.earthRadius = earthRadius;
		this.density = (earthMasses * EARTH_MASS * 1.0e-12) / ((4.0 * Math.PI * Math.pow(earthRadius * EARTH_RADIUS, 3)) / 3.0);
		this.gravity = earthMasses / (earthRadius * earthRadius) * EARTH_GRAVITY;

		this.axialTilt = axialTilt;
		this.axialTropics = axialTilt;
		this.axialPolar = 90.0 - axialTilt;

		this.minRingSpawns = 1.34 * earthRadius;
		this.maxRingSpawns = 2.44 * earthRadius;

		this.escapeVelocity = Math.sqrt(earthMasses / earthRadius) * EARTH_ESCAPE_VELOCITY;
		this.hillSphere = (orbit.getSemiMajorAxis() * SpaceConversions.AU_TO_KM * (1.0 - orbit.getEccentricity()) * Math.cbrt((earthMasses * EARTH_MASS) / (3.0 * getParentMass()))) / (earthRadius * EARTH_RADIUS);
	}

	public void update() {
		// Position is calculated around the parent star and using the amount of seconds sense an arbitrary date.
		childObjects.forEach(Celestial::update);

		Vector3f parentPosition;

		if (parentStar != null) {
			parentPosition = parentStar.getPosition();
		} else {
			parentPosition = parentCelestial.getPosition();
		}

		position.set(parentPosition); // TODO: DO ORBIT.
		position.x += orbit.getSemiMajorAxis();
	}

	public String getPlanetName() {
		return planetName;
	}

	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public Orbit getOrbit() {
		return orbit;
	}

	public List<Celestial> getChildObjects() {
		return childObjects;
	}

	public double getEarthMasses() {
		return earthMasses;
	}

	/**
	 * Gets the parent star.
	 *
	 * @return The parent star.
	 */
	public Star getParentStar() {
		if (parentStar != null) {
			return parentStar;
		} else if (parentCelestial != null) {
			return parentCelestial.getParentStar();
		}

		return null;
	}

	/**
	 * Gets the parent celestial object.
	 *
	 * @return The parent celestial object.
	 */
	public Celestial getParentCelestial() {
		if (parentCelestial != null) {
			return parentCelestial.getParentCelestial();
		}

		return this;
	}

	/**
	 * Gets the mass of the parent object (Kg).
	 *
	 * @return The mass of the parent object.
	 */
	public double getParentMass() {
		if (parentStar != null) {
			return parentStar.getSolarMasses() * Star.SOL_MASS;
		} else if (parentCelestial != null) {
			return parentCelestial.getEarthMasses() * EARTH_MASS;
		}

		return 0.0;
	}

	public double getEarthRadius() {
		return earthRadius;
	}

	public double getDensity() {
		return density;
	}

	public double getGravity() {
		return gravity;
	}

	public double getAxialTilt() {
		return axialTilt;
	}

	public double getAxialTropics() {
		return axialTropics;
	}

	public double getAxialPolar() {
		return axialPolar;
	}

	public double getMinRingSpawns() {
		return minRingSpawns;
	}

	public double getMaxRingSpawns() {
		return maxRingSpawns;
	}

	public double getEscapeVelocity() {
		return escapeVelocity;
	}

	public double getHillSphere() {
		return hillSphere;
	}

	/**
	 * Calculates the roche limit (rigid) for a second object (Earth radius.)
	 *
	 * @param secondDensity The density (in g/cm^3) for the second object.
	 *
	 * @return The roche limit for the second object.
	 */
	public double getRocheLimit(double secondDensity) {
		return 1.26 * earthRadius * Math.cbrt(this.density / secondDensity);
	}

	/**
	 * Gets if the object could support life.
	 *
	 * @return If the object could support life.
	 */
	public boolean supportsLife() {
		// Gets the parent star.
		Star star = getParentStar();

		// If the parent object is not habitable, this will not be either.
		if (parentCelestial != null) {
			Celestial celestial = getParentCelestial();

			if (!orbitHabitable(celestial.orbit, star)) {
				return false;
			}
		}

		//if (!PlanetType.getType(density, gravity).equals(PlanetType.WATERY)) {
		//	return false;
		//}

		// Calculates if this object is habitable.
		return orbitHabitable(orbit, star) && (earthMasses >= 0.1 && earthMasses <= 3.5) && (earthRadius >= 0.5 && earthRadius <= 1.5) && (gravity >= 3.923 && gravity <= 15.691);
	}

	private boolean orbitHabitable(Orbit orbit, Star star) {
		return true; // orbit.getSemiMajorAxis() >= star.getHabitableMin() && orbit.getSemiMajorAxis() <= star.getHabitableMax() && orbit.getEccentricity() > 0.0 && orbit.getEccentricity() <= 0.3; // 0.2 is a better max eccentricity.
	}

	@Override
	public String toString() {
		return celestialType + "(" + planetName + " | " + PlanetType.getType(earthMasses).name() + " | " + Composition.getComposition(earthMasses, earthRadius) + ") [ \n    " +
				"earthMasses=" + earthMasses +
				", earthRadius=" + earthRadius +
				", density=" + density +
				", gravity=" + gravity +
				", supportsLife=" + supportsLife() +
				", axialTilt=" + axialTilt +
				", axialTropics=" + axialTropics +
				", axialPolar=" + axialPolar +
				", minRingSpawns=" + minRingSpawns +
				", maxRingSpawns=" + maxRingSpawns +
				", rocheLimit(Lun)=" + getRocheLimit(3.34) +
				", hillSphere=" + hillSphere +
				", escapeVelocity=" + escapeVelocity +
				", \n" + orbit.toString() + "\n]";
	}

	@Override
	public int compareTo(Celestial o) {
		return ((Double) orbit.getSemiMajorAxis()).compareTo(o.orbit.getSemiMajorAxis());
	}

	public enum PlanetType {
		BROWN_DWARF(0.75, 317.816, 25425.318),
		DWARF(10.0, 0.0001, 0.1),
		GASEOUS(44.625, 10.0, 317.816),
		TERRESTRIAL(44.625, 0.1, 10.0);

		public final double universeMakeup; // How much of the universe if made up of this celestial type.
		public final double minMass;
		public final double maxMass;

		PlanetType(double universeMakeup, double minMass, double maxMass) {
			this.universeMakeup = universeMakeup;
			this.minMass = minMass;
			this.maxMass = maxMass;
		}

		public static PlanetType getTypeMakeup(double celestialMakeup) {
			double currentMakeup = 0.0;

			for (PlanetType type : PlanetType.values()) {
				if (celestialMakeup <= currentMakeup) {
					return type;
				}

				currentMakeup += type.universeMakeup;
			}

			return TERRESTRIAL;
		}

		public static PlanetType getType(double mass) {
			for (PlanetType type : PlanetType.values()) {
				if (mass >= type.minMass && mass < type.maxMass) {
					return type;
				}
			}

			return TERRESTRIAL;
		}
	}

	public enum Composition {
		Fe(0.5588549741, 0.3679471704),
		Fe_MgSiO3(0.8689655632, 0.3883073293),
		MgSiO3(0.9981114932, 0.4650799771),
		Fe_MgSiO3_H2O(1.3537611415, 0.49649608725),
		H2O(1.554507269, 0.5196821954),
		H_He(2.937566453, 1.337881668),
		H(3.160781167, 1.400102741),
		NULL(0.0, 1.0);

		public static final Composition[] VALUES = Composition.values();

		public static final int INNER_START = 0;
		public static final int INNER_END = 4;
		public static final int OUTER_START = 5;
		public static final int OUTER_END = 6;

		public final double a;
		public final double b;

		/**
		 * A composition for a celestial body.
		 *
		 * @param a The a value in the equation (a + b * ln(x))
		 * @param b The b value in the equation (a + b * ln(x))
		 */
		Composition(double a, double b) {
			this.a = a;
			this.b = b;
		}

		/**
		 * Gets the composition that matches the planets mass and radii the most.
		 *
		 * @param earthMasses The planets mass in earth masses.
		 * @param earthRadii The planets radius in earth radii.
		 *
		 * @return The composition that matches the planet the most.
		 */
		public static Composition getComposition(double earthMasses, double earthRadii) {
			for (Composition c : VALUES) {
				if (c.getRadius(earthMasses) == earthRadii) { // TODO
					return c;
				}
			}

			return Composition.NULL;
		}

		/**
		 * Calculates a radius from a mass in earth masses.
		 *
		 * @param earthMasses The mass in earth masses.
		 *
		 * @return The radius in earth radii.
		 */
		public double getRadius(double earthMasses) {
			return a + (b * Math.log(earthMasses));
		}

		/**
		 * Calculates a mass from a radius in earth radii.
		 *
		 * @param earthRadii The mass in earth radii.
		 *
		 * @return The radius in earth masses.
		 */
		public double getMass(double earthRadii) {
			return Math.pow(Math.E, (earthRadii - a) / b);
		}
	}
}
