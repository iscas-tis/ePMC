package epmc.param.value.dag;

import java.math.BigInteger;
import java.util.ArrayList;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

final class NumberStore {
    private final static int INVALID = -1;
    
    private final ArrayList<BigInteger> numbersList = new ArrayList<>();
    private final Object2IntOpenHashMap<BigInteger> numbersMap = new Object2IntOpenHashMap<>();

    NumberStore() {
        numbersMap.defaultReturnValue(INVALID);
    }
    
    BigInteger getNumber(int number) {
        return numbersList.get(number);
    }

    public int getIndex(BigInteger value) {
        int result = numbersMap.getInt(value);
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
