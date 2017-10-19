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
import epmc.value.operatorevaluator.OperatorEvaluatorAddInterval;
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
import epmc.value.operatorevaluator.OperatorEvaluatorEqEnum;
import epmc.value.operatorevaluator.OperatorEvaluatorEqInt;
import epmc.value.operatorevaluator.OperatorEvaluatorEqInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorExpDouble;
import epmc.value.operatorevaluator.SimpleEvaluatorFactory;
import epmc.value.operatorevaluator.OperatorEvaluatorFloor;
import epmc.value.operatorevaluator.OperatorEvaluatorGeDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorGeInt;
import epmc.value.operatorevaluator.OperatorEvaluatorGtDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorGtInt;
import epmc.value.operatorevaluator.OperatorEvaluatorId;
import epmc.value.operatorevaluator.OperatorEvaluatorIff;
import epmc.value.operatorevaluator.OperatorEvaluatorImplies;
import epmc.value.operatorevaluator.OperatorEvaluatorIsNegInfDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorIsOneDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorIsOneInt;
import epmc.value.operatorevaluator.OperatorEvaluatorIsOneInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorIsPosInfDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorIsZeroDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorIsZeroInt;
import epmc.value.operatorevaluator.OperatorEvaluatorIsZeroInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorModInt;
import epmc.value.operatorevaluator.OperatorEvaluatorIte;
import epmc.value.operatorevaluator.OperatorEvaluatorIteBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorIteDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorIteInt;
import epmc.value.operatorevaluator.OperatorEvaluatorLeDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorLeInt;
import epmc.value.operatorevaluator.OperatorEvaluatorLogDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorLtDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorLtInt;
import epmc.value.operatorevaluator.OperatorEvaluatorMaxDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorMaxInt;
import epmc.value.operatorevaluator.OperatorEvaluatorMinDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorMinInt;
import epmc.value.operatorevaluator.OperatorEvaluatorMultiplyDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorMultiplyInt;
import epmc.value.operatorevaluator.OperatorEvaluatorMultiplyInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorMultiplyInverseDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorNe;
import epmc.value.operatorevaluator.OperatorEvaluatorNeDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorNeInt;
import epmc.value.operatorevaluator.OperatorEvaluatorNot;
import epmc.value.operatorevaluator.OperatorEvaluatorOr;
import epmc.value.operatorevaluator.OperatorEvaluatorPowDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorSetAlgebraInt;
import epmc.value.operatorevaluator.OperatorEvaluatorSetArrayArray;
import epmc.value.operatorevaluator.OperatorEvaluatorSetBooleanBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorSetDoubleDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorSetEnumEnum;
import epmc.value.operatorevaluator.OperatorEvaluatorSetIntInt;
import epmc.value.operatorevaluator.OperatorEvaluatorSetIntervalInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorSetIntervalReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSetObjectObject;
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
        ContextValue.get().addEvaluatorFactory(SimpleEvaluatorFactory.get());
//        OperatorEvaluatorFactorySimple.get().addEvaluator(OperatorEvaluatorAddDouble.Builder.class);
        ContextValue.get().addEvaluator(OperatorEvaluatorAddDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorAddInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorAddInverseDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorAddInverseInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorAnd.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorCeilDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorDivideDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorDivideInterval.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorDivideIgnoreZeroDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorEqDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorEqArrayDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorEqInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorEqBoolean.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorFloor.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorExpDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorGeDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorGeInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorGtDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorGtInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorId.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIff.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorImplies.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIte.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIteInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIteBoolean.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIteDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorLeDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorLeInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorLogDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorLtDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorLtInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorMaxDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorMaxInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorMinDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorMinInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorModInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorMultiplyDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorMultiplyInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorMultiplyInverseDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorNe.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorNeDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorNeInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorNot.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorOr.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorPowDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorSubtractDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorSubtractInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorSqrtDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorAddInterval.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorSubtractInterval.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorDistanceDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorDistanceInterval.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorEqInterval.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIsNegInfDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIsPosInfDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIsZeroDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIsOneDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIsZeroInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIsOneInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIsOneInterval.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorIsZeroInterval.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorMultiplyInterval.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorSetAlgebraInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorSetDoubleDouble.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorSetIntInt.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorSetBooleanBoolean.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorSetIntervalInterval.IDENTIFIER);
        ContextValue.get().addEvaluator(OperatorEvaluatorSetIntervalReal.IDENTIFIER);
        ContextValue.get().addEvaluator(OperatorEvaluatorSetArrayArray.IDENTIFIER);
        ContextValue.get().addEvaluator(OperatorEvaluatorSetEnumEnum.IDENTIFIER);
        ContextValue.get().addEvaluator(OperatorEvaluatorSetObjectObject.INSTANCE);
        ContextValue.get().addEvaluator(OperatorEvaluatorEqEnum.INSTANCE);
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
