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
import epmc.value.OperatorEvaluatorIntegerMod;
import epmc.value.OperatorEvaluatorMultiply;
import epmc.value.OperatorEvaluatorMultiplyInverse;
import epmc.value.OperatorEvaluatorNe;
import epmc.value.OperatorEvaluatorNot;
import epmc.value.OperatorEvaluatorOr;
import epmc.value.OperatorEvaluatorPow;
import epmc.value.OperatorEvaluatorSubtract;
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
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorIntegerMod.INSTANCE);
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
