package epmc.jani.explorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import epmc.error.EPMCException;
import epmc.jani.model.component.Component;
import epmc.util.Util;

/**
 * Component converter class.
 * The purpose of this class is to instantiate the right component explorer
 * class for the given class implementing {@link Component}. This allows for a
 * modular design. Later on, it might also allow plugins to define new system
 * component types.
 * 
 * @author Ernst Moritz Hahn
 */
final class PreparatorComponentExplorer {
	/** Map from component class to corresponding component explorer class. */
	private final static List<Class<? extends ExplorerComponent>> COMPONENT_CLASSES;
	static {
		List<Class<? extends ExplorerComponent>> list = new ArrayList<>();
		list.add(ExplorerComponentSynchronisationVectors.class);
		list.add(ExplorerComponentAutomaton.class);
		list.add(ExplorerComponentParallel.class);
		list.add(ExplorerComponentRename.class);
		COMPONENT_CLASSES = Collections.unmodifiableList(list);
	}
	
	/**
	 * Prepare component explorer.
	 * None of the parameters may be {@code null}.
	 * 
	 * @param explorer explorer to which component to be converted belongs
	 * @param component component for which to obtain an explorer
	 * @return explorer for given component
	 * @throws EPMCException thrown in case of problems
	 */
	ExplorerComponent prepare(ExplorerJANI explorer, Component component) throws EPMCException {
		assert explorer != null;
		assert component != null;
		ExplorerComponent instance = null;
		for (Class<? extends ExplorerComponent> clazz : COMPONENT_CLASSES) {
			ExplorerComponent tryInstance = null;
			tryInstance = Util.getInstance(clazz);
			tryInstance.setExplorer(explorer);
			tryInstance.setComponent(component);
			if (tryInstance.canHandle()) {
				tryInstance.build();
				instance = tryInstance;
				break;
			}
		}
		assert instance != null; // TODO
		return instance;
	}
}
