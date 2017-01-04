package epmc.graph.dd;

import java.math.BigInteger;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.LowLevel;
import epmc.graph.StateSet;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueObject;

public interface GraphDD extends LowLevel {
    /* methods to be implemented by implementing classes. */

    ContextValue getContextValue();
    
    DD getInitialNodes() throws EPMCException;

    DD getTransitions() throws EPMCException;

    DD getPresCube();

    DD getNextCube();

    DD getActionCube();
    
    /* TODO get rid of following method */
    
    Permutation getSwapPresNext();
    
    DD getNodeSpace() throws EPMCException;

    @Override
    void close();
    
    GraphDDProperties getProperties();

    /* default methods */
    
    default Options getOptions() {
        return getContextValue().getOptions();
    }

    default <T> T getGraphPropertyObject(Object property) {
        ValueObject graphProperty = ValueObject.asObject(getGraphProperty(property));
        if (graphProperty == null) {
            return null;
        } else {
            return graphProperty.getObject();
        }
    }

    default void registerGraphProperty(Object property, Type type) {
        getProperties().registerGraphProperty(property, type);
    }
    
    default Value getGraphProperty(Object property) {
        return getProperties().getGraphProperty(property);
    }
    
    default DD getNodeProperty(Object property) throws EPMCException {
        return getProperties().getNodeProperty(property);
    }

    default Set<Object> getNodeProperties() {
        return getProperties().getNodeProperties();
    }

    default DD getEdgeProperty(Object property) throws EPMCException {
        return getProperties().getEdgeProperty(property);
    }

    default Set<Object> getGraphProperties() {
        return getProperties().getGraphProperties();
    }

    default void setGraphPropertyObject(Object property, Object value) {
        getProperties().setGraphPropertyObject(property, value);
    }

    default void setGraphProperty(Object property, Value value) throws EPMCException {
        getProperties().setGraphProperty(property, value);
    }

    default void registerNodeProperty(Object property, DD value) {
        getProperties().registerNodeProperty(property, value);
    }
    
    default void registerEdgeProperty(Object property, DD value) {
        getProperties().registerEdgeProperty(property, value);
    }

    default ContextDD getContextDD() throws EPMCException {
        return ContextDD.get(getContextValue());
    }

    @Override
    default StateSet newInitialStateSet() throws EPMCException {
        return new StateSetDD(this, getInitialNodes().clone());
    }

    default BigInteger getNumNodes() throws EPMCException {
        return getNodeSpace().countSat(getPresCube());
    }
    
    @Override
    default Type getType(Expression expression) throws EPMCException {
    	assert expression != null;
    	Value graphProperty = getGraphProperty(expression);
    	if (graphProperty != null) {
    		return graphProperty.getType();
    	}
    	DD nodeProperty = getNodeProperty(expression);
    	if (nodeProperty != null) {
    		return nodeProperty.getType();
    	}
    	DD edgeProperty = getEdgeProperty(expression);
    	if (edgeProperty != null) {
    		return edgeProperty.getType();
    	}
    	return null;
    }
}
