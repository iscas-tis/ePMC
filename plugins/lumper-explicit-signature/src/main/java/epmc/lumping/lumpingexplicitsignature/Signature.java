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

package epmc.lumping.lumpingexplicitsignature;

import epmc.value.ValueAlgebra;

final class Signature {
    int[] blocks;
    ValueAlgebra[] values;
    int size;

    @Override
    public int hashCode() {
        int hash = 0;
        hash = size + (hash << 6) + (hash << 16) - hash;
        for (int i = 0; i < size; i++) {
            hash = blocks[i] + (hash << 6) + (hash << 16) - hash;
        }
        for (int i = 0; i < size; i++) {
            hash = values[i].hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Signature)) {
            return false;
        }
        Signature other = (Signature) obj;
        if (this.size != other.size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (this.blocks[i] != other.blocks[i]) {
                return false;
            }
        }
        for (int i = 0; i < size; i++) {
            if (!this.values[i].equals(other.values[i])) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        for (int i = 0; i < size; i++) {
            result.append(blocks[i]);
            result.append("=");
            result.append(values[i]);
            if (i < size - 1) {
                result.append(",");
            }
        }
        result.append("}");
        return result.toString();
    }
}
