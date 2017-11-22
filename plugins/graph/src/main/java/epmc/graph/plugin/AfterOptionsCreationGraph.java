/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.graph.plugin;

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
    public void process(Options options) {
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
