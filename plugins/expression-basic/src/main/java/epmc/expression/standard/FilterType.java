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

package epmc.expression.standard;

import java.io.Serializable;

public enum FilterType implements Serializable {
    MIN("min", true),
    MAX("max", true),
    COUNT("count", true),
    SUM("sum", true),
    AVG("avg", true),
    FIRST("first", true),
    RANGE("range", true),
    FORALL("forall", true),
    EXISTS("exists", true),
    STATE("state", true),
    ARGMIN("argmin", false),
    ARGMAX("argmax",false),
    PRINT("print", false),
    PRINTALL("printall", false);

    private final String string;
    private final boolean singleValue;

    private FilterType(String string, boolean singleValue) {
        this.string = string;
        this.singleValue = singleValue;
    }

    @Override
    public String toString() {
        return string;
    }

    public boolean isSingleValue() {
        return singleValue;
    }
}
