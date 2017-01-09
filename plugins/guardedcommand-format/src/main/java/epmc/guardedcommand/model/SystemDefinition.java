package epmc.guardedcommand.model;

import java.util.List;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.value.ContextValue;

public interface SystemDefinition {
    List<SystemDefinition> getChildren();
    
    Set<Expression> getAlphabet();
    
    void setModel(ModelGuardedCommand model);
    
    ModelGuardedCommand getModel();
    
    default ContextValue getContextValue() {
        return getModel().getContextValue();
    }

    Positional getPositional();
    
    default boolean isAlphaParallel() {
        return this instanceof SystemAlphaParallel;
    }
    
    default boolean isAsyncParallel() {
        return this instanceof SystemAsyncParallel;
    }
    
    default boolean isHide() {
        return this instanceof SystemHide;
    }
    
    default boolean isModule() {
        return this instanceof SystemModule;
    }
    
    default boolean isRename() {
        return this instanceof SystemRename;
    }
    
    default boolean isRestrictedParallel() {
        return this instanceof SystemRestrictedParallel;
    }
    
    default SystemAlphaParallel asAlphaParallel() {
        assert isAlphaParallel();
        return (SystemAlphaParallel) this;
    }
    
    default SystemAsyncParallel asAsyncParallel() {
        assert isAsyncParallel();
        return (SystemAsyncParallel) this;
    }
    
    default SystemHide asHide() {
        assert isHide();
        return (SystemHide) this;
    }
    
    default SystemModule asModule() {
        assert isModule();
        return (SystemModule) this;
    }
    
    default SystemRename asRename() {
        assert isRename();
        return (SystemRename) this;
    }
    
    default SystemRestrictedParallel asRestrictedParallel() {
        assert isRestrictedParallel();
        return (SystemRestrictedParallel) this;
    }
}
