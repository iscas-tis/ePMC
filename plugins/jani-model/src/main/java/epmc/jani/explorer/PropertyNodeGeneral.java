package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.graph.explorer.Explorer;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeObject;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueEnum;
import epmc.value.ValueObject;

/**
 * Explorer node property for JANI explorers and their components.
 * Note that this class stores values only temporarily for a given call of
 * {@link Explorer#queryNode(epmc.graph.ExplorerNode)}.
 * 
 * @author Ernst Moritz Hahn
 */
public final class PropertyNodeGeneral implements PropertyNode {
	/** The explorer to which this property belongs. */
	private final Explorer explorer;
	/** Type of value stored. */
	private final Type type;
	/** Value of this property for the node queried last. */
	private final Value value;

	/**
	 * Construct new node property.
	 * None of the parameters may be {@code null}.
	 * 
	 * @param explorer explorer to which the property shall belong to
	 * @param type type of the property
	 */
	public PropertyNodeGeneral(Explorer explorer, Type type) {
		assert explorer != null;
		assert type != null;
		this.explorer = explorer;
		this.type = type;
		this.value = type.newValue();
	}

	@Override
	public Explorer getExplorer() {
		return explorer;
	}

	/**
	 * Set value of node property.
	 * The parameter may not be {@code null}.
	 * 
	 * @param value value to set
	 */
	public void set(Value value) {
		assert value != null;
		this.value.set(value);
	}

	/**
	 * Set boolean value of node property.
	 * The property type must be boolean for this function to be allowed to be
	 * called.
	 * 
	 * @param value boolean value to set for this property
	 */
	public void set(boolean value) {
		assert TypeBoolean.isBoolean(type);
		ValueBoolean.asBoolean(this.value).set(value);
	}
	
	public void set(Object object) {
		assert object != null;
		assert TypeObject.isObject(type) : type;
		ValueObject.asObject(value).set(object);
	}
	
	public void set(Enum<?> value) {
		assert value != null;
		assert TypeEnum.isEnum(type);
		ValueEnum.asEnum(this.value).set(value);
	}

	@Override
	public Value get() throws EPMCException {
		return value;
	}

	@Override
	public Type getType() {
		return type;
	}
}
