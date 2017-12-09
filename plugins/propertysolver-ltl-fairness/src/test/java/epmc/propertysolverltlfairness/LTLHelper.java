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

package epmc.propertysolverltlfairness;

import java.util.List;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.modelchecker.Property;
import epmc.modelchecker.RawProperties;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.options.UtilOptions;

public class LTLHelper {
    public static RawProperties readProperties(Options options, String propFn) {
        if (propFn != null) {
            RawProperties props = new RawProperties();
            Property property = UtilOptions.getInstance(OptionsModelChecker.PROPERTY_INPUT_TYPE);
            TestHelper.readProperties(property, props, propFn);
            return props;
        }
        return new RawProperties();
    }
    public static void print(Set<Set<Expression>> sets) {
        System.out.println("{ ");
        for(Set<Expression> set : sets) {
            print1(set);
        }
        System.out.println("}");
    }
    public static void print1(Set<Expression> set) {
        System.out.print(" { ");
        for(Expression exp : set) {
            System.out.print(exp + " ");
        }
        System.out.println(" }");
    }
    // must have P[max,min][>= k,=?,<=] [LTL-prop form]
    public static boolean canHandle(Expression property) {
        // TODO Auto-generated method stub
        assert(!(property instanceof ExpressionQuantifier));  //if quantifiers are stripped out
        return isFairLTL(property,false,false);
    }
    /*
     * check Expression whether it is in (F,G)^2-LTL,or fairness LTL every
     * atomic proposition should be preceded by at least a F and a G modality
     * return false means not in fairness-LTL 
     * return true means it is in fainess-LTL
     */
    private static boolean isFairLTL(Expression prop, boolean isStable,
            boolean isAbsolute) {
        // TODO Auto-generated method stub
        if (ExpressionIdentifier.is(prop) || ExpressionLiteral.is(prop)) {
            return (isStable && isAbsolute);       //if it is s op value,like s = 2
        } // else must be temperal
        if (ExpressionTemporalRelease.is(prop)) {
            ExpressionTemporalRelease ltlExpr = ExpressionTemporalRelease.as(prop);
            //Assert.isTrue(ltlExpr.getOperand1().isFalse());   //not assert 
            return isFalse(ltlExpr.getOperandLeft()) &&
                    isFairLTL(ltlExpr.getOperandRight(), true, isAbsolute);
        } else if (ExpressionTemporalUntil.is(prop)) {
            ExpressionTemporalUntil ltlExpr = ExpressionTemporalUntil.as(prop);
                return isTrue(ltlExpr.getOperandLeft()) && 
                        isFairLTL(ltlExpr.getOperandRight(), isStable, true);
        } else if (ExpressionTemporalNext.is(prop)) {
            // default is X, X p 
            return false;
        } else if (ExpressionOperator.is(prop)) {//only && and | could be allowed between formulas
            ExpressionOperator propOperator = (ExpressionOperator) prop;
            List<Expression> exprArr = propOperator.getOperands();
            for(int i = 0; i < exprArr.size(); i ++) {
                if(! isFairLTL(exprArr.get(i), isStable, isAbsolute)) 
                    return false;
            }
            return true;
        }//if quantifier false
        return false;
    }

    private static boolean isFalse(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return !Boolean.valueOf(expressionLiteral.getValue());
    }

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return Boolean.valueOf(expressionLiteral.getValue());
    }

}
