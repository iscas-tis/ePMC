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

import epmc.value.Value;

public final class ValueObject implements Value {
    public static boolean is(Value value) {
        return value instanceof ValueObject;
    }

    public static ValueObject as(Value value) {
        if (is(value)) {
            return (ValueObject) value;
        } else {
            return null;
        }
    }

    private final static String SPACE = " ";

    private Object content;
    private final TypeObject type;

    ValueObject(TypeObject type) {
        assert type != null;
        this.type = type;
    }

    @Override
    public TypeObject getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject() {
        return (T) content;
    }

    public void set(Object content) {
        assert content == null ||
                getType().getUsedClass().isInstance(content) :
                    content + SPACE + content.getClass()
                    + SPACE + getType().getUsedClass();
                this.content = content;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueObject)) {
            return false;
        }
        ValueObject other = (ValueObject) obj;
        return content.equals(other.content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

    @Override
    public String toString() {
        return "value(" + content + ")";
    }
}
