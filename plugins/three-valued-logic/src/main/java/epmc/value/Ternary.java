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

public enum Ternary implements Comparable<Ternary> {
    FALSE,
    UNKNOWN,
    TRUE;

    private final static String TRUE_STRING = "true";
    private final static String FALSE_STRING = "false";
    private final static String UNKNOWN_STRING = "unknown";

    public boolean isFalse() {
        return this == FALSE;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isKnown() {
        return this != UNKNOWN;
    }

    public boolean getBoolean() {
        if (isTrue()) {
            return true;
        } else if (isFalse()) {
            return false;
        } else {
            assert false;
        }
        return false;
    }

    @Override
    public String toString() {
        switch (this) {
        case FALSE:
            return FALSE_STRING;
        case TRUE:
            return TRUE_STRING;
        case UNKNOWN:
            return UNKNOWN_STRING;
        default:
            assert false;
            return null;
        }
    }
}
