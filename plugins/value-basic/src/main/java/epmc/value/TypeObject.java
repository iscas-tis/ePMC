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

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;

public final class TypeObject implements TypeNumBitsKnown {
    public static boolean is(Type type) {
        return type instanceof TypeObject;
    }

    public static TypeObject as(Type type) {
        if (type instanceof TypeObject) {
            return (TypeObject) type;
        } else {
            return null;
        }
    }

    public final static class Builder {
        private Class<?> clazz;
        private StorageType storageType = StorageType.DIRECT;

        public Builder setClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        private Class<?> getClazz() {
            return clazz;
        }

        public Builder setStorageClass(StorageType storageType) {
            this.storageType = storageType;
            return this;
        }

        private StorageType getStorageType() {
            return storageType;
        }

        public TypeObject build() {
            return ContextValue.get().makeUnique(new TypeObject(this));
        }
    }

    public enum StorageType {
        DIRECT,
        NUMERATED_NORMAL,
        NUMERATED_IDENTITY
    }

    private final Class<?> usedClass;
    private final StorageType storageType;

    private TypeObject(Builder builder) {
        assert builder != null;
        assert builder.getClazz() != null;
        this.usedClass = builder.getClazz();
        this.storageType = builder.getStorageType();
    }

    public Class<?> getUsedClass() {
        return usedClass;
    }

    @Override
    public ValueObject newValue() {
        return new ValueObject(this);
    }


    public ValueObject newValue(Object object) {
        assert object != null;
        ValueObject result = newValue();
        result.set(object);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        TypeObject other = (TypeObject) obj;
        if (this.usedClass != other.usedClass) {
            return false;
        }
        if (this.storageType != other.storageType) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = usedClass.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("object(");
        builder.append(usedClass);
        builder.append(",");
        builder.append(storageType);
        builder.append(")");
        return builder.toString();
    }

    @Override
    public int getNumBits() {
        return Integer.SIZE;
    }

    StorageType getStorageType() {
        return storageType;
    }

    boolean isDirect() {
        return storageType == StorageType.DIRECT;
    }

    boolean isNumeratedNormal() {
        return storageType == StorageType.NUMERATED_NORMAL;
    }

    boolean isNumeratedIdentity() {
        return storageType == StorageType.NUMERATED_IDENTITY;
    }

    @Override
    public TypeArray getTypeArray() {
        if (isDirect()) {
            return ContextValue.get().makeUnique(new TypeArrayObjectDirect(this));
        } else if (isNumeratedNormal()) {
            return ContextValue.get().makeUnique(new TypeArrayObjectNumerated(this, false));
        } else if (isNumeratedIdentity()) {
            return ContextValue.get().makeUnique(new TypeArrayObjectNumerated(this, true));
        }
        assert false : storageType;
        return null;
    }
}
