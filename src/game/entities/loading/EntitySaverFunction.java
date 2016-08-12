package game.entities.loading;

import java.io.*;

/**
 * A class capable of writing to a entity save file component section.
 */
public abstract class EntitySaverFunction {
	private String sectionName;

	/**
	 * Creates a new abstract save function.
	 *
	 * @param sectionName The sections classpath.
	 */
	public EntitySaverFunction(String sectionName) {
		this.sectionName = sectionName;
	}

	public abstract void writeIntoSection(EntityFileWriter entityFileWriter) throws IOException;

	/**
	 * Gets the sections classpath.
	 *
	 * @return The sections classpath.
	 */
	public String getSectionName() {
		return sectionName;
	}
}