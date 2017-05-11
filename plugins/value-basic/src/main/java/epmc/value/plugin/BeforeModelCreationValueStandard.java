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
