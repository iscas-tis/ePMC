package epmc.jani.extensions.functions;

import java.util.List;

import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.ExplorerJANI;
import epmc.jani.model.ModelExtension;

public final class ExplorerExtensionFunctions implements ExplorerExtension {
    public final static String IDENTIFIER = "functions";
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setExplorer(ExplorerJANI explorer) {
        List<ModelExtension> extensions = explorer.getModel().getModelExtensionsOrEmpty();
        for (ModelExtension extension : extensions) {
            if (extension instanceof ModelExtensionFunctions) {
                ModelExtensionFunctions extensionFunctions = (ModelExtensionFunctions) extension;
                explorer.getEvaluatorCache().putAux(ModelExtensionFunctions.class, extensionFunctions);
            }
        }
    }
}
