package epmc.jani.explorer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.graph.explorer.Explorer;
import epmc.jani.model.Action;
import epmc.jani.model.ModelJANI;
import epmc.value.Type;
import epmc.value.TypeObject;
import epmc.value.Value;
import epmc.value.ValueObject;
import epmc.value.TypeObject.StorageType;

public final class PropertyEdgeAction implements PropertyEdge {
	private final ExplorerJANI explorer;
	private final Map<Action,Integer> actionToNumber = new HashMap<>();
	private final Action[] numberToAction;
	private final TypeObject type;
	private final ValueObject value;
	private int[] values = new int[1];
	
	PropertyEdgeAction(ExplorerJANI explorer) {
		this.explorer = explorer;
		ModelJANI model = explorer.getModel();
		numberToAction = new Action[model.getActions().size() + 1];
		int actionNumber = 0;
		actionToNumber.put(model.getSilentAction(), actionNumber);
		numberToAction[actionNumber] = model.getSilentAction();
		actionNumber++;
		for (Action action : model.getActions()) {
			actionToNumber.put(action, actionNumber);
			numberToAction[actionNumber] = action;
			actionNumber++;
		}
		type = new TypeObject.Builder()
                .setContext(model.getContextValue())
                .setClazz(Action.class)
                .setStorageClass(StorageType.NUMERATED_IDENTITY)
                .build();
		value = type.newValue();
	}
	
	@Override
	public Explorer getExplorer() {
		return explorer;
	}

	@Override
	public Value get(int successor) throws EPMCException {
		Action action = numberToAction[values[successor]];
		value.set(action);
		return value;
	}
	
	public int getInt(int successor) {
		return values[successor];
	}

	@Override
	public Type getType() {
		return type;
	}

	public void set(int successor, Object value) throws EPMCException {
		assert value instanceof Action : value + " " + value.getClass();
		assert actionToNumber.containsKey(value);
		ensureSuccessorsSize(successor);
		int actionNumber = actionToNumber.get(value);
		values[successor] = actionNumber;
	}

	void set(int successor, int value) {
		ensureSuccessorsSize(successor);
		values[successor] = value;
	}

	private void ensureSuccessorsSize(int successor) {
		int numSuccessors = successor + 1;
		if (numSuccessors < values.length) {
			return;
		}
		int newLength = values.length;
		while (newLength <= numSuccessors) {
			newLength *= 2;
		}
		int[] newValues = Arrays.copyOf(values, newLength);
		values = newValues;
	}
}
