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
import epmc.value.TypeUnknown;
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
        ContextValue.get().addOrSetOperator(OperatorAdd.class);
        ContextValue.get().addOrSetOperator(OperatorAddInverse.class);
        ContextValue.get().addOrSetOperator(OperatorAnd.class);
        ContextValue.get().addOrSetOperator(OperatorCeil.class);
        ContextValue.get().addOrSetOperator(OperatorDivide.class);
        ContextValue.get().addOrSetOperator(OperatorDivideIgnoreZero.class);
        ContextValue.get().addOrSetOperator(OperatorEq.class);
        ContextValue.get().addOrSetOperator(OperatorFloor.class);
        ContextValue.get().addOrSetOperator(OperatorGe.class);
        ContextValue.get().addOrSetOperator(OperatorGt.class);
        ContextValue.get().addOrSetOperator(OperatorId.class);
        ContextValue.get().addOrSetOperator(OperatorIff.class);
        ContextValue.get().addOrSetOperator(OperatorImplies.class);
        ContextValue.get().addOrSetOperator(OperatorIte.class);
        ContextValue.get().addOrSetOperator(OperatorLe.class);
        ContextValue.get().addOrSetOperator(OperatorLog.class);
        ContextValue.get().addOrSetOperator(OperatorLt.class);
        ContextValue.get().addOrSetOperator(OperatorMax.class);
        ContextValue.get().addOrSetOperator(OperatorMin.class);
        ContextValue.get().addOrSetOperator(OperatorMod.class);
        ContextValue.get().addOrSetOperator(OperatorMultiply.class);
        ContextValue.get().addOrSetOperator(OperatorMultiplyInverse.class);
        ContextValue.get().addOrSetOperator(OperatorNe.class);
        ContextValue.get().addOrSetOperator(OperatorNot.class);
        ContextValue.get().addOrSetOperator(OperatorOr.class);
        ContextValue.get().addOrSetOperator(OperatorPow.class);
        ContextValue.get().addOrSetOperator(OperatorSubtract.class);
    }

    private static void addTypes() {
    	TypeWeight.set(new TypeDouble(null, null));
    	TypeWeightTransition.set(new TypeDouble(null, null));
    	TypeReal.set(new TypeDouble(null, null));
    	TypeInterval.set(new TypeInterval());
    	TypeBoolean.set(new TypeBoolean());
    	TypeUnknown.set(new TypeUnknown());
    	TypeInteger.set(new TypeInteger());
	}
}
