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

package epmc.constraintsolver.isat3.textual;

import java.io.BufferedReader;
import java.io.IOException;

import epmc.constraintsolver.ConstraintSolverResult;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueSetString;

final class ISatOutputReader {
    private final static char LBRACK_C = '[';
    private final static char RBRACK_C = ']';
    private final static String COMMA = ",";
    private final static String COLON = ":";
    private final static String SATISFIABLE = "SATISFIABLE";
    private final static String UNSATISFIABLE = "UNSATISFIABLE";
    private final static String DASH_DASH = "--";

    private final ConstraintSolverISat3Textual solver;

    ISatOutputReader(ConstraintSolverISat3Textual solver) {
        assert solver != null;
        this.solver = solver;
    }

    ISatResult parseOutput(BufferedReader input) {
        ISatResult result = new ISatResult();
        result.type = ConstraintSolverResult.UNKNOWN;
        result.values = new Value[solver.getVariables().size()];
        String line = null;
        do {
            try {
                line = input.readLine();
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            if (line == null) {
                break;
            }
            if (line.endsWith(COLON)) {
                int varNr = solver.getVariableToNumber().get(line.subSequence(0, line.length() - 1));
                try {
                    line = input.readLine();
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
                assert line != null;
                int dashDashPos = line.indexOf(DASH_DASH);
                if (dashDashPos != -1) {
                    line = line.substring(0, dashDashPos);
                }
                line = line.trim();
                ISatVariable variable = solver.getVariables().get(varNr);
                Value varValue = valueFromInterval(variable.getType(), line);
                result.values[varNr] = varValue;
            }
            if (line.length() >= SATISFIABLE.length()
                    && line.substring(0, SATISFIABLE.length())
                    .equals(SATISFIABLE)) {
                result.type = ConstraintSolverResult.SAT;
            } else if (line.length() >= UNSATISFIABLE.length()
                    && line.substring(0, UNSATISFIABLE.length())
                    .equals(UNSATISFIABLE)) {
                result.type = ConstraintSolverResult.UNSAT;
            }
        } while (line != null);

        return result;
    }

    private Value valueFromInterval(Type type, String line) {
        assert type != null;
        assert line != null;
        assert line.charAt(0) == LBRACK_C : line;
        assert line.charAt(line.length() - 1) == RBRACK_C: line;
        assert line.contains(COMMA): line;
        String lower = line.substring(1, line.indexOf(COMMA));
        return newValue(type, lower);
    }


    private static Value newValue(Type type, String valueString) {
        Value value = type.newValue();
        ValueSetString.as(value).set(valueString);
        return value;
    }
}
