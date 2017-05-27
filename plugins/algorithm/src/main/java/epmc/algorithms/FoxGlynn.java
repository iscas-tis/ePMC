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

import epmc.error.EPMCException;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.OperatorCeil;
import epmc.value.OperatorEvaluator;
import epmc.value.OperatorFloor;
import epmc.value.TypeArray;
import epmc.value.TypeHasNativeArray;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.ValueArrayAlgebra;
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

    private void finder(int m) throws EPMCException {
        OperatorEvaluator ceil = ContextValue.get().getOperatorEvaluator(OperatorCeil.CEIL, typeReal);
        OperatorEvaluator floor = ContextValue.get().getOperatorEvaluator(OperatorFloor.FLOOR, typeReal);
    	ValueReal kTimesEpsilon = typeReal.newValue();
        ValueReal tau = typeReal.newValue();
        ValueReal e = UtilValue.newValue(TypeReal.get(), UtilValue.LOG);
        tau.log(this.tau, e);
        ValueReal epsilon = typeReal.newValue();
        epsilon.multiply(this.epsilon, sqrt_2_pi);
        ValueReal kReal = typeReal.newValue();
        ValueReal maxError = typeReal.newValue();
        if (m < 25) {
        	ValueReal minusLambda = typeReal.newValue();
            minusLambda.addInverse(lambda);
            ValueReal expMinusLambda = typeReal.newValue();
            expMinusLambda.exp(minusLambda);
            if (minusLambda.isLt(tau)) {
                log.send(MessagesAlgorithm.FOX_GLYNN_UNRELIABLE_EXP_BELOW_TAU, lambda, expMinusLambda);
            }
            left = 0;
        } else {
            // const double bl = (1 + 1 / lambda) * exp((1/lambda) * 0.125);
            ValueReal oneDivLambda = typeReal.newValue();
            oneDivLambda.divide(one, lambda);
            ValueReal onePlusOneDivLambda = typeReal.newValue();
            onePlusOneDivLambda.add(one, oneDivLambda);
            ValueReal bl = typeReal.newValue();
            bl.multiply(oneDivLambda, oneEights);
            bl.exp(bl);
            bl.multiply(onePlusOneDivLambda, bl);
            ValueReal sqrtLambda = typeReal.newValue();
            sqrtLambda.pow(lambda, oneHalf);
            
            ValueReal ceiled = typeReal.newValue();
            ValueReal maxErrorT2 = typeReal.newValue();
            for (int k = 4; true; k++) {
                kReal.set(k);
                ceiled.multiply(kReal, sqrtLambda);
                ceiled.add(ceiled, oneHalf);
                ceil.apply(ceiled, ceiled);
                left = m - ceiled.getInt();
                if (left <= 0) {
                    left = 0;
                    break;
                }
                maxError.multiply(kReal, kReal);
                maxError.multiply(oneHalf, maxError);
                maxError.addInverse(maxError);
                maxError.exp(maxError);
                maxError.multiply(bl, maxError);
                maxError.divide(maxError, kReal);
                maxErrorT2.multiply(two, maxError);
                if (maxErrorT2.isLt(epsilon)) {
                    epsilon.subtract(epsilon, maxError);
                    break;
                }
            }
        }
        
        ValueReal lambda_max = typeReal.newValue();
        int m_max;
        if (m < 400) {
            lambda_max.set(lambda_400);
            m_max = 400;
            epsilon.multiply(epsilon, factor1);
        } else {
            ValueReal lf = typeReal.newValue();
            ValueReal factor = typeReal.newValue();
            lf.add(lambda, one);
            lf.divide(one, lf);
            lf.subtract(one, lf);
            factor.multiply(lf, factor2);
            lambda_max.set(lambda);
            m_max = m;
            epsilon.multiply(epsilon, factor);
        }
        int k;
        for (k = 4; true; k++) {
            kReal.set(k);
            maxError.multiply(kReal, kReal);
            maxError.multiply(oneHalf, maxError);
            maxError.addInverse(maxError);
            maxError.exp(maxError);
            kTimesEpsilon.multiply(kReal, epsilon);
            if (kTimesEpsilon.isGt(maxError)) {
                break;
            }
        }
        kReal.set(k);        
        ValueReal rightAdd = typeReal.newValue();
        rightAdd.multiply(two, lambda_max);
        rightAdd.pow(rightAdd, oneHalf);
        rightAdd.multiply(kReal, rightAdd);
        rightAdd.add(rightAdd, oneHalf);
        ceil.apply(rightAdd, rightAdd);
        right = m_max + rightAdd.getInt();
        ValueReal checkValueReal = typeReal.newValue();
        checkValueReal.add(lambda_max, one);
        checkValueReal.multiply(checkValueReal, oneHalf);
        ceil.apply(checkValueReal, checkValueReal);
        if (right > checkValueReal.getInt() + m_max) {
            log.send(MessagesAlgorithm.FOX_GLYNN_UNRELIABLE_CANT_BOUND_RIGHT, right, lambda_max);
        }

        startValue = UtilValue.newValue(typeReal, right - left);
        startValue.multiply(bigNumber, startValue);
        startValue.divide(omega, startValue);
        
        if (m >= 25) {
            ValueReal result = typeReal.newValue();
            ValueReal logStartValue = typeReal.newValue();
            logStartValue.log(startValue, e);
            tau.subtract(tau, logStartValue);
            ValueReal log_c_m_inf = UtilValue.newValue(typeReal, m);
            log_c_m_inf.log(log_c_m_inf, e);
            log_c_m_inf.multiply(log_c_m_inf, oneHalf);
            log_c_m_inf.subtract(vm1922272, log_c_m_inf);
            int i = m - left;
            
            if (i <= left) {
            	ValueReal ii1 = UtilValue.newValue(typeReal, -i * (i+1));
            	ValueReal v2i = UtilValue.newValue(typeReal, 2*i + 1);
                result.set(6);
                result.multiply(result, lambda);
                result.divide(v2i, result);
                result.add(oneHalf, result);
                result.multiply(ii1, result);
                result.divide(result, lambda);
                result.add(log_c_m_inf, result);
            } else {
                result.addInverse(lambda);
                if (0 != left) {
                	ValueReal iReal = UtilValue.newValue(typeReal, i);
                    ValueReal result_1 = UtilValue.newValue(typeReal, m+1);
                    result_1.divide(iReal, result_1);
                    result_1.subtract(one, result_1);
                    result_1.log(result_1, e);
                    result_1.multiply(iReal, result_1);
                    result_1.add(log_c_m_inf, result_1);
                    if (result_1.isGt(result)) {
                        result.set(result_1);
                    }
                }
            }
            
            if (result.isLt(tau)) {
                ValueReal log10_result = typeReal.newValue();
                log10_result.multiply(result, log10_e);
                floor.apply(log10_result, log10_result);
                ValueReal exprRL10 = typeReal.newValue();
                exprRL10.divide(log10_result, log10_e);
                exprRL10.subtract(result, exprRL10);
                exprRL10.exp(exprRL10);
                log.send(MessagesAlgorithm.FOX_GLYNN_UNRELIABLE_25, lambda, left, exprRL10, log10_result);
            }
            if (m >= 400) {
                i = right - m;
                ValueReal ii = UtilValue.newValue(typeReal, i * (i + 1));
                result.multiply(two, lambda);
                result.divide(ii, result);
                result.subtract(log_c_m_inf, result);
                if (result.isLt(tau)) {
                    ValueReal log10_result = typeReal.newValue();
                    log10_result.multiply(result, log10_e);
                    floor.apply(log10_result, log10_result);
                    ValueReal exprRL10 = typeReal.newValue();
                    exprRL10.divide(log10_result, log10_e);
                    exprRL10.subtract(result, exprRL10);
                    exprRL10.exp(exprRL10);
                    log.send(MessagesAlgorithm.FOX_GLYNN_UNRELIABLE_400, lambda, right, exprRL10, log10_result);
                }
            }
        }
        
        // checked until here
    }
    
    private void weighter(int m) throws EPMCException {
        assert left <= right : left + " " + right;
        values = UtilValue.newArray(typeArray, right - left + 1);
        values.set(startValue, m - left);
        ValueReal entry = typeReal.newValue();
        ValueReal leftSide = typeReal.newValue();
        ValueReal oldEntry = typeReal.newValue();
        for (int j = m - left; j > 0; j--) {
            leftSide.set(j+left);
            values.get(oldEntry, j);
            entry.divide(leftSide, lambda);
            entry.multiply(entry, oldEntry);
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
                q.divide(lambda, q);
                tauDq.divide(tau, q);
                if (entry.isGt(tauDq)) {
                    entry.multiply(q, entry);
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
                buff.divide(lambda, buff);
                entry.multiply(buff, entry);
                values.set(entry,  j + 1);
            }
        }
        
        totalWeight = UtilValue.newValue(typeReal, 0);
        
        int j = 0;

        ValueReal entryJ = typeReal.newValue();
        ValueReal entryT = typeReal.newValue();
        while (j < t) {
            values.get(entryJ, j);
            values.get(entryT, t);
            if (entryJ.isLt(entryT)) {
                totalWeight.add(totalWeight, entryJ);
                j++;
            } else {
                totalWeight.add(totalWeight, entryT);
                t--;
            }
        }
        values.get(entryJ, j);
        totalWeight.add(totalWeight, entryJ);
        
        for (int i = left; i <= right; i++) {
            values.get(entry, i - left);
            entry.divide(entry, totalWeight);
            values.set(entry, i - left);
        }        
    }
    
    private FoxGlynn(ValueReal lambda, ValueReal tau,
    		ValueReal omega, ValueReal epsilon, boolean useNative) throws EPMCException {
        assert lambda != null;
        assert tau != null;
        assert omega != null;
        assert epsilon != null;
        Options options = Options.get();
        assert TypeReal.isReal(lambda.getType());
        assert TypeReal.isReal(tau.getType());
        assert TypeReal.isReal(omega.getType());
        assert TypeReal.isReal(epsilon.getType());
        assert lambda.isGe(TypeReal.get().getZero()) : lambda;
        assert !lambda.isPosInf();
        this.typeReal = TypeReal.get();
        
        /* store input parameters and derived stuff */
        this.log = options.get(OptionsMessages.LOG);
        this.lambda = typeReal.newValue();
        this.lambda.set(lambda);
        this.tau = typeReal.newValue();
        this.tau.set(tau);
        this.omega = typeReal.newValue();
        this.omega.set(omega);
        this.epsilon = typeReal.newValue();
        this.epsilon.set(epsilon);
        
        this.typeArray = useNative
                ? TypeHasNativeArray.asHasNativeArray(TypeReal.get()).getTypeArrayNative()
                : TypeReal.get().getTypeArray();

        /* set constants */
        this.zero = UtilValue.newValue(typeReal, 0);
        this.one = UtilValue.newValue(typeReal, 1);
        this.two = UtilValue.newValue(typeReal, 2);
        this.oneHalf = typeReal.newValue();
        this.oneHalf.divide(one, two);
        this.oneEights = UtilValue.newValue(typeReal, 8);
        this.oneEights.divide(one, oneEights);
        this.lambda_400 = UtilValue.newValue(typeReal, 400);
        this.sqrt_2_pi = UtilValue.newValue(typeReal, "2.50662827463100050241577");
        this.log10_e = UtilValue.newValue(typeReal, "0.434294481903251827651129");
        this.factor1 = UtilValue.newValue(typeReal, "0.662608824988162441697980");
        this.factor2 = UtilValue.newValue(typeReal, "0.664265347050632847802225");
        this.bigNumber = UtilValue.newValue(typeReal, "1.0e+10");
        this.vm1922272 = ValueReal.asReal(UtilValue.newValue(typeReal, "-1.922272"));

        assert this.lambda.isGe(this.zero);
        assert this.omega.isGt(this.zero);
        assert this.tau.isLt(this.omega);
        assert this.tau.isGt(this.zero);
        assert this.epsilon.isGt(this.tau);
        assert this.epsilon.isGt(this.zero);
        
        if (this.lambda.isEq(zero)) {
            left = 0;
            right = 0;
            values = UtilValue.newArray(typeArray, 1);
            values.set(one, 0);
            totalWeight = UtilValue.clone(one);
            startValue = typeReal.newValue();
        } else {
            OperatorEvaluator floor = ContextValue.get().getOperatorEvaluator(OperatorFloor.FLOOR, typeReal);
            ValueReal floorReal = typeReal.newValue();
            floor.apply(floorReal, this.lambda);
            int m = floorReal.getInt();
            finder(m);
            weighter(m);
        }
    }
    
    public FoxGlynn(ValueReal lambda, ValueReal epsilon, boolean useNative)
            throws EPMCException {
        this(lambda, getUnderflow(lambda), getOverflow(lambda), epsilon, useNative);
    }

    private static ValueReal getUnderflow(ValueReal value) {
        assert value != null;
        return TypeReal.get().getUnderflow();
    }
    
    private static ValueReal getOverflow(ValueReal value) {
        assert value != null;
        return TypeReal.get().getOverflow();
    }
    
    public int getLeft() {
        return left;
    }
    
    public int getRight() {
        return right;
    }
    
    public void getWeight(int index, ValueReal weight) throws EPMCException {
        values.get(weight, index - left);
    }

    public ValueArrayAlgebra getArray() {
        return values;
    }
    
    public TypeReal getTypeReal() {
        return vm1922272.getType();
    }
}
