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

package epmc.value.plugin;

import epmc.error.EPMCException;
import epmc.plugin.BeforeModelCreation;
import epmc.value.ContextValue;
import epmc.value.TypeBoolean;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;
import epmc.value.operatorevaluator.OperatorEvaluatorAdd;
import epmc.value.operatorevaluator.OperatorEvaluatorAddInverse;
import epmc.value.operatorevaluator.OperatorEvaluatorAnd;
import epmc.value.operatorevaluator.OperatorEvaluatorCeil;
import epmc.value.operatorevaluator.OperatorEvaluatorDivide;
import epmc.value.operatorevaluator.OperatorEvaluatorDivideIgnoreZero;
import epmc.value.operatorevaluator.OperatorEvaluatorEq;
import epmc.value.operatorevaluator.OperatorEvaluatorExp;
import epmc.value.operatorevaluator.OperatorEvaluatorFloor;
import epmc.value.operatorevaluator.OperatorEvaluatorGe;
import epmc.value.operatorevaluator.OperatorEvaluatorGt;
import epmc.value.operatorevaluator.OperatorEvaluatorId;
import epmc.value.operatorevaluator.OperatorEvaluatorIff;
import epmc.value.operatorevaluator.OperatorEvaluatorImplies;
import epmc.value.operatorevaluator.OperatorEvaluatorIntegerMod;
import epmc.value.operatorevaluator.OperatorEvaluatorIte;
import epmc.value.operatorevaluator.OperatorEvaluatorLe;
import epmc.value.operatorevaluator.OperatorEvaluatorLog;
import epmc.value.operatorevaluator.OperatorEvaluatorLt;
import epmc.value.operatorevaluator.OperatorEvaluatorMax;
import epmc.value.operatorevaluator.OperatorEvaluatorMin;
import epmc.value.operatorevaluator.OperatorEvaluatorMultiply;
import epmc.value.operatorevaluator.OperatorEvaluatorMultiplyInverseDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorNe;
import epmc.value.operatorevaluator.OperatorEvaluatorNot;
import epmc.value.operatorevaluator.OperatorEvaluatorOr;
import epmc.value.operatorevaluator.OperatorEvaluatorPow;
import epmc.value.operatorevaluator.OperatorEvaluatorSqrt;
import epmc.value.operatorevaluator.OperatorEvaluatorSubtract;

public final class BeforeModelCreationValueStandard implements BeforeModelCreation {
	private final static String IDENTIFIER = "before-model-creation-value-standard";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}


	@Override
	public void process() throws EPMCException {
		addOperators();
		addTypes();
	}

	private static void addOperators() {        
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAdd.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAddInverse.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAnd.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorCeil.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorDivide.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorDivideIgnoreZero.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorEq.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorFloor.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorExp.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorGe.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorGt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorId.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorIff.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorImplies.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorIte.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorLe.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorLog.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorLt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMax.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMin.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorIntegerMod.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMultiply.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMultiplyInverseDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorNe.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorNot.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorOr.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorPow.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorSubtract.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorSqrt.INSTANCE);
    }

    private static void addTypes() {
    	TypeDouble typeDouble = new TypeDouble(null, null);
    	TypeWeight.set(new TypeDouble(null, null));
    	TypeWeightTransition.set(new TypeDouble(null, null));
    	TypeReal.set(typeDouble);
    	TypeInterval.set(new TypeInterval());
    	TypeBoolean.set(new TypeBoolean());
    	TypeInteger.set(new TypeInteger());
    	TypeDouble.set(typeDouble);
	}
}
