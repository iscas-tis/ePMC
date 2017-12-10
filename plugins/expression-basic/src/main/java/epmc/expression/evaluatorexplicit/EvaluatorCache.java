package epmc.expression.evaluatorexplicit;

import java.util.HashMap;
import java.util.Map;

import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit.EvaluatorCacheEntry;

public final class EvaluatorCache {
    private final Map<EvaluatorCacheEntry,EvaluatorExplicit> map = new HashMap<>();
    
    public EvaluatorExplicit get(EvaluatorCacheEntry key) {
        return map.get(key);
    }
    
    public void put(EvaluatorCacheEntry key,EvaluatorExplicit value) {
        map.put(key, value);
    }
}
