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

package epmc.graphsolver;

import java.util.Map;

import epmc.graphsolver.preprocessor.PreprocessorExplicitMCMDPOne;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeLong;
import epmc.options.OptionTypeStringListSubset;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public final class AfterOptionsCreationGraphsolver implements AfterOptionsCreation {
    public final static String IDENTIFIER = "after-options-creation-graphsolver";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;

        Category category = options.addCategory()
                .setBundleName(OptionsGraphsolver.OPTIONS_GRAPHSOLVER)
                .setIdentifier(OptionsGraphsolver.GRAPHSOLVER_CATEGORY)
                .build();

        Map<String,Class<?>> preprocessors = new OrderedMap<>();
        preprocessors.put(PreprocessorExplicitMCMDPOne.IDENTIFIER, PreprocessorExplicitMCMDPOne.class);
        options.set(OptionsGraphsolver.GRAPHSOLVER_PREPROCESSOR_EXPLICIT_CLASS, preprocessors);
        OptionTypeStringListSubset<Class<?>> preprocessorType = new OptionTypeStringListSubset<>(preprocessors);
        options.addOption().setBundleName(OptionsGraphsolver.OPTIONS_GRAPHSOLVER)
        .setIdentifier(OptionsGraphsolver.GRAPHSOLVER_PREPROCESSOR_EXPLICIT)
        .setType(preprocessorType)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        Map<String,Class<?>> graphSolvers = new OrderedMap<>(true);
        options.set(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS, graphSolvers);
        OptionTypeStringListSubset<Class<?>> graphSolverType = new OptionTypeStringListSubset<>(graphSolvers);
        options.addOption().setBundleName(OptionsGraphsolver.OPTIONS_GRAPHSOLVER)
        .setIdentifier(OptionsGraphsolver.GRAPHSOLVER_SOLVER)
        .setType(graphSolverType)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        Map<String,Class<?>> lumpersExplicit = new OrderedMap<>();
        options.set(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT_CLASS, lumpersExplicit);
        OptionTypeStringListSubset<Class<?>> lumperExplicitType = new OptionTypeStringListSubset<>(lumpersExplicit);
        options.addOption().setBundleName(OptionsGraphsolver.OPTIONS_GRAPHSOLVER)
        .setIdentifier(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT)
        .setType(lumperExplicitType)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsGraphsolver.OPTIONS_GRAPHSOLVER)
        .setIdentifier(OptionsGraphsolver.GRAPHSOLVER_LUMP_BEFORE_GRAPH_SOLVING)
        .setType(typeBoolean).setDefault(false)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        Map<String,Class<?>> lumpersDD = new OrderedMap<>();
        options.set(OptionsGraphsolver.GRAPHSOLVER_DD_LUMPER_CLASS, lumpersDD);
        OptionTypeStringListSubset<Class<?>> lumperDDType = new OptionTypeStringListSubset<>(lumpersDD);
        options.addOption().setBundleName(OptionsGraphsolver.OPTIONS_GRAPHSOLVER)
        .setIdentifier(OptionsGraphsolver.GRAPHSOLVER_LUMPER_DD)
        .setType(lumperDDType)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        
        options.addOption().setBundleName(OptionsGraphsolver.OPTIONS_GRAPHSOLVER)
        .setIdentifier(OptionsGraphsolver.GRAPHSOLVER_UPDATE_DELAY)
        .setType(OptionTypeLong.getTypeLong())
        .setDefault(1000L)
        .setCommandLine().setGui().setWeb()
        .setCategory(category)
        .build();
    }

}
