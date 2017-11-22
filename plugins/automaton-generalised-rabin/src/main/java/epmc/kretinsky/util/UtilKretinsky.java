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

package epmc.kretinsky.util;

import static epmc.error.UtilError.ensure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.options.Options;

public final class UtilKretinsky {
    private UtilKretinsky() {
    }

    public static boolean isTemporal(Expression expression) {
        if (expression.isTemporal()) {
            return true;
        } else {
            for (Expression child : expression.getChildren()) {
                if (isTemporal(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean spotLTLEquivalent(Expression expression1, Expression expression2) {
        assert expression1 != null;
        assert expression2 != null;
        Map<Expression,String> expr2str = contextExpression.newMap();
        int[] numAPs = new int[1];
        UtilExpression.expr2string(expression1, expr2str, numAPs, false);
        UtilExpression.expr2string(expression2, expr2str, numAPs, false);
        String spotFn1 = UtilExpression.expr2spot(expression1, expr2str);
        String spotFn2 = UtilExpression.expr2spot(expression2, expr2str);
        String ltlfilt = Options.get().get();
        try {
            final String[] autExecArgs = {ltlfilt, "-f", spotFn1,
                    "--equivalent-to", spotFn2};
            final Process autProcess = Runtime.getRuntime().exec(autExecArgs);
            final BufferedReader autIn = new BufferedReader
                    (new InputStreamReader(autProcess.getInputStream()));
            int i = autIn.read();
            return i != -1;
        } catch (IOException e) {
            ensure(false, ProblemsExpression.LTL2BA_SPOT_PROBLEM_IO, e);
            return false;
        }
    }

}
