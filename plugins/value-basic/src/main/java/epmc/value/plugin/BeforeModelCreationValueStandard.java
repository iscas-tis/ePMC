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
import epmc.value.TypeBooleanJava;
import epmc.value.TypeDouble;
import epmc.value.TypeDoubleJava;
import epmc.value.TypeInteger;
import epmc.value.TypeIntegerJava;
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
import epmc.value.operatorevaluator.OperatorEvaluatorLnDouble;
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
import epmc.value.operatorevaluator.OperatorEvaluatorOverflowDouble;
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
import epmc.value.operatorevaluator.OperatorEvaluatorUnderflowDouble;

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
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddInverseDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddInverseInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAnd.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorCeilDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorDivideDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorDivideInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorDivideIgnoreZeroDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorEqDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorEqArrayDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorEqInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorEqBoolean.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorFloor.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorExpDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorGeDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorGeInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorGtDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorGtInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorId.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIff.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorImplies.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIte.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIteInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIteBoolean.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIteDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorLeDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorLeInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorLnDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorLogDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorLtDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorLtInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMaxDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMaxInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMinDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMinInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorModInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMultiplyDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMultiplyInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMultiplyInverseDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorNe.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorNeDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorNeInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorNot.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorOr.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorPowDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSubtractDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSubtractInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSqrtDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSubtractInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorDistanceDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorDistanceInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorEqInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsNegInfDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsPosInfDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsZeroDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsOneDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsZeroInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsOneInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsOneInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsZeroInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMultiplyInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetAlgebraInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetDoubleDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetIntInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetBooleanBoolean.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetIntervalInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetIntervalReal.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetArrayArray.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetEnumEnum.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetObjectObject.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorEqEnum.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorUnderflowDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorOverflowDouble.Builder.class);
    }

    private static void addTypes() {
        TypeDouble typeDouble = new TypeDoubleJava(null, null);
        TypeWeight.set(new TypeDoubleJava(null, null));
        TypeWeightTransition.set(new TypeDoubleJava(null, null));
        TypeReal.set(typeDouble);
        TypeInterval.set(new TypeInterval());
        TypeBoolean.set(new TypeBooleanJava());
        TypeInteger.set(new TypeIntegerJava());
        TypeDouble.set(typeDouble);
    }
}
