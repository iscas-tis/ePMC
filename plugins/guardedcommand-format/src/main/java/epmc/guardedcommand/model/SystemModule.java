package epmc.guardedcommand.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
public final class SystemModule implements SystemDefinition {
	private final static String QUOT = "\"";
	private final static String SPACE = " ";
	
    private ModelGuardedCommand model;
    private final Positional positional;
    private final List<SystemDefinition> children = new ArrayList<>();
    private final String module;
    private final String instanceName;
    
    SystemModule(String module, String instanceName, Positional positional) {
        assert module != null;
        this.positional = positional;
        this.module = module;
        this.instanceName = instanceName;
    }

    public SystemModule(String module, Positional positional) {
        this(module, null, positional);
    }
    
    public String getModule() {
        return module;
    }
    
    @Override
    public String toString() {
        return module;
    }

    @Override
    public Set<Expression> getAlphabet() {
        assert model != null;
        for (Module module : getModel().getModules()) {
            if (module.getName().equals(getModule())) {
                return module.getAlphabet();
            }
        }
        assert false : QUOT + module + QUOT + SPACE + SPACE + QUOT + getModel().getModules() + QUOT;
        return null;
    }
    
    String getInstanceName() {
        return instanceName;
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
        return this.model;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
}
