package epmc.kretinsky.automaton;

import java.util.Arrays;

import epmc.automaton.AutomatonStateUtil;
import epmc.dd.DD;
import epmc.error.EPMCException;

final class AutomatonSlaveState implements AutomatonStateUtil {
    private final AutomatonSlave observer;
    private final int[] content;
    private int number;

    AutomatonSlaveState(AutomatonSlave observer, int[] content) {
        this.observer = observer;
        int usedLength = content.length;
        while (usedLength > 0 && content[usedLength - 1] == Integer.MAX_VALUE) {
            usedLength--;
        }
        this.content = Arrays.copyOf(content, usedLength);
    }

    AutomatonSlaveState(AutomatonSlaveState other) {
        this(other.getAutomaton(), other.content);
    }
    
    @Override
    protected AutomatonStateUtil clone() {
        return new AutomatonSlaveState(this);
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof AutomatonSlaveState)) {
            return false;
        }
        AutomatonSlaveState other = (AutomatonSlaveState) obj;
        return Arrays.equals(content, other.content);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < content.length; i++) {
            int value = content[i];
            if (value < Integer.MAX_VALUE) {
                builder.append(value);
            } else {
                builder.append("\u22A5");
            }
            if (i < content.length - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
        
        return builder.toString();
    }

    AutomatonSlaveState disable() {
        return getAutomaton().makeUnique(new AutomatonSlaveState(getAutomaton(), new int[0]));
    }
    
    int get(int position) {
        if (position < content.length) {
            return content[position];
        } else {
            return Integer.MAX_VALUE;
        }
    }
    
    int size() {
        return content.length;
    }
    
    @Override
    public AutomatonSlave getAutomaton() {
        return this.observer;
    }
    
    AutomatonMojmir getObserverMojmir() {
        return getAutomaton().getMojmir();
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }
    
    public int[] getContent() {
        return content;
    }

    public DD getDDAssignsRank(int rank) throws EPMCException {
        DD result = observer.getContextDD().newConstant(true);
        for (int i = 0; i < content.length; i++) {
            if (content[i] == -1 || content[i] > rank) {
                continue;
            }
            DD stateDD = observer.getMojmir().getStateExpressionDD(i);
            result = result.andWith(stateDD.clone());
        }
        
        return result;
    }
}
