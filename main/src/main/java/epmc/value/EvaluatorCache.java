package epmc.value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import epmc.operator.Operator;

public final class EvaluatorCache {
    private final static class Key {
        Operator operator;
        Type[] types;
        
        @Override
        public int hashCode() {
            int hash = 0;
            hash = operator.hashCode() + (hash << 6) + (hash << 16) - hash;
            hash = Arrays.hashCode(types) + (hash << 6) + (hash << 16) - hash;
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if (operator != other.operator) {
                return false;
            }
            if (!Arrays.equals(types, other.types)) {
                return false;
            }
            return true;
        }
    }
    
    private final Map<Key,OperatorEvaluator> map = new HashMap<>();
    private final Key testKey = new Key();
    private final ContextValue contextValue = ContextValue.get();
    
    public OperatorEvaluator getEvaluator(Operator operator, Type...types) {
        testKey.operator = operator;
        testKey.types = types;
        OperatorEvaluator result = map.get(testKey);
        if (result == null) {
            result = contextValue.getEvaluator(operator, types);
            Key newKey = new Key();
            newKey.operator = operator;
            newKey.types = types;
            map.put(newKey, result);
        }
        return result;
    }
}
