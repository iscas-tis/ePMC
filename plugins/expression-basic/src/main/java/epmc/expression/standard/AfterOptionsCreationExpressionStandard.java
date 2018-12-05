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

package epmc.expression.standard;

import java.util.Map;

import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.evaluatordd.EvaluatorDD;
import epmc.expression.standard.evaluatordd.EvaluatorDDLiteral;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorGeneral;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorVectorAdd;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorVectorAddInverse;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorVectorEq;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorVectorGe;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorVectorGt;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorVectorLe;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorVectorLt;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorVectorMax;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorVectorMin;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorVectorNe;
import epmc.expression.standard.evaluatordd.EvaluatorDDOperatorVectorSub;
import epmc.expression.standard.evaluatordd.EvaluatorDDVariable;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitLiteralInteger;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitLiteralReal;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitIntegerVariable;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitLiteralBoolean;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitOperator;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitOperatorBinaryIntegerToBoolean;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitOperatorBinaryIntegerToInteger;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitOperatorShortcutAnd;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitOperatorShortcutIfThenElse;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitOperatorShortcutImplies;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitOperatorShortcutNot;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitOperatorShortcutOr;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitOperatorUnaryIntegerToInteger;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitVariable;
import epmc.expression.standard.simplify.ExpressionSimplifier;
import epmc.expression.standard.simplify.ExpressionSimplifierAnd;
import epmc.expression.standard.simplify.ExpressionSimplifierConstant;
import epmc.expression.standard.simplify.ExpressionSimplifierImplies;
import epmc.expression.standard.simplify.ExpressionSimplifierOr;
import epmc.expression.standard.simplify.ExpressionSimplifierSubtract;
import epmc.options.OptionTypeBoolean;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public final class AfterOptionsCreationExpressionStandard implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-options-creation-exrpession-standard";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        assert options != null;

        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsExpressionBasic.OPTIONS_EXPRESSION_BASIC)
        .setIdentifier(OptionsExpressionBasic.DD_EXPRESSION_VECTOR)
        .setType(typeBoolean)
        .setDefault(true)
        .setCommandLine().setGui().build();

        options.addOption().setBundleName(OptionsExpressionBasic.OPTIONS_EXPRESSION_BASIC)
        .setIdentifier(OptionsExpressionBasic.DD_EXPRESSION_CACHE)
        .setType(typeBoolean)
        .setDefault(true)
        .setCommandLine().setGui().build();

        Map<String,Class<? extends EvaluatorExplicit.Builder>> evaluatorsExplicit = options.get(OptionsExpressionBasic.EXPRESSION_EVALUTOR_EXPLICIT_CLASS);
        if (evaluatorsExplicit == null) {
            evaluatorsExplicit = new OrderedMap<>(true);            
        }
        evaluatorsExplicit.put(EvaluatorExplicitVariable.IDENTIFIER, EvaluatorExplicitVariable.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitOperator.IDENTIFIER, EvaluatorExplicitOperator.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitOperatorShortcutNot.IDENTIFIER, EvaluatorExplicitOperatorShortcutNot.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitOperatorShortcutAnd.IDENTIFIER, EvaluatorExplicitOperatorShortcutAnd.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitOperatorShortcutOr.IDENTIFIER, EvaluatorExplicitOperatorShortcutOr.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitOperatorShortcutImplies.IDENTIFIER, EvaluatorExplicitOperatorShortcutImplies.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitOperatorShortcutIfThenElse.IDENTIFIER, EvaluatorExplicitOperatorShortcutIfThenElse.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitLiteralInteger.IDENTIFIER, EvaluatorExplicitLiteralInteger.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitLiteralBoolean.IDENTIFIER, EvaluatorExplicitLiteralBoolean.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitLiteralReal.IDENTIFIER, EvaluatorExplicitLiteralReal.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitIntegerVariable.IDENTIFIER, EvaluatorExplicitIntegerVariable.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitOperatorBinaryIntegerToInteger.IDENTIFIER, EvaluatorExplicitOperatorBinaryIntegerToInteger.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitOperatorBinaryIntegerToBoolean.IDENTIFIER, EvaluatorExplicitOperatorBinaryIntegerToBoolean.Builder.class);
        evaluatorsExplicit.put(EvaluatorExplicitOperatorUnaryIntegerToInteger.IDENTIFIER, EvaluatorExplicitOperatorUnaryIntegerToInteger.Builder.class);
        options.set(OptionsExpressionBasic.EXPRESSION_EVALUTOR_EXPLICIT_CLASS, evaluatorsExplicit);

        Map<String,Class<? extends EvaluatorDD>> evaluatorsDD = new OrderedMap<>(true);
        evaluatorsDD.put(EvaluatorDDVariable.IDENTIFIER, EvaluatorDDVariable.class);
        evaluatorsDD.put(EvaluatorDDOperatorGeneral.IDENTIFIER, EvaluatorDDOperatorGeneral.class);
        evaluatorsDD.put(EvaluatorDDLiteral.IDENTIFIER, EvaluatorDDLiteral.class);
        evaluatorsDD.put(EvaluatorDDOperatorVectorAdd.IDENTIFIER, EvaluatorDDOperatorVectorAdd.class);
        evaluatorsDD.put(EvaluatorDDOperatorVectorAddInverse.IDENTIFIER, EvaluatorDDOperatorVectorAddInverse.class);
        evaluatorsDD.put(EvaluatorDDOperatorVectorEq.IDENTIFIER, EvaluatorDDOperatorVectorEq.class);
        evaluatorsDD.put(EvaluatorDDOperatorVectorGe.IDENTIFIER, EvaluatorDDOperatorVectorGe.class);
        evaluatorsDD.put(EvaluatorDDOperatorVectorGt.IDENTIFIER, EvaluatorDDOperatorVectorGt.class);
        evaluatorsDD.put(EvaluatorDDOperatorVectorLe.IDENTIFIER, EvaluatorDDOperatorVectorLe.class);
        evaluatorsDD.put(EvaluatorDDOperatorVectorLt.IDENTIFIER, EvaluatorDDOperatorVectorLt.class);
        evaluatorsDD.put(EvaluatorDDOperatorVectorMax.IDENTIFIER, EvaluatorDDOperatorVectorMax.class);
        evaluatorsDD.put(EvaluatorDDOperatorVectorMin.IDENTIFIER, EvaluatorDDOperatorVectorMin.class);
        evaluatorsDD.put(EvaluatorDDOperatorVectorNe.IDENTIFIER, EvaluatorDDOperatorVectorNe.class);
        evaluatorsDD.put(EvaluatorDDOperatorVectorSub.IDENTIFIER, EvaluatorDDOperatorVectorSub.class);
        options.set(OptionsExpressionBasic.EXPRESSION_EVALUTOR_DD_CLASS, evaluatorsDD);

        Map<String,Class<? extends ExpressionSimplifier.Builder>> simplifiers = new OrderedMap<>(true);
        simplifiers.put(ExpressionSimplifierAnd.IDENTIFIER, ExpressionSimplifierAnd.Builder.class);
        simplifiers.put(ExpressionSimplifierConstant.IDENTIFIER, ExpressionSimplifierConstant.Builder.class);
        simplifiers.put(ExpressionSimplifierOr.IDENTIFIER, ExpressionSimplifierOr.Builder.class);
        simplifiers.put(ExpressionSimplifierImplies.IDENTIFIER, ExpressionSimplifierImplies.Builder.class);
        simplifiers.put(ExpressionSimplifierSubtract.IDENTIFIER, ExpressionSimplifierSubtract.Builder.class);
        options.set(OptionsExpressionBasic.EXPRESSION_SIMPLIFIER_CLASS, simplifiers);
    }

}
