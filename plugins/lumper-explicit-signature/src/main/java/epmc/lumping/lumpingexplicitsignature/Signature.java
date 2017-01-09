package epmc.lumping.lumpingexplicitsignature;

import epmc.value.ValueAlgebra;

final class Signature implements Comparable<Signature> {
    int[] blocks;
    ValueAlgebra[] values;
    int size;
    
    @Override
    public int compareTo(Signature other) {
        if (this.size < other.size) {
            return -1;
        } if (this.size > other.size) {
            return 1;
        }
        for (int i = 0; i < size; i++) {
            if (this.blocks[i] < other.blocks[i]) {
                return -1;
            } else if (this.blocks[i] > other.blocks[i]) {
                return 1;
            }
        }
        for (int i = 0; i < size; i++) {
            int cmp = this.values[i].compareTo(other.values[i]);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }
    
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
