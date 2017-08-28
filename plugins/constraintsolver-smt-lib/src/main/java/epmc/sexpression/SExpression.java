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

package epmc.sexpression;

import java.util.Arrays;

public final class SExpression {
    final static String LIST_OPEN = "(";
    final static String LIST_CLOSE = ")";
    final static String LIST_SEPARATE = " ";

    private final SExpression[] list;
    private final String atomic;

    SExpression(SExpression[] list, String atomic) {
        assert (list == null) != (atomic == null);
        this.list = list;
        this.atomic = atomic;
    }

    public boolean isAtomic() {
        return atomic != null;
    }

    public boolean isList() {
        return list != null;
    }

    public String getAtomic() {
        assert atomic != null;
        return atomic;
    }

    public int listSize() {
        assert list != null;
        return list.length;
    }

    public SExpression getChild(int childNr) {
        assert list != null;
        assert childNr >= 0 : childNr;
        assert childNr < list.length : childNr;
        return list[childNr];
    }

    @Override
    public String toString() {
        if (isAtomic()) {
            return getAtomic();
        } else if (isList()) {
            StringBuilder builder = new StringBuilder();
            builder.append(LIST_OPEN);
            for (SExpression child : list) {
                builder.append(child);
                builder.append(LIST_SEPARATE);
            }
            if (list.length > 0) {
                builder.delete(builder.length() - 1, builder.length());
            }
            builder.append(LIST_CLOSE);
            return builder.toString();
        } else {
            assert false;
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SExpression)) {
            return false;
        }
        SExpression other = (SExpression) obj;
        if (this.isAtomic() != other.isAtomic()) {
            return false;
        }
        if (isAtomic() && !this.atomic.equals(other.atomic)) {
            return false;
        }
        if (!Arrays.deepEquals(this.list, other.list)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = (isAtomic() ? 7 : 13) + (hash << 6) + (hash << 16) - hash;
        if (isAtomic()) {
            hash = atomic.hashCode() + (hash << 6) + (hash << 16) - hash;
        } else if (isList()) {
            hash = Arrays.deepHashCode(list) + (hash << 6) + (hash << 16) - hash;
        } else {
            assert false;
        }
        return hash;
    }
}
