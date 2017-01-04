package epmc.algorithms.explicit;

import epmc.error.EPMCException;
import epmc.util.BitSet;

public interface EndComponents {
    public BitSet next() throws EPMCException;
}
