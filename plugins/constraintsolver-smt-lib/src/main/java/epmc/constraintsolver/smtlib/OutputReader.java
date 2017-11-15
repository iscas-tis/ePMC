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
import java.io.IOException;

import epmc.constraintsolver.ConstraintSolverResult;
import epmc.operator.OperatorDivide;
import epmc.sexpression.SExpression;
import epmc.sexpression.SExpressionParser;
import epmc.sexpression.UtilSExpression;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;

final class OutputReader {
    private final static String SAT = "sat";
    private final static String UNSAT = "unsat";
    private final static String UNKNOWN = "unknown";
    private final static String DIV = "/";

    private final ConstraintSolverSMTLib solver;
    private final SExpressionParser parser ;

    OutputReader(ConstraintSolverSMTLib solver) {
        assert solver != null;
        this.solver = solver;
        parser = UtilSExpression.newParser();
    }

    SMTLibResult parseOutput(BufferedReader input) {
        SMTLibResult result = new SMTLibResult();
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
                continue;
            }
            SExpression parsed = parser.parse(line);
            line = line.trim();
            if (parsed.isAtomic() && parsed.getAtomic().equals(SAT)) {
                result.type = ConstraintSolverResult.SAT;				
            } else if (parsed.isAtomic() && parsed.getAtomic().equals(UNSAT)) {
                result.type = ConstraintSolverResult.UNSAT;		
                return result;
            } else if (parsed.isAtomic() && parsed.getAtomic().equals(UNKNOWN)) {
                result.type = ConstraintSolverResult.UNKNOWN;
                return result;
            } else if (parsed.isList() && parsed.listSize() > 0
                    && parsed.getChild(0).isList()
                    && parsed.getChild(0).listSize() == 2
                    && parsed.getChild(0).getChild(0).isAtomic()) {
                SExpression inner = parsed.getChild(0);
                int varNr = solver.getVariableToNumber().get(inner.getChild(0).getAtomic());
                Type varType = solver.getVariables().get(varNr).getType();
                SExpression valueExpr = inner.getChild(1);
                if (valueExpr.isAtomic()) {
                    result.values[varNr] = UtilValue.newValue(varType, valueExpr.getAtomic());
                } else {
                    assert valueExpr.getChild(0).getAtomic().equals(DIV);
                    Value num = UtilValue.newValue(varType, valueExpr.getChild(1).getAtomic());
                    Value den = UtilValue.newValue(varType, valueExpr.getChild(2).getAtomic());
                    OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, num.getType(), den.getType());
                    result.values[varNr] = varType.newValue();
                    divide.apply(result.values[varNr], num, den);
                }
            }
        } while (line != null);
        return result;
    }

}
