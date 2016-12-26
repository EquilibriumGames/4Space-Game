package ebon.entities.editing;

import ebon.entities.*;
import ebon.entities.components.*;
import ebon.entities.loading.*;
import flounder.helpers.*;

import javax.swing.*;
import java.util.*;

public abstract class IEditorComponent {
	public static final IEditorComponent[] EDITOR_COMPONENTS = new IEditorComponent[]{
			new EditorAnimation((Entity) null),
			new EditorCollider((Entity) null),
			new EditorCollision((Entity) null),
			new EditorModel((Entity) null),
			new EditorParticleSystem((Entity) null),
			new EditorRemoveFade((Entity) null),
	};
	public static final List<Pair<String, JPanel>> ADD_SIDE_TAB = new ArrayList<>();
	public static final List<String> REMOVE_SIDE_TAB = new ArrayList<>();

	public abstract String getTabName();

	public abstract IEntityComponent getComponent();

	public abstract void addToPanel(JPanel panel);

	public abstract void update();

	/**
	 * Gets the list of values that are saved with the component.
	 *
	 * @param entityName The name of the save type, can be used to save extra files under /entities/name/*.
	 *
	 * @return Returns values saved with the component.
	 */
	public abstract Pair<String[], EntitySaverFunction[]> getSavableValues(String entityName);

	/**
	 * Creates a new text panel for the component.
	 *
	 * @return The new text panel.
	 */
	public static JPanel makeTextPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new WrapLayout());
		return panel;
	}
}
