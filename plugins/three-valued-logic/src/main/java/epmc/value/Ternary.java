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
            return "false";
        case TRUE:
            return "true";
        case UNKNOWN:
            return "unknown";
        default:
            assert false;
            return null;
        }
    }
    
    public boolean up() {
        if (isTrue() || isUnknown()) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean down() {
        if (isFalse() || isUnknown()) {
            return false;
        } else {
            return true;
        }
    }
    
    public Ternary and(Ternary other) {
        assert other != null;
        if (this.isTrue() && other.isTrue()) {
            return TRUE;
        } else if (this.isFalse() || other.isFalse()) {
            return FALSE;
        } else {
            return UNKNOWN;
        }
    }
    
    public Ternary or(Ternary other) {
        assert other != null;
        if (this.isTrue() || other.isTrue()) {
            return TRUE;
        } else if (this.isFalse() && other.isFalse()) {
            return FALSE;
        } else {
            return UNKNOWN;
        }
    }

    public Ternary not() {
        if (isTrue()) {
            return FALSE;
        } else if (isFalse()) {
            return TRUE;
        } else if (isUnknown()){
            return UNKNOWN;
        } else {
            assert false;
            return null;
        }
    }

    public Ternary implies(Ternary other) {
        assert other != null;
        if (this.isFalse() || other.isTrue()) {
            return TRUE;
        } else if (this.isTrue() && other.isFalse()) {
            return FALSE;
        } else {
            return UNKNOWN;
        }
    }
    
    public Ternary iff(Ternary other) {
        assert other != null;
        if (this.isKnown() && other.isKnown()) {
            return this == other ? TRUE : FALSE;
        } else {
            return UNKNOWN;
        }
    }

}
