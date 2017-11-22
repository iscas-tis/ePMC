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

package epmc.constraintsolver.smtlib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.Direction;
import epmc.constraintsolver.Feature;
import epmc.constraintsolver.smtlib.options.OptionsSMTLib;
import epmc.expression.Expression;
import epmc.options.Options;
import epmc.value.Type;
import epmc.value.Value;

public final class ConstraintSolverSMTLib implements ConstraintSolver {
    public final static String IDENTIFIER = "smt-lib";
    private final static String TEMPFILE_PREFIX = "smtlib";
    private final static String TEMPFILE_SUFFIX = ".smtlib";

    private boolean closed;
    private final Set<Feature> features = new LinkedHashSet<>();
    private final List<Expression> constraints = new ArrayList<>();
    private final List<SMTLibVariable> variables = new ArrayList<>();
    private final Map<String,Integer> variableToNumber = new LinkedHashMap<>();
    private Value[] resultVariableValues;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void requireFeature(Feature feature) {
        assert !closed;
        assert feature != null;
        features.add(feature);
    }

    @Override
    public boolean canHandle() {
        assert !closed;
        for (Feature feature : features) {
            if (feature != Feature.SMT) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void build() {
        assert !closed;
    }

    @Override
    public int addVariable(String name, Type type, Value lower, Value upper) {
        assert !closed;
        assert name != null;
        assert type != null;
        this.variableToNumber.put(name, variables.size());
        variables.add(new SMTLibVariable.Builder()
                .setName(name)
                .setType(type)
                .setLower(lower)
                .setUpper(upper)
                .build());
        return variables.size() - 1;
    }

    @Override
    public void addConstraint(Expression expression) {
        assert !closed;
        constraints.add(expression);
    }

    @Override
    public void setObjective(Expression objective) {
        assert false;
    }

    @Override
    public void setDirection(Direction direction) {
        assert direction == Direction.FEASIBILITY;
    }

    @Override
    public ConstraintSolverResult solve() {
        assert !closed;
        Path path = null;
        try {
            path = Files.createTempFile(TEMPFILE_PREFIX, TEMPFILE_SUFFIX);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File file = path.toFile();
        if (!Options.get().getBoolean(OptionsSMTLib.SMTLIB_KEEP_TEMPORARY_FILES)) {
            file.deleteOnExit();
        }
        try (OutputStream outStream = new FileOutputStream(file);) {
            InputWriter writer = new InputWriter(this, outStream);
            writer.write();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        SMTLibResult result = callSolver(file.getAbsolutePath());
        this.resultVariableValues = result.values;
        return result.type;
    }

    private SMTLibResult callSolver(String file) {
        List<String> callOptions = Options.get().get(OptionsSMTLib.SMTLIB_COMMAND_LINE);
        List<String> execArgs = new ArrayList<>();
        for (String param : callOptions) {
            execArgs.add(MessageFormat.format(param, file));
        }

        Process isat3Process = null;
        try {
            isat3Process = Runtime.getRuntime().exec(execArgs.toArray(new String[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final BufferedReader isat3In = new BufferedReader
                (new InputStreamReader(isat3Process.getInputStream()));
        OutputReader reader = new OutputReader(this);
        SMTLibResult result = reader.parseOutput(isat3In);
        try {
            isat3Process.waitFor();
            //assert isat3Process.waitFor() == 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result;
    }

    @Override
    public Value[] getResultVariablesValues() {
        return resultVariableValues;
    }

    @Override
    public Value getResultObjectiveValue() {
        return null;
    }

    @Override
    public void close() {
        closed = true;
    }

    List<Expression> getConstraints() {
        return constraints;
    }

    List<SMTLibVariable> getVariables() {
        return variables;
    }

    Map<String, Integer> getVariableToNumber() {
        return variableToNumber;
    }

    @Override
    public String getVariableName(int number) {
        return variables.get(number).getName();
    }
}
