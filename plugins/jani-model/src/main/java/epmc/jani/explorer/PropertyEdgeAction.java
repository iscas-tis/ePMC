/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.jani.explorer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import epmc.jani.model.Action;
import epmc.jani.model.ModelJANI;
import epmc.value.Type;
import epmc.value.TypeObject;
import epmc.value.Value;
import epmc.value.ValueObject;
import epmc.value.TypeObject.StorageType;

public final class PropertyEdgeAction implements PropertyEdge {
    private final Map<Action,Integer> actionToNumber = new HashMap<>();
    private final Action[] numberToAction;
    private final TypeObject type;
    private final ValueObject value;
    private int[] values = new int[1];

    PropertyEdgeAction(ExplorerJANI explorer) {
        assert explorer != null;
        ModelJANI model = explorer.getModel();
        numberToAction = new Action[model.getActionsOrEmpty().size() + 1];
        int actionNumber = 0;
        actionToNumber.put(model.getSilentAction(), actionNumber);
        numberToAction[actionNumber] = model.getSilentAction();
        actionNumber++;
        for (Action action : model.getActionsOrEmpty()) {
            actionToNumber.put(action, actionNumber);
            numberToAction[actionNumber] = action;
            actionNumber++;
        }
        type = new TypeObject.Builder()
                .setClazz(Action.class)
                .setStorageClass(StorageType.NUMERATED_IDENTITY)
                .build();
        value = type.newValue();
    }

    @Override
    public Value get(int successor) {
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

    public void set(int successor, Object value) {
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
    
    @Override
    public String toString() {
        return actionToNumber.toString();
    }
}
