package epmc.imdp.lump.signature;

import java.util.Comparator;

import epmc.error.EPMCException;
import epmc.value.Value;

final class ActionSignatureIdentityComparator implements Comparator<ActionSignature>{

	@Override
	public int compare(ActionSignature o1, ActionSignature o2) {
		int o1Hash = System.identityHashCode(o1);
		int o2Hash = System.identityHashCode(o2);
		if (o1Hash < o2Hash) {
			return -1;
		}
		if (o1Hash > o2Hash) {
			return 1;
		}
		if (o1 == o2) {
			return 0;
		}
		
		int o1Size = o1.getSize();
		int o2Size = o2.getSize();
		if (o1Size < o2Size) {
			return -1;
		}
		if (o1Size > o2Size) {
			return 1;
		}
		for (int number = 0; number < o1Size; number++) {
			int o1Block = o1.getBlock(number);
			int o2Block = o2.getBlock(number);
			if (o1Block != o2Block) {
				return Integer.compare(o1Block, o2Block);
			}
		}
		for (int number = 0; number < o1Size; number++) {
			try {
				Value o1Value = o1.getValue(number);
				Value o2Value = o2.getValue(number);
				if (!o1Value.isEq(o2Value)) {
					return o1Value.compareTo(o2Value);
				}
			} catch (EPMCException e) {
				throw new RuntimeException(e);
			}
		}
		return 0;
	}

}
