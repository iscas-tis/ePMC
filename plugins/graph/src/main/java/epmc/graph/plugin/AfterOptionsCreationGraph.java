package epmc.graph.plugin;

import epmc.error.EPMCException;
import epmc.graph.OptionsTypesGraph;
import epmc.graph.options.OptionsGraph;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.EngineExplorer;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationGraph implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-graph";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
        assert options != null;
        assert options != null;
        options.addOption().setBundleName(OptionsGraph.OPTIONS_GRAPH)
            .setIdentifier(OptionsGraph.STATE_STORAGE)
            .setType(new OptionTypeEnum(OptionsTypesGraph.StateStorage.class))
            .setDefault(OptionsTypesGraph.StateStorage.SMALLEST)
            .setCommandLine().setGui().setWeb().build();
        options.addOption().setBundleName(OptionsGraph.OPTIONS_GRAPH)
            .setIdentifier(OptionsGraph.WRAPPER_GRAPH_SUCCESSORS_SIZE)
            .setType(new OptionTypeEnum(OptionsTypesGraph.WrapperGraphSuccessorsSize.class))
            .setDefault(OptionsTypesGraph.WrapperGraphSuccessorsSize.SMALLEST)
            .setCommandLine().setGui().setWeb().build();
        
        OptionTypeMap<Class<?>> engineType = options.getOption(OptionsModelChecker.ENGINE).getType();
        engineType.put(EngineDD.IDENTIFIER, EngineDD.class);
        engineType.put(EngineExplorer.IDENTIFIER, EngineExplorer.class);
        engineType.put(EngineExplicit.IDENTIFIER, EngineExplicit.class);
	}

}
