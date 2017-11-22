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

package epmc.jani.measure;

import epmc.value.Type;
import epmc.value.Value;

public final class DiscreteMeasureValue implements DiscreteMeasure, Value {
    private DiscreteMeasure[] operands;

    @Override
    public Type getEntryType() {
        return null;
        /// TODO
    }

    private Type[] getOperandsEntryTypes() {
        Type[] result = new Type[operands.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = operands[i].getEntryType();
        }
        return result;
    }

    @Override
    public void getTotal(Value total) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getFrom() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getTo() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void getValue(Value value, int of) {
        // TODO Auto-generated method stub

    }

    @Override
    public Value clone() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type getType() {
        // TODO Auto-generated method stub
        return null;
    }

    public void add(Value operand1, Value operand2) {
        //		this.operator = ContextValue.get().getOperator(OperatorAdd.IDENTIFIER);
        this.operands = new DiscreteMeasure[2];
        this.operands[0] = (DiscreteMeasure) operand1; // TODO castorimport
        this.operands[1] = (DiscreteMeasure) operand2;
    }
}
