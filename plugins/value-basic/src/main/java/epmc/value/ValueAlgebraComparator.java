package epmc.value;

import java.util.Comparator;

import epmc.error.EPMCException;

public final class ValueAlgebraComparator implements Comparator<ValueAlgebra> {

	@Override
	public int compare(ValueAlgebra o1, ValueAlgebra o2) {
        try {
            if (o1.isEq(o2)) {
                return 0;
            } else if (o1.isLt(o2)) {
                return -1;
            } else {
                assert o1.isGt(o2);
                return 1;
            }
        } catch (EPMCException e) {
            throw new RuntimeException(e);
        }
	}
}
