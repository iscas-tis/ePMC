package epmc.guardedcommand.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
public final class SystemHide implements SystemDefinition {
    private ModelGuardedCommand model;
    private final Positional positional;
    private final List<SystemDefinition> children = new ArrayList<>();
    private final Set<Expression> hidden;
    
    public SystemHide(SystemDefinition inner, Set<Expression> hidden, Positional positional) {
        this.positional = positional;
        assert inner != null;
        assert hidden != null;
        for (Expression expr : hidden) {
            assert expr != null;
        }
        children.add(inner);
        if (!hidden.isEmpty()) {
            this.hidden = new HashSet<>();
        } else {
            this.hidden = Collections.emptySet();
        }
        this.hidden.addAll(hidden);
    }
    
    public SystemDefinition getInner() {
        return children.get(0);
    }
    
    public Set<Expression> getHidden() {
        return Collections.unmodifiableSet(hidden);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(" + children.get(0) + "/{");
        int hiddenNr = 0;
        for (Expression label : hidden) {
            builder.append(label);
            if (hiddenNr < hidden.size() - 1) {
                builder.append(",");
            }
            hiddenNr++;
        }
        builder.append("})");
        return builder.toString();
    }

    @Override
    public Set<Expression> getAlphabet() {
        Set<Expression> result = new HashSet<>();
        result.addAll(getInner().getAlphabet());
        result.removeAll(hidden);
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