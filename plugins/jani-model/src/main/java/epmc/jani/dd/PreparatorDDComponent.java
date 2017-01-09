package epmc.jani.dd;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;
import epmc.jani.model.component.ComponentParallel;
import epmc.jani.model.component.ComponentRename;

/**
 * Translate a JANI system component to a DD component.
 * For this, for each class implementing {@link Component}, the according class
 * implementing {@link DDComponent} has to be found and instantiated.
 * Afterwards, the instantiated DD component will build a symbolic
 * representation of the component.
 * 
 * @author Ernst Moritz Hahn
 */
final class PreparatorDDComponent {
	/** Map from component class to corresponding component explorer class. */
	private final static Map<Class<? extends Component>, Class<? extends DDComponent>> MAP;
	static {
		Map<Class<? extends Component>, Class<? extends DDComponent>> map = new LinkedHashMap<>();
		map.put(ComponentAutomaton.class, DDComponentAutomaton.class);
		map.put(ComponentParallel.class, DDComponentParallel.class);
		map.put(ComponentRename.class, DDComponentRename.class);
		MAP = Collections.unmodifiableMap(map);
	}
	
	/**
	 * Prepare DD component.
	 * None of the parameters may be {@code null}.
	 * 
	 * @param graph graph to which component to be converted belongs
	 * @param component component for which to obtain an explorer
	 * @return DD representation of given component
	 * @throws EPMCException thrown in case of problems
	 */
	DDComponent prepare(GraphDDJANI graph, Component component) throws EPMCException {
		assert graph != null;
		assert component != null;
		Class<? extends DDComponent> clazz = MAP.get(component.getClass());
		DDComponent instance = null;
		try {
			instance = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			assert false;
			return null;
		}
		instance.setGraph(graph);
		instance.setComponent(component);
		instance.build();
		return instance;
	}
}
