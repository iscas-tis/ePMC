package epmc.param.value.dag;

import java.math.BigInteger;
import java.util.ArrayList;

import gnu.trove.map.hash.TObjectIntHashMap;

final class NumberStore {
    private final static int INVALID = -1;
    
    private final ArrayList<BigInteger> numbersList = new ArrayList<>();
    private final TObjectIntHashMap<BigInteger> numbersMap = new TObjectIntHashMap<>(100, 0.5f, INVALID);

    BigInteger getNumber(int number) {
        return numbersList.get(number);
    }

    public int getIndex(BigInteger value) {
        int result = numbersMap.get(value);
        if (result != INVALID) {
            return result;
        }
        result = numbersList.size();
        numbersMap.put(value, result);
        numbersList.add(value);
        return result;
    }

    // TODO
    public void sendStatistics() {
    }
}
