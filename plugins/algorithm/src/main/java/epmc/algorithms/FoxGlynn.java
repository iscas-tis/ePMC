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

package epmc.algorithms;

import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorCeil;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorExp;
import epmc.operator.OperatorFloor;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLn;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorOverflow;
import epmc.operator.OperatorPow;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.operator.OperatorUnderflow;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueReal;

/**
 * Fox-Glynn algorithm.
 * This implementation follows the instructions by
 * <a href="https://pms.cs.ru.nl/iris-diglib/src/getContent.php?id=2011-Jansen-UnderstandingFoxGlynn">
 * David N. Jansen: Understanding Fox and Glynn’s “Computing Poisson probabilities”.
 * </a>
 * TODO check constants used; only valid for doubles?
 * 
 * @author Ernst Moritz Hahn
 */
public final class FoxGlynn {
    /* input */
    private final Log log;
    private final ValueReal lambda;
    private final ValueReal tau;
    private final ValueReal omega;
    private final ValueReal epsilon;
    private final TypeReal typeReal;
    private final TypeArray typeArray;

    /* constants */
    private final ValueReal zero;
    private final ValueReal one;
    private final ValueReal two;
    private final ValueReal oneHalf;
    private final ValueReal oneEights;
    private final ValueReal lambda_400;
    private final ValueReal sqrt_2_pi;  // sqrt( 2.0 * pi )
    private final ValueReal log10_e; // log10(e)
    private final ValueReal factor1; // 1 / ((1+1/400) * exp(1/16) * sqrt_2)
    private final ValueReal factor2; // 1 / ((1+1/lambda) * exp(1/16) * sqrt_2)
    private final ValueReal bigNumber;
    private final ValueReal vm1922272;

    /* computed values */
    private int left;
    private int right;
    private ValueReal startValue;
    private ValueArrayAlgebra values;
    private ValueReal totalWeight;

    private void finder(int m) {
        OperatorEvaluator logOp = ContextValue.get().getEvaluator(OperatorLn.LN, TypeReal.get());
        OperatorEvaluator ceil = ContextValue.get().getEvaluator(OperatorCeil.CEIL, typeReal);
        OperatorEvaluator floor = ContextValue.get().getEvaluator(OperatorFloor.FLOOR, typeReal);
        OperatorEvaluator pow = ContextValue.get().getEvaluator(OperatorPow.POW, typeReal, typeReal);
        OperatorEvaluator exp = ContextValue.get().getEvaluator(OperatorExp.EXP, typeReal);
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeReal.get(), TypeReal.get());
        OperatorEvaluator lt = ContextValue.get().getEvaluator(OperatorLt.LT, typeReal, typeReal);
        OperatorEvaluator gt = ContextValue.get().getEvaluator(OperatorGt.GT, typeReal, typeReal);
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, typeReal, typeReal);
        ValueBoolean cmp = TypeBoolean.get().newValue();
        ValueReal kTimesEpsilon = typeReal.newValue();
        ValueReal tau = typeReal.newValue();
        logOp.apply(tau, this.tau);
        ValueReal epsilon = typeReal.newValue();
        multiply.apply(epsilon, this.epsilon, sqrt_2_pi);
        ValueReal kReal = typeReal.newValue();
        ValueReal maxError = typeReal.newValue();
        OperatorEvaluator addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, TypeReal.get());
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeReal.get(), TypeReal.get());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeReal.get(), TypeReal.get());
        if (m < 25) {
            ValueReal minusLambda = typeReal.newValue();
            addInverse.apply(minusLambda, lambda);
            ValueReal expMinusLambda = typeReal.newValue();
            exp.apply(expMinusLambda, minusLambda);
            lt.apply(cmp, minusLambda, tau);
            if (cmp.getBoolean()) {
                log.send(MessagesAlgorithm.FOX_GLYNN_UNRELIABLE_EXP_BELOW_TAU, lambda, expMinusLambda);
            }
            left = 0;
        } else {
            // const double bl = (1 + 1 / lambda) * exp((1/lambda) * 0.125);
            ValueReal oneDivLambda = typeReal.newValue();
            divide.apply(oneDivLambda, one, lambda);
            ValueReal onePlusOneDivLambda = typeReal.newValue();
            add.apply(onePlusOneDivLambda, one, oneDivLambda);
            ValueReal bl = typeReal.newValue();
            multiply.apply(bl, oneDivLambda, oneEights);
            exp.apply(bl, bl);
            multiply.apply(bl, onePlusOneDivLambda, bl);
            ValueReal sqrtLambda = typeReal.newValue();
            pow.apply(sqrtLambda, lambda, oneHalf);

            ValueReal ceiled = typeReal.newValue();
            ValueReal maxErrorT2 = typeReal.newValue();
            for (int k = 4; true; k++) {
                kReal.set(k);
                multiply.apply(ceiled, kReal, sqrtLambda);
                add.apply(ceiled, ceiled, oneHalf);
                ceil.apply(ceiled, ceiled);
                left = m - ceiled.getInt();
                if (left <= 0) {
                    left = 0;
                    break;
                }
                multiply.apply(maxError, kReal, kReal);
                multiply.apply(maxError, oneHalf, maxError);
                addInverse.apply(maxError, maxError);
                exp.apply(maxError, maxError);
                multiply.apply(maxError, bl, maxError);
                divide.apply(maxError, maxError, kReal);
                multiply.apply(maxErrorT2, two, maxError);
                lt.apply(cmp, maxErrorT2, epsilon);
                if (cmp.getBoolean()) {
                    subtract.apply(epsilon, epsilon, maxError);
                    break;
                }
            }
        }

        ValueReal lambda_max = typeReal.newValue();
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        int m_max;
        if (m < 400) {
            set.apply(lambda_max, lambda_400);
            m_max = 400;
            multiply.apply(epsilon, epsilon, factor1);
        } else {
            ValueReal lf = typeReal.newValue();
            ValueReal factor = typeReal.newValue();
            add.apply(lf, lambda, one);
            divide.apply(lf, one, lf);
            subtract.apply(lf, one, lf);
            multiply.apply(factor, lf, factor2);
            set.apply(lambda_max, lambda);
            m_max = m;
            multiply.apply(epsilon, epsilon, factor);
        }
        int k;
        for (k = 4; true; k++) {
            kReal.set(k);
            multiply.apply(maxError, kReal, kReal);
            multiply.apply(maxError, oneHalf, maxError);
            addInverse.apply(maxError, maxError);
            exp.apply(maxError, maxError);
            multiply.apply(kTimesEpsilon, kReal, epsilon);
            gt.apply(cmp, kTimesEpsilon, maxError);
            if (cmp.getBoolean()) {
                break;
            }
        }
        kReal.set(k);        
        ValueReal rightAdd = typeReal.newValue();
        multiply.apply(rightAdd, two, lambda_max);
        pow.apply(rightAdd, rightAdd, oneHalf);
        multiply.apply(rightAdd, kReal, rightAdd);
        add.apply(rightAdd, rightAdd, oneHalf);
        ceil.apply(rightAdd, rightAdd);
        right = m_max + rightAdd.getInt();
        ValueReal checkValueReal = typeReal.newValue();
        add.apply(checkValueReal, lambda_max, one);
        multiply.apply(checkValueReal, checkValueReal, oneHalf);
        ceil.apply(checkValueReal, checkValueReal);
        if (right > checkValueReal.getInt() + m_max) {
            log.send(MessagesAlgorithm.FOX_GLYNN_UNRELIABLE_CANT_BOUND_RIGHT, right, lambda_max);
        }

        startValue = UtilValue.newValue(typeReal, right - left);
        multiply.apply(startValue, bigNumber, startValue);
        divide.apply(startValue, omega, startValue);

        if (m >= 25) {
            ValueReal result = typeReal.newValue();
            ValueReal logStartValue = typeReal.newValue();
            logOp.apply(logStartValue, startValue);
            subtract.apply(tau, tau, logStartValue);
            ValueReal log_c_m_inf = UtilValue.newValue(typeReal, m);
            logOp.apply(log_c_m_inf, log_c_m_inf);
            multiply.apply(log_c_m_inf, log_c_m_inf, oneHalf);
            subtract.apply(log_c_m_inf, vm1922272, log_c_m_inf);
            int i = m - left;

            if (i <= left) {
                ValueReal ii1 = UtilValue.newValue(typeReal, -i * (i+1));
                ValueReal v2i = UtilValue.newValue(typeReal, 2*i + 1);
                result.set(6);
                multiply.apply(result, result, lambda);
                divide.apply(result, v2i, result);
                add.apply(result, oneHalf, result);
                multiply.apply(result, ii1, result);
                divide.apply(result, result, lambda);
                add.apply(result, log_c_m_inf, result);
            } else {
                addInverse.apply(result, lambda);
                if (0 != left) {
                    ValueReal iReal = UtilValue.newValue(typeReal, i);
                    ValueReal result_1 = UtilValue.newValue(typeReal, m+1);
                    divide.apply(result_1, iReal, result_1);
                    subtract.apply(result_1, one, result_1);
                    logOp.apply(result_1, result_1);
                    multiply.apply(result_1, iReal, result_1);
                    add.apply(result_1, log_c_m_inf, result_1);
                    gt.apply(cmp, result_1, result);
                    if (cmp.getBoolean()) {
                        set.apply(result, result_1);
                    }
                }
            }
            lt.apply(cmp, result, tau);
            if (cmp.getBoolean()) {
                ValueReal log10_result = typeReal.newValue();
                multiply.apply(log10_result, result, log10_e);
                floor.apply(log10_result, log10_result);
                ValueReal exprRL10 = typeReal.newValue();
                divide.apply(exprRL10, log10_result, log10_e);
                subtract.apply(exprRL10, result, exprRL10);
                exp.apply(exprRL10, exprRL10);
                log.send(MessagesAlgorithm.FOX_GLYNN_UNRELIABLE_25, lambda, left, exprRL10, log10_result);
            }
            if (m >= 400) {
                i = right - m;
                ValueReal ii = UtilValue.newValue(typeReal, i * (i + 1));
                multiply.apply(result, two, lambda);
                divide.apply(result, ii, result);
                subtract.apply(result, log_c_m_inf, result);
                lt.apply(cmp, result, tau);
                if (cmp.getBoolean()) {
                    ValueReal log10_result = typeReal.newValue();
                    multiply.apply(log10_result, result, log10_e);
                    floor.apply(log10_result, log10_result);
                    ValueReal exprRL10 = typeReal.newValue();
                    divide.apply(exprRL10, log10_result, log10_e);
                    subtract.apply(exprRL10, result, exprRL10);
                    exp.apply(exprRL10, exprRL10);
                    log.send(MessagesAlgorithm.FOX_GLYNN_UNRELIABLE_400, lambda, right, exprRL10, log10_result);
                }
            }
        }

        // checked until here
    }

    private void weighter(int m) {
        assert left <= right : left + " " + right;
        values = UtilValue.newArray(typeArray, right - left + 1);
        values.set(startValue, m - left);
        ValueReal entry = typeReal.newValue();
        ValueReal leftSide = typeReal.newValue();
        ValueReal oldEntry = typeReal.newValue();
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeReal.get(), TypeReal.get());
        OperatorEvaluator gt = ContextValue.get().getEvaluator(OperatorGt.GT, TypeReal.get(), TypeReal.get());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeReal.get(), TypeReal.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeReal.get(), TypeReal.get());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        for (int j = m - left; j > 0; j--) {
            leftSide.set(j+left);
            values.get(oldEntry, j);
            divide.apply(entry, leftSide, lambda);
            multiply.apply(entry, entry, oldEntry);
            values.set(entry, j - 1);
        }

        int t = right - left;

        if (m < 400) {
            if (right > 600) {
                log.send(MessagesAlgorithm.FOX_GLYNN_UNDERFLOW_600);
                // TODO ask David why in his version he stops here
                // but not in other bad cases
            }

            ValueReal q = typeReal.newValue();
            ValueReal tauDq = typeReal.newValue();
            for (int j = m - left; j < t; j++) {
                values.get(entry, j);
                q.set(j + 1 + left);
                divide.apply(q, lambda, q);
                divide.apply(tauDq, tau, q);
                gt.apply(cmp, entry, tauDq);
                if (cmp.getBoolean()) {
                    multiply.apply(entry, q, entry);
                    values.set(entry,  j + 1);
                } else {
                    t = j;
                    right = j + left;
                    break;
                }
            }
        } else {
            ValueReal buff = typeReal.newValue();
            for (int j = m - left; j < t; j++) {
                values.get(entry, j);
                buff.set(j + 1 + left);
                divide.apply(buff, lambda, buff);
                multiply.apply(entry, buff, entry);
                values.set(entry,  j + 1);
            }
        }

        totalWeight = UtilValue.newValue(typeReal, 0);

        int j = 0;

        ValueReal entryJ = typeReal.newValue();
        ValueReal entryT = typeReal.newValue();
        OperatorEvaluator lt = ContextValue.get().getEvaluator(OperatorLt.LT, typeReal, typeReal);
        while (j < t) {
            values.get(entryJ, j);
            values.get(entryT, t);
            lt.apply(cmp, entryJ, entryT);
            if (cmp.getBoolean()) {
                add.apply(totalWeight, totalWeight, entryJ);
                j++;
            } else {
                add.apply(totalWeight, totalWeight, entryT);
                t--;
            }
        }
        values.get(entryJ, j);
        add.apply(totalWeight, totalWeight, entryJ);

        for (int i = left; i <= right; i++) {
            values.get(entry, i - left);
            divide.apply(entry, entry, totalWeight);
            values.set(entry, i - left);
        }        
    }

    private FoxGlynn(ValueReal lambda, ValueReal tau,
            ValueReal omega, ValueReal epsilon) {
        assert lambda != null;
        assert tau != null;
        assert omega != null;
        assert epsilon != null;
        Options options = Options.get();
        this.typeReal = TypeReal.get();

        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        /* store input parameters and derived stuff */
        this.log = options.get(OptionsMessages.LOG);
        this.lambda = typeReal.newValue();
        set.apply(this.lambda, lambda);
        this.tau = typeReal.newValue();
        set.apply(this.tau, tau);
        this.omega = typeReal.newValue();
        set.apply(this.omega, omega);
        this.epsilon = typeReal.newValue();
        set.apply(this.epsilon, epsilon);

        this.typeArray = TypeReal.get().getTypeArray();

        /* set constants */
        this.zero = UtilValue.newValue(typeReal, 0);
        this.one = UtilValue.newValue(typeReal, 1);
        this.two = UtilValue.newValue(typeReal, 2);
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeReal.get(), TypeReal.get());
        this.oneHalf = typeReal.newValue();
        divide.apply(this.oneHalf, one, two);
        this.oneEights = UtilValue.newValue(typeReal, 8);
        divide.apply(this.oneEights, one, oneEights);
        this.lambda_400 = UtilValue.newValue(typeReal, 400);
        this.sqrt_2_pi = UtilValue.newValue(typeReal, "2.50662827463100050241577");
        this.log10_e = UtilValue.newValue(typeReal, "0.434294481903251827651129");
        this.factor1 = UtilValue.newValue(typeReal, "0.662608824988162441697980");
        this.factor2 = UtilValue.newValue(typeReal, "0.664265347050632847802225");
        this.bigNumber = UtilValue.newValue(typeReal, "1.0e+10");
        this.vm1922272 = ValueReal.as(UtilValue.newValue(typeReal, "-1.922272"));

        OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeReal.get(), TypeReal.get());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        
        eq.apply(cmp, this.lambda, zero);
        if (cmp.getBoolean()) {
            left = 0;
            right = 0;
            values = UtilValue.newArray(typeArray, 1);
            values.set(one, 0);
            totalWeight = UtilValue.clone(one);
            startValue = typeReal.newValue();
        } else {
            OperatorEvaluator floor = ContextValue.get().getEvaluator(OperatorFloor.FLOOR, typeReal);
            ValueReal floorReal = typeReal.newValue();
            floor.apply(floorReal, this.lambda);
            int m = floorReal.getInt();
            finder(m);
            weighter(m);
        }
    }

    public FoxGlynn(ValueReal lambda, ValueReal epsilon) {
        this(lambda, getUnderflow(lambda), getOverflow(lambda), epsilon);
    }

    private static ValueReal getUnderflow(ValueReal value) {
        assert value != null;
        ValueReal underflow = TypeReal.get().newValue();
        OperatorEvaluator operatorUnderflow = ContextValue.get().getEvaluator(OperatorUnderflow.UNDERFLOW);
        operatorUnderflow.apply(underflow);
        return underflow;
    }

    private static ValueReal getOverflow(ValueReal value) {
        assert value != null;
        ValueReal overflow = TypeReal.get().newValue();
        OperatorEvaluator operatorOverflow = ContextValue.get().getEvaluator(OperatorOverflow.OVERFLOW);
        operatorOverflow.apply(overflow);
        return overflow;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public void getWeight(int index, ValueReal weight) {
        values.get(weight, index - left);
    }

    public ValueArrayAlgebra getArray() {
        return values;
    }
}
