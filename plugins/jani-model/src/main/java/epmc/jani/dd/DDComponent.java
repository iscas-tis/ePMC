package epmc.jani.dd;

import java.io.Closeable;
import java.util.List;
import java.util.Set;

import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.jani.model.component.Component;

/**
 * DD-based symbolic representation of a {@link Component}.
 * This class is responsible to build symbolic representations of
 * <ul>
 * <li>initial nodes,</li>
 * <li>transitions resulting from automata and their composition,</li>
 * <li>variables used (read and written) by the component</li>
 * </ul>
 * of the component, which result either directly (from automata) or indirectly
 * (from composition).
 * 
 * @author Ernst Moritz Hahn
 */
interface DDComponent extends Closeable {
	/**
	 * Set the graph to which this DD component belongs.
	 * The parameter may not be {@code null}.
	 * The function must be called exactly once before calling {@link #build()}.
	 * 
	 * @param graph graph to which this DD component belongs
	 */
	void setGraph(GraphDDJANI graph);
	
	/**
	 * Set component which shall be represented by this DD component.
	 * The parameter may not be {@code null}.
	 * The function must be called exactly once before calling {@link #build()}.
	 * The function may only be called for a parameter which is of the correct
	 * class implementing the translation. Thus, this function as well as the
	 * preparing previous function calls should be performed by
	 * {@link PreparatorDDComponent} which contains an according translation
	 * table.
	 * 
	 * @param component component to represent by this DD component
	 */
	void setComponent(Component component);
	
	/**
	 * Build this DD component.
	 * Before calling this function, {@link #setGraph(GraphDDJANI)} and {@link
	 * #setComponent(Component)} must have been called. The function must be
	 * called exactly once.
	 * 
	 * @throws EPMCException thrown in case of problems
	 */
	void build() throws EPMCException;

	/**
	 * Obtain the DD transitions of this DD component.
	 * Before calling this function, {@link #build()} must have been called
	 * without throwing an exception.
	 * 
	 * @return DD transitions of this DD component
	 */
	List<DDTransition> getTransitions();

	/**
	 * Obtain initial nodes of this DD component.
	 * The initial nodes should assign the according values to the local
	 * variables of this DD component, but not the global ones.
	 * Before calling this function, {@link #build()} must have been called
	 * without throwing an exception.
	 * 
	 * @return initial nodes of this DD component
	 */
	DD getInitialNodes();

	/**
	 * Get the cube of present state variables of the component.
	 * This includes local variables as well as variables to encode states of
	 * automata. It does not include global variables.
	 * 
	 * @return present state variables cube
	 */
	DD getPresCube();

	/**
	 * Get the cube of next state variables of the component.
	 * This includes local variables as well as variables to encode states of
	 * automata. It does not include global variables.
	 * 
	 * @return next state variables cube
	 */
	DD getNextCube();
	
	/**
	 * Get the set of DD variables of the component.
	 * This includes the local variables of automata, as well as variables to
	 * encode variables in automata. It does not include global variables.
	 * 
	 * @return variables of the component
	 */
	Set<VariableDD> getVariables();
	
	@Override
	void close();
}