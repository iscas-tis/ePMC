package epmc.expression.evaluatorexplicit;

import java.util.HashMap;
import java.util.Map;

import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit.EvaluatorCacheEntry;

public final class EvaluatorCache {
    private final Map<EvaluatorCacheEntry,EvaluatorExplicit> map = new HashMap<>();
    private final Map<Object,Object> auxMap = new HashMap<>();
    
    public EvaluatorCache() {
    }
    
    public void put(EvaluatorCacheEntry key,EvaluatorExplicit value) {
        map.put(key, value);
    }

    public EvaluatorExplicit get(EvaluatorCacheEntry key) {
        return map.get(key);
    }
    
    public void putAux(Object key, Object value) {
        auxMap.put(key, value);
    }
    
    public Object getAux(Object key) {
        return auxMap.get(key);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(map);
        builder.append("\n");
        builder.append(auxMap);
        return builder.toString();
    }
}
