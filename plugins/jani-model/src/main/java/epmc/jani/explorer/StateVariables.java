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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import epmc.expression.Expression;
import epmc.value.TypeNumBitsKnown;

public final class StateVariables {
    private final List<StateVariable> variables = new ArrayList<>();
    private final List<StateVariable> variablesExternal = Collections.unmodifiableList(variables);
    private final Map<Expression,Integer> numberMap = new LinkedHashMap<>();
    private int numBits;

    public int add(StateVariable variable) {
        assert variable != null;
        int number = variables.size();
        variables.add(variable);
        numberMap.put(variable.getIdentifier(), number);
        if (variable.isPermanent()) {
            if (TypeNumBitsKnown.getNumBits(variable.getType()) == TypeNumBitsKnown.UNKNOWN
                    || numBits == Integer.MAX_VALUE) {
                numBits = Integer.MAX_VALUE;
            } else {
                numBits += TypeNumBitsKnown.getNumBits(variable.getType());
            }
        }
        return number;
    }

    public int getVariableNumber(Expression identifier) {
        assert identifier != null;
        assert numberMap != null;
        assert numberMap.containsKey(identifier) : identifier;
        return numberMap.get(identifier);
    }

    public Expression[] getIdentifiersArray() {
        Expression[] result = new Expression[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            result[i] = variables.get(i).getIdentifier();
        }
        return result;
    }

    public List<Expression> getVariableIdentifiers() {
        List<Expression> result = new ArrayList<>();
        for (StateVariable variable : variables) {
            result.add(variable.getIdentifier());
        }
        return result;
    }

    public int getNumBits() {
        return numBits;
    }

    public boolean contains(Expression identifier) {
        assert identifier != null;
        return numberMap.containsKey(identifier);
    }

    public StateVariable get(int number) {
        assert number >= 0;
        assert number < variables.size();
        return variables.get(number);
    }

    public StateVariable get(Expression expression) {
        assert expression != null;
        assert numberMap.containsKey(expression) : expression + " " + numberMap;
        int number = numberMap.get(expression);
        return variables.get(number);
    }

    public List<StateVariable> getVariables() {
        return variablesExternal;
    }
}
