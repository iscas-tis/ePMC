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
public final class SystemAsyncParallel implements SystemDefinition {
    private ModelGuardedCommand model;
    private final Positional positional;
    private List<SystemDefinition> children = new ArrayList<>();

    public SystemAsyncParallel(SystemDefinition left, SystemDefinition right, Positional positional) {
        this.positional = positional;
        assert left != null;
        assert right != null;
        children.add(left);
        children.add(right);
    }
    
    public SystemDefinition getLeft() {
        return children.get(0);
    }
    
    public SystemDefinition getRight() {
        return children.get(1);
    }
    
    @Override
    public String toString() {
        return "(" + children.get(0) + "|||" + children.get(1) + ")";
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
    public void setModel(ModelGuardedCommand model) {
        assert model != null;
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
