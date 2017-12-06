package epmc.value.operatorevaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import epmc.operator.Operator;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.OperatorEvaluatorFactory;
import epmc.value.Type;

public final class SimpleEvaluatorFactory implements OperatorEvaluatorFactory {
    private final static Map<ContextValue,SimpleEvaluatorFactory> MAP = new HashMap<>();
    
    public final static SimpleEvaluatorFactory get() {
        SimpleEvaluatorFactory result = MAP.get(ContextValue.get());
        if (result != null) {
            return result;
        }
        result = new SimpleEvaluatorFactory();
        MAP.put(ContextValue.get(), result);
        return result;
    }

    private final static class CacheKey {
        private Operator operator;
        private Type[] types;

        @Override
        public int hashCode() {
            int hash = 0;
            hash = operator.hashCode() + (hash << 6) + (hash << 16) - hash;
            hash = Arrays.hashCode(types) + (hash << 6) + (hash << 16) - hash;
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CacheKey)) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            if (!this.operator.equals(other.operator)) {
                return false;
            }
            if (!Arrays.equals(types, types)) {
                return false;
            }
            return true;
        }
    }
    
    private final List<Class<? extends OperatorEvaluatorSimpleBuilder>> evaluators = new ArrayList<>();
    private final List<Class<? extends OperatorEvaluatorSimpleBuilder>> evaluatorsReversed = Lists.reverse(evaluators);
    private final CacheKey testEntry = new CacheKey();
    private final Map<CacheKey,Class<? extends OperatorEvaluatorSimpleBuilder>> tryMap = new HashMap<>();

    public void add(Class<? extends OperatorEvaluatorSimpleBuilder> clazz) {
        assert clazz != null;
        evaluators.add(clazz);
    }
    
    @Override
    public OperatorEvaluator getEvaluator(Operator operator, Type... types) {
        testEntry.operator = operator;
        testEntry.types = types;
        Class<? extends OperatorEvaluatorSimpleBuilder> tryClazz = tryMap.get(testEntry);
        if (tryClazz != null) {
            try {
                OperatorEvaluatorSimpleBuilder builder = tryClazz.newInstance();
                builder.setOperator(operator);
                builder.setTypes(types);
                OperatorEvaluator evaluator = builder.build();
                if (evaluator != null) {
                    return evaluator;
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        for (Class<? extends OperatorEvaluatorSimpleBuilder> clazz : evaluatorsReversed) {
            try {
                OperatorEvaluatorSimpleBuilder builder = clazz.newInstance();
                builder.setOperator(operator);
                builder.setTypes(types);
                OperatorEvaluator evaluator = builder.build();
                if (evaluator != null) {
                    CacheKey newEntry = new CacheKey();
                    newEntry.operator = operator;
                    newEntry.types = types;
                    tryMap.put(newEntry, clazz);
                    return evaluator;
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
