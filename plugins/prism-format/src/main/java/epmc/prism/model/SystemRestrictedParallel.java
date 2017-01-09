package epmc.prism.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
public final class SystemRestrictedParallel implements SystemDefinition {
    private final Set<Expression> sync;
    private ModelPRISM model;
    private final Positional positional;
    private final List<SystemDefinition> children = new ArrayList<>();
    
    public SystemRestrictedParallel(SystemDefinition left, SystemDefinition right,
            Set<Expression> sync, Positional positional) {
        this.positional = positional;
        assert left != null;
        assert right != null;
        assert sync != null;
        for (Expression expr : sync) {
            assert expr != null;
        }
        children.add(left);
        children.add(right);
        if (!sync.isEmpty()) {
            this.sync = new HashSet<>();
        } else {
            this.sync = Collections.emptySet();
        }
        this.sync.addAll(sync);
    }
    
    public SystemDefinition getLeft() {
        return children.get(0);
    }
    
    public SystemDefinition getRight() {
        return children.get(1);
    }
    
    public Set<Expression> getSync() {
        return Collections.unmodifiableSet(sync);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(" + children.get(0) + "|[");
        
        int syncNr = 0;
        for (Expression label : sync) {
            builder.append(label);
            if (syncNr < sync.size() - 1) {
                builder.append(",");
            }
            syncNr++;
        }
        builder.append("]|" + children.get(1) + ")");
        return builder.toString();
    }
    
    @Override
    public Set<Expression> getAlphabet() {
        Set<Expression> result = new HashSet<>();
        result.addAll(getLeft().getAlphabet());
        result.addAll(getRight().getAlphabet());
        return Collections.unmodifiableSet(result);
    }

    @Override
    public List<SystemDefinition> getChildren() {
        return children;
    }

    @Override
    public void setModel(ModelPRISM model) {
        this.model = model;
        for (SystemDefinition system : getChildren()) {
            system.setModel(model);
        }
    }

    @Override
    public ModelPRISM getModel() {
        return model;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
}
