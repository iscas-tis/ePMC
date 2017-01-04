package epmc.jani.explorer;

import java.util.Collection;

import epmc.error.EPMCException;
import epmc.graph.explorer.Explorer;
import epmc.graph.explorer.ExplorerNode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.component.Component;
import epmc.value.ContextValue;

/**
 * Explorer for a given system component.
 * Classes implementing this interface are intended to be used internally as
 * part of {@link ExplorerJANI}.
 * 
 * The reason to derive the interface from {@link Explorer} is to prepare
 * possible later on compositional lumping.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ExplorerComponent extends Explorer {
	void setExplorer(ExplorerJANI explorer);

	/**
	 * Get the model the component explorer is used for.
	 * 
	 * @return model the component explorer is used for
	 */
	ExplorerJANI getExplorer();
	
	/**
	 * Set the component the explorer is supposed to explore.
	 * This method shall be called once with a non-{@code null} parameter before
	 * {@link #build()} is called.
	 * 
	 * @param component component the explorer is supposed to explore
	 */
	void setComponent(Component component);
	
	/**
	 * Check whether explorer component can handle given model component.
	 * 
	 * @return whether explorer component can handle given model component
	 */
	boolean canHandle();
	
	/**
	 * Build the component explorer.
	 * Before calling this method, {@link #setExplorer(ModelJANI)} and
	 * {@link #setComponent(Component)} must have been called. After having
	 * called this model, the explorer is ready to explore the component.
	 * 
	 * @throws EPMCException thrown in case of problems
	 */
	void build() throws EPMCException;
	
	void buildAfterVariables() throws EPMCException;
	
	@Override
	NodeJANI newNode() throws EPMCException;

    /**
     * {@inheritDoc}
     * <p>
     * Note that for component explorers, the returned property is of a subclass
     * of {@link PropertyNodeGeneral}.
     * </p>
     */
	@Override
    PropertyNode getNodeProperty(Object property) throws EPMCException;

    /**
     * {@inheritDoc}
     * <p>
     * Note that for component explorers, the returned property is of a subclass
     * of {@link PropertyEdgeGeneral}.
     * </p>
     */
	@Override
	PropertyEdge getEdgeProperty(Object property) throws EPMCException;

	/**
     * {@inheritDoc}
     * <p>
     * Note that for component explorers, the returned collection can only
     * contain nodes of the according subclass of {@link NodeComponent}.
     * </p>
     */
	@Override
    Collection<NodeJANI> getInitialNodes() throws EPMCException;

	@Override
	default void queryNode(ExplorerNode node) throws EPMCException {
		assert node instanceof NodeJANI;
		queryNode((NodeJANI) node);
	}

    void queryNode(NodeJANI node) throws EPMCException;

    boolean isState(NodeJANI node) throws EPMCException;
    
    /**
     * {@inheritDoc}
     */
	@Override
	NodeJANI getSuccessorNode(int succNr);
	
	@Override
	default ContextValue getContextValue() {
		return getExplorer().getContextValue();
	}

	/**
	 * Set the number of successors the component is supposed to have.
	 * This function is meant to be called e.g. in case of there are no
	 * successors and self-loops shall be introduced to fix the model
	 * automatically.
	 * 
	 * @param numSuccessors number of successors the component shall have
	 */
	void setNumSuccessors(int numSuccessors);

	boolean isState() throws EPMCException;
}
