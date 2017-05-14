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
import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorAnd;
import epmc.value.OperatorCeil;
import epmc.value.OperatorDivide;
import epmc.value.OperatorDivideIgnoreZero;
import epmc.value.OperatorEq;
import epmc.value.OperatorEvaluatorAdd;
import epmc.value.OperatorEvaluatorAddInverse;
import epmc.value.OperatorEvaluatorAnd;
import epmc.value.OperatorEvaluatorCeil;
import epmc.value.OperatorEvaluatorDivide;
import epmc.value.OperatorEvaluatorDivideIgnoreZero;
import epmc.value.OperatorEvaluatorEq;
import epmc.value.OperatorEvaluatorFloor;
import epmc.value.OperatorEvaluatorGe;
import epmc.value.OperatorEvaluatorGt;
import epmc.value.OperatorEvaluatorId;
import epmc.value.OperatorEvaluatorIff;
import epmc.value.OperatorEvaluatorImplies;
import epmc.value.OperatorEvaluatorIte;
import epmc.value.OperatorEvaluatorLe;
import epmc.value.OperatorEvaluatorLog;
import epmc.value.OperatorEvaluatorLt;
import epmc.value.OperatorEvaluatorMax;
import epmc.value.OperatorEvaluatorMin;
import epmc.value.OperatorEvaluatorMod;
import epmc.value.OperatorEvaluatorMultiply;
import epmc.value.OperatorEvaluatorMultiplyInverse;
import epmc.value.OperatorEvaluatorNe;
import epmc.value.OperatorEvaluatorNot;
import epmc.value.OperatorEvaluatorOr;
import epmc.value.OperatorEvaluatorPow;
import epmc.value.OperatorEvaluatorSubtract;
import epmc.value.OperatorFloor;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorId;
import epmc.value.OperatorIff;
import epmc.value.OperatorImplies;
import epmc.value.OperatorIte;
import epmc.value.OperatorLe;
import epmc.value.OperatorLog;
import epmc.value.OperatorLt;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorMod;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorMultiplyInverse;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.OperatorPow;
import epmc.value.OperatorSubtract;
import epmc.value.TypeBoolean;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;

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
        ContextValue.get().addOrSetOperator(OperatorAdd.IDENTIFIER, OperatorAdd.class);
        ContextValue.get().addOrSetOperator(OperatorAddInverse.IDENTIFIER, OperatorAddInverse.class);
        ContextValue.get().addOrSetOperator(OperatorAnd.IDENTIFIER, OperatorAnd.class);
        ContextValue.get().addOrSetOperator(OperatorCeil.IDENTIFIER, OperatorCeil.class);
        ContextValue.get().addOrSetOperator(OperatorDivide.IDENTIFIER, OperatorDivide.class);
        ContextValue.get().addOrSetOperator(OperatorDivideIgnoreZero.IDENTIFIER, OperatorDivideIgnoreZero.class);
        ContextValue.get().addOrSetOperator(OperatorEq.IDENTIFIER, OperatorEq.class);
        ContextValue.get().addOrSetOperator(OperatorFloor.IDENTIFIER, OperatorFloor.class);
        ContextValue.get().addOrSetOperator(OperatorGe.IDENTIFIER, OperatorGe.class);
        ContextValue.get().addOrSetOperator(OperatorGt.IDENTIFIER, OperatorGt.class);
        ContextValue.get().addOrSetOperator(OperatorId.IDENTIFIER, OperatorId.class);
        ContextValue.get().addOrSetOperator(OperatorIff.IDENTIFIER, OperatorIff.class);
        ContextValue.get().addOrSetOperator(OperatorImplies.IDENTIFIER, OperatorImplies.class);
        ContextValue.get().addOrSetOperator(OperatorIte.IDENTIFIER, OperatorIte.class);
        ContextValue.get().addOrSetOperator(OperatorLe.IDENTIFIER, OperatorLe.class);
        ContextValue.get().addOrSetOperator(OperatorLog.IDENTIFIER, OperatorLog.class);
        ContextValue.get().addOrSetOperator(OperatorLt.IDENTIFIER, OperatorLt.class);
        ContextValue.get().addOrSetOperator(OperatorMax.IDENTIFIER, OperatorMax.class);
        ContextValue.get().addOrSetOperator(OperatorMin.IDENTIFIER, OperatorMin.class);
        ContextValue.get().addOrSetOperator(OperatorMod.IDENTIFIER, OperatorMod.class);
        ContextValue.get().addOrSetOperator(OperatorMultiply.IDENTIFIER, OperatorMultiply.class);
        ContextValue.get().addOrSetOperator(OperatorMultiplyInverse.IDENTIFIER, OperatorMultiplyInverse.class);
        ContextValue.get().addOrSetOperator(OperatorNe.IDENTIFIER, OperatorNe.class);
        ContextValue.get().addOrSetOperator(OperatorNot.IDENTIFIER, OperatorNot.class);
        ContextValue.get().addOrSetOperator(OperatorOr.IDENTIFIER, OperatorOr.class);
        ContextValue.get().addOrSetOperator(OperatorPow.IDENTIFIER, OperatorPow.class);
        ContextValue.get().addOrSetOperator(OperatorSubtract.IDENTIFIER, OperatorSubtract.class);
        
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAdd.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAddInverse.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAnd.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorCeil.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorDivide.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorDivideIgnoreZero.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorEq.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorFloor.INSTANCE);
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
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMod.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMultiply.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMultiplyInverse.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorNe.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorNot.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorOr.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorPow.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorSubtract.INSTANCE);
    }

    private static void addTypes() {
    	TypeWeight.set(new TypeDouble(null, null));
    	TypeWeightTransition.set(new TypeDouble(null, null));
    	TypeReal.set(new TypeDouble(null, null));
    	TypeInterval.set(new TypeInterval());
    	TypeBoolean.set(new TypeBoolean());
    	TypeInteger.set(new TypeInteger());
	}
}
