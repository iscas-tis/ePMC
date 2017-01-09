package epmc.kretinsky.util;

import static epmc.error.UtilError.ensure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.expression.ProblemsExpression;
import epmc.expression.UtilExpression;
import epmc.kretinsky.options.OptionsKretinsky;
import epmc.options.OptionsEPMC;

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
    
    public static boolean spotLTLEquivalent(Expression expression1, Expression expression2) throws EPMCException {
        assert expression1 != null;
        assert expression2 != null;
        assert expression1.getContext() == expression2.getContext();
        ContextExpression contextExpression = expression1.getContext();
        Map<Expression,String> expr2str = contextExpression.newMap();
        int[] numAPs = new int[1];
        UtilExpression.expr2string(expression1, expr2str, numAPs, false);
        UtilExpression.expr2string(expression2, expr2str, numAPs, false);
        String spotFn1 = UtilExpression.expr2spot(expression1, expr2str);
        String spotFn2 = UtilExpression.expr2spot(expression2, expr2str);
        String ltlfilt = expression1.getOptions().get(OptionsKretinsky.KRETINSKY_LTLFILT_CMD);
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
