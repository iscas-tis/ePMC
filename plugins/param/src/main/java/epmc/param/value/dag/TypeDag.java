package epmc.param.value.dag;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import epmc.param.value.ParameterSet;
import epmc.param.value.TypeFunction;
import epmc.param.value.TypeStatistics;
import epmc.value.ContextValue;
import epmc.value.Type;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public final class TypeDag implements TypeFunction, TypeStatistics {
    public final static String IDENTIFIER = "dag";
    private final static String DAG = "Dag";
    
    public final static class Builder implements TypeFunction.Builder {
        private ParameterSet parameters;

        @Override
        public TypeFunction.Builder setParameters(ParameterSet parameters) {
            this.parameters = parameters;
            return this;
        }

        @Override
        public TypeFunction build() {
            return ContextValue.get().makeUnique(new TypeDag(this));
        }
    }

    private final ParameterSet parametersSet;
    private final boolean useReferenceCounting = false;
    private final ObjectOpenHashSet<PhantomReference<ValueDag>> references;
    private final ReferenceQueue<ValueDag> queue;
    private final Dag dag;

    public static boolean is(Type type) {
        return type instanceof TypeDag;
    }

    public static TypeDag as(Type type) {
        if (is(type)) {
            return (TypeDag) type;
        }
        return null;
    }

    public TypeDag(ParameterSet parameterSet) {
        assert parameterSet != null;
        this.parametersSet = parameterSet;
        if (useReferenceCounting) {
            references = new ObjectOpenHashSet<>();
            queue = new ReferenceQueue<>();
        } else {
            references = null;
            queue = null;
        }
        dag = new Dag(parameterSet, useReferenceCounting);
    }
    
    private TypeDag(Builder builder) {
        this(builder.parameters);
    }
    
    @Override
    public TypeArrayFunctionDag getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayFunctionDag(this));
    }

    @Override
    public ValueDag newValue() {
        ValueDag result = new ValueDag(this);
        handleReferenceCounting(result);
        
        return result;
    }

    private void handleReferenceCounting(ValueDag result) {
        if (!useReferenceCounting) {
            return;
        }
        Reference<? extends ValueDag> r = queue.poll();
        int num = 0;
        while (r != null) {
            assert references.contains(r);
            references.remove(r);
            num++;
            r.clear();
            r = queue.poll();
        }
        if (num > 0) {
            System.out.println("HERE " + num + " " + references.size());
        }
        references.add(new PhantomReference<>(result, queue));
    }

    @Override
    public ParameterSet getParameterSet() {
        return parametersSet;
    }

    public Dag getDag() {
        return dag;
    }
    
    public boolean isUseReferenceCounting() {
        return useReferenceCounting;
    }
    
    @Override
    public String toString() {
        return DAG;
    }
    
    @Override
    public void sendStatistics() {
        dag.sendStatistics();
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = parametersSet.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeDag)) {
            return false;
        }
        TypeDag other = (TypeDag) obj;
        if (!this.parametersSet.equals(other.parametersSet)) {
            return false;
        }
        return true;
    }
}
