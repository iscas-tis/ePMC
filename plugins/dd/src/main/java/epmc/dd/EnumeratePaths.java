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

package epmc.dd;

import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class EnumeratePaths {
    @FunctionalInterface
    public interface EnumerateSATCallback {
        void call(Value[] values);
    }

    private DD dd;
    private VariableDD[] variables;
    private EnumerateSATCallback callback;

    private Walker ddWalker;
    private Type[] types;
    private ValueBoolean[] callbackValues;
    private ValueBoolean[] values;
    private int[] variableMap;

    public void setBDD(DD dd) {
        assert dd != null;
        assert TypeBoolean.is(dd.getType());
        this.dd = dd;
    }

    public void setVariables(VariableDD[] variables) {
        assert variables != null;
        for (VariableDD variable : variables) {
            assert variable != null;
            assert TypeBoolean.is(variable.getType());
        }
        IntArrayList variableMap = new IntArrayList();
        for (int varNr = 0; varNr < variables.length; varNr++) {
            int ddVariable = variables[varNr].getDDVariables().get(0).variable();
            while (variableMap.size() <= ddVariable) {
                variableMap.add(-1);
            }
            variableMap.set(ddVariable, varNr);
        }
        this.variableMap = variableMap.toIntArray();
        this.variables = variables;
    }

    public void setCallback(EnumerateSATCallback callback) {
        assert callback != null;
        this.callback = callback;
    }

    public void enumerate() {
        values = new ValueBoolean[variables.length];   
        callbackValues = new ValueBoolean[variables.length];   
        types = new Type[variables.length];
        for (int varNr = 0; varNr < variables.length; varNr++) {
            TypeBoolean type = TypeBoolean.get();
            types[varNr] = type;
            values[varNr] = type.newValue();
        }

        ddWalker = dd.walker();
        recurse();
    }

    private void recurse() {
        if (ddWalker.isFalse()) {
            return;
        } else if (ddWalker.isTrue()) {
            terminalCase();
        } else {
            int ddVariable = ddWalker.variable();
            int variable = variableMap[ddVariable];
            ddWalker.low();
            callbackValues[variable] = values[variable];
            callbackValues[variable].set(false);
            recurse();
            ddWalker.back();
            ddWalker.high();
            callbackValues[variable].set(true);
            recurse();
            ddWalker.back();
            callbackValues[variable] = null;
        }
    }

    private void terminalCase() {
        callback.call(callbackValues);
    }
}
