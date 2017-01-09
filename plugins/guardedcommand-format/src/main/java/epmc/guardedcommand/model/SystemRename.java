package epmc.guardedcommand.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
public final class SystemRename implements SystemDefinition {
    private ModelGuardedCommand model;
    private final Positional positional;
    private final List<SystemDefinition> children = new ArrayList<>();

    private final Map<Expression,Expression> renaming;
    
    public SystemRename(SystemDefinition inner, Map<Expression,Expression> renaming, Positional positional) {
        this.positional = positional;
        assert inner != null;
        assert renaming != null;
        for (Entry<Expression, Expression> entry : renaming.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        if (!renaming.isEmpty()) {
            this.renaming = new HashMap<>();
            children.add(inner);
            this.renaming.putAll(renaming);
        } else {
            this.renaming = Collections.emptyMap();
        }
    }
    
    public SystemDefinition getInner() {
        return children.get(0);
    }
    
    public Map<Expression,Expression> getRenaming() {
        return Collections.unmodifiableMap(renaming);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(" + children.get(0) + "/{");
        int renameNr = 0;
        for (Entry<Expression,Expression> entry : renaming.entrySet()) {
            builder.append(entry.getKey() + "<-" + entry.getValue());
            if (renameNr < renaming.size() - 1) {
                builder.append(",");
            }
            renameNr++;
        }
        builder.append("})");
        return builder.toString();
    }

    @Override
    public Set<Expression> getAlphabet() {
        Set<Expression> result = new HashSet<>();
        for (Expression expression : getInner().getAlphabet()) {
            if (renaming.containsKey(expression)) {
                result.add(renaming.get(expression));
            } else {
                result.add(expression);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public List<SystemDefinition> getChildren() {
        return children;
    }

    @Override
    public void setModel(ModelGuardedCommand model) {
        this.model = model;
        for (SystemDefinition system : getChildren()) {
            system.setModel(model);
        }
    }

    @Override
    public ModelGuardedCommand getModel() {
        return model;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
}
