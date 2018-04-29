package epmc.imdp.lump.signature;

final class StateSignature implements Cloneable {
    private int size;
    private ActionSignature[] actions;

    ActionSignature getActionSignature(int number) {
        assert number >= 0;
        assert number < size;

        return actions[number];
    }

    @Override
    protected StateSignature clone() {
        StateSignature clone = new StateSignature();
        clone.size = this.size;
        clone.actions = new ActionSignature[size];
        for (int actionNr = 0; actionNr < size; actionNr++) {
            clone.actions[actionNr] = this.actions[actionNr];
        }
        return clone;
    }

    public void setSize(int size) {
        assert size >= 0;
        int oldSize = this.size;
        if (size > oldSize) {
            actions = new ActionSignature[size];
        }
        this.size = size;
    }

    public void setActionSignature(int number, ActionSignature actionSignature) {
        actions[number] = actionSignature;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int number = 0; number < size; number++) {
            builder.append(actions[number]);
            builder.append(",");
        }
        builder.delete(builder.length() - 1, builder.length());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof StateSignature)) {
            return false;
        }
        StateSignature other = (StateSignature) obj;
        if (this.size != other.size) {
            return false;
        }
        for (int number = 0; number < size; number++) {
            if (this.actions[number] != other.actions[number]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = size + (hash << 6) + (hash << 16) - hash;
        for (int number = 0; number < size; number++) {
            hash = System.identityHashCode(actions[number]) + (hash << 6) + (hash << 16) - hash;        	
        }
        return hash;
    }
}
