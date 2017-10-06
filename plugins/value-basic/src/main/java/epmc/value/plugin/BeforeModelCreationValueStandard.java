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

import epmc.plugin.BeforeModelCreation;
import epmc.value.ContextValue;
import epmc.value.TypeBoolean;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;
import epmc.value.operatorevaluator.OperatorEvaluatorAddDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorAddInt;
import epmc.value.operatorevaluator.OperatorEvaluatorAdd;
import epmc.value.operatorevaluator.OperatorEvaluatorAddInverseDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorAddInverseInt;
import epmc.value.operatorevaluator.OperatorEvaluatorAnd;
import epmc.value.operatorevaluator.OperatorEvaluatorCeilDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorDistanceDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorDistanceInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorDivideDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorDivideIgnoreZeroDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorDivideInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorEqArrayDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorEqBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorEqDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorEqInt;
import epmc.value.operatorevaluator.OperatorEvaluatorEqInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorExpDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorFloor;
import epmc.value.operatorevaluator.OperatorEvaluatorGe;
import epmc.value.operatorevaluator.OperatorEvaluatorGeDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorGeInt;
import epmc.value.operatorevaluator.OperatorEvaluatorGt;
import epmc.value.operatorevaluator.OperatorEvaluatorGtDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorGtInt;
import epmc.value.operatorevaluator.OperatorEvaluatorId;
import epmc.value.operatorevaluator.OperatorEvaluatorIff;
import epmc.value.operatorevaluator.OperatorEvaluatorImplies;
import epmc.value.operatorevaluator.OperatorEvaluatorIsNegInfDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorIsPosInfDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorModInt;
import epmc.value.operatorevaluator.OperatorEvaluatorIte;
import epmc.value.operatorevaluator.OperatorEvaluatorLe;
import epmc.value.operatorevaluator.OperatorEvaluatorLeDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorLeInt;
import epmc.value.operatorevaluator.OperatorEvaluatorLogDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorLtDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorLtInt;
import epmc.value.operatorevaluator.OperatorEvaluatorMax;
import epmc.value.operatorevaluator.OperatorEvaluatorMaxDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorMaxInt;
import epmc.value.operatorevaluator.OperatorEvaluatorMin;
import epmc.value.operatorevaluator.OperatorEvaluatorMinDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorMinInt;
import epmc.value.operatorevaluator.OperatorEvaluatorMultiply;
import epmc.value.operatorevaluator.OperatorEvaluatorMultiplyDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorMultiplyInt;
import epmc.value.operatorevaluator.OperatorEvaluatorMultiplyInverseDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorNe;
import epmc.value.operatorevaluator.OperatorEvaluatorNeDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorNeInt;
import epmc.value.operatorevaluator.OperatorEvaluatorNot;
import epmc.value.operatorevaluator.OperatorEvaluatorOr;
import epmc.value.operatorevaluator.OperatorEvaluatorPowDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorSqrtDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorSubtractDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorSubtractInt;
import epmc.value.operatorevaluator.OperatorEvaluatorSubtractInterval;

public final class BeforeModelCreationValueStandard implements BeforeModelCreation {
    private final static String IDENTIFIER = "before-model-creation-value-standard";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }


    @Override
    public void process() {
        addOperators();
        addTypes();
    }

    private static void addOperators() {        
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAdd.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAddDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAddInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAddInverseDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAddInverseInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorAnd.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorCeilDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorDivideDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorDivideInterval.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorDivideIgnoreZeroDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorEqDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorEqArrayDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorEqInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorEqBoolean.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorFloor.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorExpDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorGe.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorGeDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorGeInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorGt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorGtDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorGtInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorId.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorIff.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorImplies.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorIte.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorLe.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorLeDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorLeInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorLogDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorLtDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorLtInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMax.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMaxDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMaxInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMin.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMinDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMinInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorModInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMultiply.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMultiplyDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMultiplyInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorMultiplyInverseDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorNe.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorNeDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorNeInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorNot.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorOr.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorPowDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorSubtractDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorSubtractInt.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorSqrtDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorSubtractInterval.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorDistanceDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorDistanceInterval.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorEqInterval.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorIsNegInfDouble.INSTANCE);
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorIsPosInfDouble.INSTANCE);
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
