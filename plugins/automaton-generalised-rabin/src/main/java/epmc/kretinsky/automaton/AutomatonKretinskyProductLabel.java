package epmc.kretinsky.automaton;


import java.util.Arrays;

import epmc.automaton.AutomatonLabelUtil;
import epmc.error.EPMCException;

public final class AutomatonKretinskyProductLabel implements AutomatonGeneralisedRabinLabel, AutomatonLabelUtil {
    private final AutomatonKretinskyProduct observer;
    private int number;
    private AutomatonKretinskyProductState state;
    private AutomatonLabelUtil[] succLabel;
    
    public AutomatonKretinskyProductLabel(
            AutomatonKretinskyProduct observerKretinskyProduct, AutomatonKretinskyProductState current, AutomatonLabelUtil[] succLabel) {
        this.state = current;
        this.observer = observerKretinskyProduct;
        this.succLabel = succLabel;
    }

    @Override
    public boolean isAccepting(int pair, int number) throws EPMCException {
        int[] acceptance = observer.getAcceptance(pair);
        int slaveNr = observer.acceptanceToSlaveNumber(pair, number);
        AutomatonSlaveLabel label = (AutomatonSlaveLabel) succLabel[slaveNr + 1];
        boolean[] test = observer.getStateAcc(pair, slaveNr);
        return label.isSuccess(test, acceptance[slaveNr]);
    }

    @Override
    public boolean isStable(int pair) throws EPMCException {
        if (!state.isStable(pair)) {
            return false;
        }

        int[] acceptance = observer.getAcceptance(pair);
        int numAccepting = observer.getNumAccepting(pair);
        for (int number = 0; number < numAccepting; number++) {
            int slaveNr = observer.acceptanceToSlaveNumber(pair, number);
            AutomatonSlaveLabel label = (AutomatonSlaveLabel) succLabel[slaveNr + 1];
            boolean[] test = observer.getStateAcc(pair, slaveNr);
            if (label.isFailOrBuy(test, acceptance[slaveNr])) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = Arrays.hashCode(succLabel) + (hash << 6) + (hash << 16) - hash;
        hash = state.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AutomatonKretinskyProductLabel)) {
            return false;
        }
        AutomatonKretinskyProductLabel other = (AutomatonKretinskyProductLabel) obj;
        if (!state.equals(other.state)) {
            return false;
        }
        if (!Arrays.equals(succLabel, other.succLabel)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }
    
    public void toString(StringBuilder builder) {
        try {
            for (int pair = 0; pair < observer.getNumPairs(); pair++) {
                builder.append(Arrays.toString(observer.getAcceptance(pair)));
                builder.append(" : ");
                builder.append(isStable(pair) ? "S " : "U ");
                int numAccepting = observer.getNumAccepting(pair);
                for (int accNr = 0; accNr < numAccepting; accNr++) {
                    builder.append(isAccepting(pair, accNr) ? "t" : "f");
                }
                if (pair < observer.getNumPairs() - 1) {
                    builder.append(", ");
                }
            }
        } catch (Throwable e) {
            builder.append(e);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }
}
