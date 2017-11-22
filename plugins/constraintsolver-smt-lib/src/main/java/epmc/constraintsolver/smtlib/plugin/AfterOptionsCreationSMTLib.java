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

package epmc.constraintsolver.smtlib.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import epmc.constraintsolver.options.OptionsConstraintsolver;
import epmc.constraintsolver.smtlib.ConstraintSolverSMTLib;
import epmc.constraintsolver.smtlib.options.OptionsSMTLib;
import epmc.constraintsolver.smtlib.options.SMTLibVersion;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeStringList;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationSMTLib implements AfterOptionsCreation {
    public final static String IDENTIFIER = "constraintsolver-smtlib";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;

        Map<String,Class<?>> solvers = options.get(OptionsConstraintsolver.CONSTRAINTSOLVER_SOLVER_CLASS);
        assert solvers != null;
        solvers.put(ConstraintSolverSMTLib.IDENTIFIER, ConstraintSolverSMTLib.class);		

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

        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption()
        .setBundleName(OptionsSMTLib.OPTIONS_SMTLIB)
        .setIdentifier(OptionsSMTLib.SMTLIB_KEEP_TEMPORARY_FILES)
        .setCategory(category)
        .setType(typeBoolean)
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
    }
}
