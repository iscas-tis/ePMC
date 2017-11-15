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

package epmc.value;

import epmc.operator.OperatorSet;
import epmc.value.Value;

public final class ValueArrayGenericAlgebra implements ValueArrayAlgebra {
    private final static String SPACE = " ";
    private final TypeArrayGenericAlgebra type;
    private final OperatorEvaluator set;
    private ValueAlgebra[] content;
    private int size;

    ValueArrayGenericAlgebra(TypeArrayGenericAlgebra type) {
        this.type = type;
        this.content = new ValueAlgebra[0];
        set = ContextValue.get().getEvaluator(OperatorSet.SET, type.getEntryType(), type.getEntryType());
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert index >= 0;
        assert index < size();
        set.apply(content[index], value);
    }

    @Override
    public void set(int entry, int index) {
        assert index >= 0;
        assert index < size();
        content[index].set(entry);
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert index >= 0 : index;
        assert index < size() : index + SPACE + size();
        set.apply(value, content[index]);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < size(); i++) {
            hash = content[i].hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public TypeArrayGenericAlgebra getType() {
        return type;
    }

    @Override
    public void setSize(int size) {
        assert size >= 0;
        TypeAlgebra entryType = getType().getEntryType();
        this.content = new ValueAlgebra[size];
        for (int index = 0; index < size; index++) {
            this.content[index] = entryType.newValue();
        }
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return UtilValue.arrayToString(this);
    }
}
