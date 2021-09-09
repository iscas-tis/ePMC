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

package epmc.petl.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import epmc.constraintsolver.options.OptionsConstraintsolver;
import epmc.constraintsolver.smtlib.options.SMTLibVersion;
import epmc.constraintsolver.smtlib.options.OptionsSMTLib;
import epmc.constraintsolver.smtlib.petl.ConstraintSolverSMTLib;
import epmc.graph.LowLevel;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeInteger;
import epmc.options.OptionTypeMap;
import epmc.options.OptionTypeStringList;
import epmc.options.Options;
import epmc.petl.model.LowLevelMASBuilder;
import epmc.petl.model.ModelMAS;
import epmc.petl.model.PropertyPETL;
import epmc.plugin.AfterOptionsCreation;
import epmc.prism.model.convert.UtilPrismConverter;
import epmc.prism.options.OptionsPRISM;
import epmc.propertysolver.OptionsUCT;
import epmc.propertysolver.PropertySolverPETLUntilUCT;
import epmc.propertysolver.PropertySolverExplicitKnowledge;
import epmc.propertysolver.PropertySolverPETLUntilMINLP;

public final class AfterOptionsCreationPETL implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-options-creation-petl";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
    	assert options != null;
        OptionTypeMap<Class<?>> modelInputType = options.getType(OptionsModelChecker.MODEL_INPUT_TYPE);
        assert modelInputType != null;
        modelInputType.put(ModelMAS.IDENTIFIER, ModelMAS.class);
        Map<String,Class<?>> propertyClasses = options.get(OptionsModelChecker.PROPERTY_CLASS);
        assert propertyClasses != null;
        propertyClasses.put(PropertyPETL.IDENTIFIER, PropertyPETL.class);
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsPRISM.PRISM_OPTIONS)
        .setIdentifier(OptionsPRISM.PRISM_FLATTEN)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb().build();
        Map<String,Class<? extends LowLevel.Builder>> map = 
                options.get(OptionsModelChecker.LOW_LEVEL_ENGINE_CLASS);
        map.put(LowLevelMASBuilder.IDENTIFIER, LowLevelMASBuilder.class);
        UtilPrismConverter.addOptions(options);
        
        Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
        solvers.put(PropertySolverPETLUntilUCT.IDENTIFIER, PropertySolverPETLUntilUCT.class);
        solvers.put(PropertySolverExplicitKnowledge.IDENTIFIER, PropertySolverExplicitKnowledge.class);
        solvers.put(PropertySolverPETLUntilMINLP.IDENTIFIER, PropertySolverPETLUntilMINLP.class);

        Map<String,Class<?>> smtSolvers = options.get(OptionsConstraintsolver.CONSTRAINTSOLVER_SOLVER_CLASS);
        assert smtSolvers != null;
        smtSolvers.put(ConstraintSolverSMTLib.IDENTIFIER, ConstraintSolverSMTLib.class);
        
        Category category = options.addCategory()
                .setBundleName(OptionsSMTLib.OPTIONS_SMTLIB)
                .setIdentifier(OptionsSMTLib.SMTLIB_CATEGORY)
                .setParent(OptionsConstraintsolver.CONSTRAINTSOLVER_CATEGORY)
                .build();
        OptionTypeStringList typeCommand = new OptionTypeStringList("command");
        List<String> defaultCommandLine = new ArrayList<>();
        defaultCommandLine.add("z3");
        defaultCommandLine.add("-I");
        defaultCommandLine.add("{0}");
        options.addOption()
        .setBundleName(OptionsSMTLib.OPTIONS_SMTLIB)
        .setIdentifier(OptionsSMTLib.SMTLIB_COMMAND_LINE)
        .setCategory(category)
        .setType(typeCommand)
        .setDefault(defaultCommandLine)
        .setCommandLine().setGui().setWeb()
        .build();

        OptionTypeBoolean typeBoolean1 = OptionTypeBoolean.getInstance();
        options.addOption()
        .setBundleName(OptionsSMTLib.OPTIONS_SMTLIB)
        .setIdentifier(OptionsSMTLib.SMTLIB_KEEP_TEMPORARY_FILES)
        .setCategory(category)
        .setType(typeBoolean1)
        .setDefault(false)
        .setCommandLine().setGui().setWeb()
        .build();

        OptionTypeEnum typeSMTLibVersion = new OptionTypeEnum(SMTLibVersion.class);
        options.addOption()
        .setBundleName(OptionsSMTLib.OPTIONS_SMTLIB)
        .setIdentifier(OptionsSMTLib.SMTLIB_VERSION)
        .setCategory(category)
        .setType(typeSMTLibVersion)
        .setDefault(SMTLibVersion.V20)
        .setCommandLine().setGui().setWeb()
        .build();
        
        OptionTypeInteger typeUCTInteger = OptionTypeInteger.getInstance();
        options.addOption()
        .setBundleName(OptionsUCT.OPTIONS_UCT)
        .setIdentifier(OptionsUCT.UCT_DEPTH_LIMIT)
        .setType(typeUCTInteger)
        .setDefault(10)
        .setCommandLine().setGui().setWeb()
        .build();
        
        options.addOption()
        .setBundleName(OptionsUCT.OPTIONS_UCT)
        .setIdentifier(OptionsUCT.UCT_TIME_LIMIT)
        .setType(typeUCTInteger)
        .setDefault(1)
        .setCommandLine().setGui().setWeb()
        .build();
        
        options.addOption()
        .setBundleName(OptionsUCT.OPTIONS_UCT)
        .setIdentifier(OptionsUCT.RANDOM_SEED)
        .setType(typeUCTInteger)
        .setDefault(1)
        .setCommandLine().setGui().setWeb()
        .build();
        
        options.addOption()
        .setBundleName(OptionsUCT.OPTIONS_UCT)
        .setIdentifier(OptionsUCT.PRINT_TIME_INTERVAL)
        .setType(typeUCTInteger)
        .setDefault(1)
        .setCommandLine().setGui().setWeb()
        .build();
        
        //OptionTypeString typeUCTString = OptionTypeString.getInstance();
        options.addOption()
        .setBundleName(OptionsUCT.OPTIONS_UCT)
        .setIdentifier(OptionsUCT.BVALUE)
        .setType(typeUCTInteger)
        .setDefault(1)
        .setCommandLine().setGui().setWeb()
        .build();
    }
}
