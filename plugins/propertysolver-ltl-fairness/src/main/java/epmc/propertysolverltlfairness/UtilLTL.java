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


import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionPropositional;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.expression.standard.TimeBound;
import epmc.operator.Operator;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorIff;
import epmc.operator.OperatorImplies;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public final class UtilLTL {
    public static Set<Expression> collectLTLInner(Expression expression) {
        if (expression instanceof ExpressionPropositional) {
            if (isNot(expression)) {
                ExpressionOperator expressionOperator = (ExpressionOperator) expression;
                return Collections.singleton(expressionOperator.getOperand1());
            } else {
                return Collections.singleton(expression);
            }
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalGlobally.is(expression)) {
            ExpressionTemporalGlobally expressionTemporal = ExpressionTemporalGlobally.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalRelease.is(expression)) {
            ExpressionTemporalRelease expressionTemporal = ExpressionTemporalRelease.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectLTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectLTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionOperator.is(expression)) {
            ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression inner : expressionOperator.getOperands()) {
                result.addAll(collectLTLInner(inner));
            }
            return result;
        } else {
            return Collections.singleton(expression);			
        }
    }

    /*
     * DNF <=> CNF for Set<Set<Object>>
     */
    public static Set<Set<Expression>> permute(Set<Set<Expression>> sets) {
        if (sets.size() < 2)
            return sets;
        List<Set<Expression>> listOfSets = new ArrayList<>(
                sets.size());
        for (Set<Expression> set : sets) {
            listOfSets.add(set);
        }
        Set<Set<Expression>> perms = permute(0, listOfSets);

        perms.remove(new HashSet<>());

        // remove redundant/duplicate accepting conditions
        boolean exitIteration = false;
        do {
            Set<Set<Expression>> result = new HashSet<>();
            for (Set<Expression> c : perms) {//
                boolean subsumed = false;
                Set<Expression> replace = null;

                for (Set<Expression> d : result) {
                    if (c.containsAll(d)) {
                        subsumed = true;
                        break;
                    }// c contais d, do not add
                    if (d.containsAll(c)) {
                        replace = d;
                        break;
                    }// d contains c, then remove d, add c
                }// first check whether d is subsumed by some set in result
                if (!subsumed) {
                    if (replace != null) {
                        result.remove(replace);
                    }
                    result.add(c);
                }
            }// until no more changes
            exitIteration = result.size() == perms.size();
            perms = result;
        } while (!exitIteration);
        return perms;
    }

    //	public static Set<Set<Expression>> transform(Set<Set<Expression>> sets) {
    //		CartesianProduct<Expression> product = new CartesianProduct<Expression>();
    //		product.set(sets);
    //		return product.get();
    //	}

    /*
     * recursive to get product
     */
    public static Set<Set<Expression>> permute(int index,
            List<Set<Expression>> listOfSets) {
        Set<Set<Expression>> result = new HashSet<>();
        if (index == listOfSets.size()) {
            result.add(new HashSet<Expression>());
        } else {
            for (Object list : listOfSets.get(index)) {
                for (Set set : permute(index + 1, listOfSets)) {
                    Set tmp = new HashSet<>(set);
                    set.add(list);
                    result.add(set);
                    result.add(tmp);
                }
            }
        }
        return result;
    }

    private static boolean isNot(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorNot.NOT);
    }

    //	/**
    //	 * replace R modality with G and U modalities, we also replace expression
    //	 * (true U a) by (F a)
    //	 */
    //	public static Expression rewriteExpr(Expression expression) {
    //		
    //		if(expression instanceof ExpressionPropositional) return expression;
    //		
    //		Expression result = null;
    //		if (expression instanceof ExpressionTemporal) { //
    //			ExpressionTemporal expressionTemporal = (ExpressionTemporal)expression;
    //			Expression op1, op2;
    //			switch(expressionTemporal.getTemporalType()) {
    //			case RELEASE:
    //				op1 = expressionTemporal.getOperand1();
    //				op2 = expressionTemporal.getOperand2();
    //				if(expressionTemporal.isFalse()) {// 0 R a
    //					result = context.newGlobally(rewriteExpr(op2));
    //				}//else
    //				else {
    //					Expression left = rewriteExpr(op2);
    //					Expression right = UtilExpression.opAnd(left, rewriteExpr(op1)); 
    //					result = context.newOperator(OR
    //							, context.newGlobally(left)
    //							, context.newUntil(left, right));
    //				}
    //				break;
    //			case UNTIL:
    //				op1 = expression.getOperand1();
    //				op2 = expression.getOperand2();
    //				if(op1.isTrue()) {// 0 R a
    //					result = context.newFinally(rewriteExpr(op2));
    //				}//else
    //				else {
    //					Expression left = rewriteExpr(op1);
    //					Expression right = rewriteExpr(op2);
    //					result = context.newUntil(left, right);
    //				}
    //				break;
    //			case FINALLY:
    //				op1 = rewriteExpr(expression.getOperand1());
    //				result = context.newFinally(op1);
    //				break;
    //			case GLOBALLY:
    //				op1 = rewriteExpr(expression.getOperand1());
    //				result = context.newFinally(op1);
    //				break;
    //			case NEXT:
    //				op1 = rewriteExpr(expression.getOperand1());
    //				result = context.newNext(op1);
    //				break;
    //			default:
    //				break;
    //			}
    //
    //		} else if (expression.isOperator()) { /* only AND , NOT and OR allowed */
    //			List<Expression> operands = expression.getChildren();
    //			List<Expression> children = new ArrayList<>();
    //			for(Expression op : operands ) {
    //				children.add(rewriteExpr(op));
    //			}
    //			context.newOperator(expression.getOperator(), children);
    //		}
    //
    //		return result;
    //	}

    /**
     * Currently only those LTL formula in which every LTL(U, X) formula
     * is preceded by at least one F and G modalities
     */
    public static boolean isFairLTL(Expression prop, boolean isStable,
            boolean isAbsolute) {
        // TODO Auto-generated method stub
        if (isUXLTL(prop)) {
            return (isStable && isAbsolute); 
        } 
        if (ExpressionTemporalFinally.is(prop)) {
            ExpressionTemporalFinally propTemporal = ExpressionTemporalFinally.as(prop);
            return isFairLTL(propTemporal.getOperand(), isStable, true);
        } else if (ExpressionTemporalGlobally.is(prop)) {
            ExpressionTemporalGlobally propTemporal = ExpressionTemporalGlobally.as(prop);
            return isFairLTL(propTemporal.getOperand(), true, isAbsolute);
        } else if (ExpressionTemporalRelease.is(prop)) {
            return false;
        } else if (ExpressionTemporalNext.is(prop)) {
            return false;
        } else if (ExpressionTemporalUntil.is(prop)) {
            return false;
        } else if (ExpressionOperator.is(prop)) {
            // actually only AND , OR allowed

            List<? extends Expression> exprArr = prop.getChildren();
            for (int i = 0; i < exprArr.size(); i++) {
                if (!isFairLTL(exprArr.get(i), isStable, isAbsolute))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Make sure that input expression<br/>
     * 1 contains only X,F,U and G modalities <br/>
     * 2 in which F and G modality are not in the scope of U and X modalities <br/>
     * 3 is in negative normal form
     */
    public static boolean isValidLTL(Expression expression) {
        return isValidLTL(expression, false);
    }

    private static boolean isValidLTL(Expression expression, boolean flag) {
        if (expression instanceof ExpressionPropositional) {
            return true;
        }
        if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            if (flag) {
                return false;
            }
            return isValidLTL(expressionTemporal.getOperand(), false);
        } else if (ExpressionTemporalGlobally.is(expression)) {
            if (flag) {
                return false;
            }
            ExpressionTemporalGlobally expressionTemporal = ExpressionTemporalGlobally.as(expression);
            return isValidLTL(expressionTemporal.getOperand(), false); 
        } else if (ExpressionTemporalRelease.is(expression)) {
            return false;
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
            if (flag) {
                return false;
            }
            return isValidLTL(expressionTemporal.getOperandLeft(), false);
        } else if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            return isValidLTL(expressionTemporal.getOperand(), true);
        } else {
            ExpressionOperator expressionOp = (ExpressionOperator)expression;
            if(isNot(expressionOp)) return false;
            for(Expression op : expression.getChildren()) {
                if(! isValidLTL(op, flag)) return false;
            }
        }
        return true;
    }

    /**
     * check whether there exist only U and X modalities 
     */
    public static boolean isUXLTL(Expression expression) {
        if (expression instanceof ExpressionPropositional) {
            return true;
        }
        if (ExpressionTemporalFinally.is(expression)) {
            return false;
        } else if (ExpressionTemporalGlobally.is(expression)) {
            return false;
        } else if (ExpressionTemporalRelease.is(expression)) {
            return false;
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionTemporal = (ExpressionTemporalUntil)expression;
            return isUXLTL(expressionTemporal.getOperandLeft())
                    && isUXLTL(expressionTemporal.getOperandRight());
        } else if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            return isUXLTL(expressionTemporal.getOperand());
        } else {
            for(Expression op : expression.getChildren()) {
                if (!isUXLTL(op)) return false;
            }
        }

        return true;
    }

    public static boolean isFalse(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return !Boolean.valueOf(expressionLiteral.getValue());
    }

    public static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return Boolean.valueOf(expressionLiteral.getValue());
    }

    public static Expression newOperator(Operator operator, Expression... operands) {
        return new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(Arrays.asList(operands))
                .build();
    }

    public static ExpressionTemporalFinally newFinally(Expression inner, Positional positional) {
        return new ExpressionTemporalFinally.Builder()
                .setOperand(inner)
                .setTimeBound(new TimeBound.Builder()
                        .build())
                .setPositional(positional)
                .build();
    }

    public static ExpressionTemporalGlobally newGlobally(Expression operand, Positional positional) {
        return new ExpressionTemporalGlobally.Builder()
                .setOperand(operand)
                .setTimeBound(new TimeBound.Builder()
                        .build())
                .setPositional(positional)
                .build();
    }

    public static ExpressionTemporalGlobally newGlobally(Expression operand) {
        return new ExpressionTemporalGlobally.Builder()
                .setOperand(operand)
                .setTimeBound(new TimeBound.Builder()
                        .build())
                .build();
    }

    public static ExpressionTemporalFinally newFinally(Expression inner) {
        return new ExpressionTemporalFinally.Builder()
                .setOperand(inner)
                .setTimeBound( new TimeBound.Builder()
                        .build())
                .build();
    }

    /*
     * Transform a LTL formula to positive normal form 
     * !(G a) = F !a !(F a) = G !a
     */
    public static Expression getNormForm(Expression prop, Set<Expression> stateLabels) {
        return getNormForm(prop, stateLabels, false);
    }


    // only allowed AND,OR
    private static Expression getNormForm(Expression prop, Set<Expression> stateLabels,
            boolean sig) {
        if (ExpressionIdentifier.is(prop) || ExpressionLiteral.is(prop)) {
            return prop; // this could not happen
        }

        if (stateLabels.contains(prop)) {
            if (!sig) {
                return prop;
            }
            // NOT has been pushed down here
            return newOperator(OperatorNot.NOT, prop);
        }
        if (ExpressionTemporalFinally.is(prop)) {
            ExpressionTemporalFinally ltlExpr = ExpressionTemporalFinally.as(prop);
            if (!sig) {
                Expression op1 = getNormForm(ltlExpr.getOperand(), stateLabels,
                        sig);
                return newFinally(op1, prop.getPositional()); // F !b
            } else {
                Expression op1 = getNormForm(ltlExpr.getOperand(), stateLabels,
                        sig);
                return newGlobally(op1, prop.getPositional()); // F !b
            }
        } else if (ExpressionTemporalGlobally.is(prop)) {
            ExpressionTemporalGlobally ltlExpr = ExpressionTemporalGlobally.as(prop);
            if (sig) {
                Expression op1 = getNormForm(ltlExpr.getOperand(), stateLabels,
                        sig);
                return newFinally(op1, prop.getPositional()); // F !b
            } else {
                Expression op1 = getNormForm(ltlExpr.getOperand(), stateLabels,
                        sig);
                return newGlobally(op1, prop.getPositional()); // F !b
            }
        } else if (ExpressionTemporalRelease.is(prop)) {
            ExpressionTemporalRelease ltlExpr = ExpressionTemporalRelease.as(prop);
            if (sig) {
                Expression op2 = getNormForm(ltlExpr.getOperandRight(), stateLabels,
                        sig);
                return newFinally(op2, prop.getPositional()); // F !b
            } else {
                Expression op2 = getNormForm(ltlExpr.getOperandRight(), stateLabels,
                        sig);
                return new ExpressionTemporalGlobally.Builder()
                        .setOperand(op2)
                        .setPositional(prop.getPositional())
                        .build();
            }
        } else if (ExpressionTemporalUntil.is(prop)) { //
            ExpressionTemporalUntil ltlExpr = ExpressionTemporalUntil.as(prop);

            if (!sig) {
                Expression op2 = getNormForm(ltlExpr.getOperandRight(), stateLabels,
                        sig);
                return newFinally(op2, prop.getPositional()); // F !b
            } else if (sig) { // G b = 0 R b
                Expression op2 = getNormForm(ltlExpr.getOperandRight(), stateLabels,
                        sig);
                return new ExpressionTemporalGlobally.Builder()
                        .setOperand(op2)
                        .setPositional(prop.getPositional())
                        .build();
            } else {
                Expression op1 = getNormForm(ltlExpr.getOperandLeft(), stateLabels,
                        sig);
                return newGlobally(op1, prop.getPositional()); // F !b
            }
        } else if (ExpressionOperator.is(prop)) { /* only AND , NOT and OR allowed */
            ExpressionOperator expressionOperator = (ExpressionOperator) prop;
            List<? extends Expression> ops = expressionOperator.getOperands();
            List<Expression> exprList = new ArrayList<>();
            Operator operator = expressionOperator.getOperator();
            if (operator.equals(OperatorAnd.AND)) {
                exprList.clear();
                for (int i = 0; i < ops.size(); i++) {
                    exprList.add(getNormForm(ops.get(i), stateLabels, sig));
                }
                if (sig) {
                    return new ExpressionOperator.Builder()
                            .setOperator(OperatorOr.OR)
                            .setOperands(exprList)
                            .build();
                } else {
                    return new ExpressionOperator.Builder()
                            .setOperator(OperatorAnd.AND)
                            .setOperands(exprList)
                            .build();
                }
            } else if (operator.equals(OperatorNot.NOT)) {
                // Assert.notNull(ops[0]);
                if (sig) {
                    return getNormForm(ops.get(0), stateLabels, false);
                } else {
                    return getNormForm(ops.get(0), stateLabels, true);
                }
            } else if (operator.equals(OperatorOr.OR)) {
                exprList.clear();
                for (int i = 0; i < ops.size(); i++) {
                    exprList.add(getNormForm(ops.get(i), stateLabels, sig));
                }
                if (sig) {
                    return new ExpressionOperator.Builder()
                            .setOperator(OperatorAnd.AND)
                            .setOperands(exprList)
                            .build();
                } else {
                    return new ExpressionOperator.Builder()
                            .setOperator(OperatorOr.OR)
                            .setOperands(exprList)
                            .build();
                }
            } else {
                assert false;
            }
            ExpressionOperator propOp = (ExpressionOperator) prop;
            operator = propOp.getOperator();
            if (operator.equals(OperatorGt.GT)) {
                if (!sig)
                    return prop;
                return newOperator(OperatorLe.LE, ops.get(0), ops.get(1));
            } else if (operator.equals(OperatorGe.GE)) {
                if (!sig)
                    return prop;
                return newOperator(OperatorLt.LT, ops.get(0), ops.get(1));
            } else if (operator.equals(OperatorLt.LT)) {
                if (!sig)
                    return prop;
                return newOperator(OperatorGe.GE, ops.get(0), ops.get(1));
            } else if (operator.equals(OperatorLe.LE)) {
                if (!sig)
                    return prop;
                return newOperator(OperatorGt.GT, ops.get(0), ops.get(1));
            } else if (operator.equals(OperatorAnd.AND)) {
                exprList.clear();
                for (int i = 0; i < ops.size(); i++) {
                    exprList.add(getNormForm(ops.get(i), stateLabels, sig));
                }
                if (sig) {
                    return new ExpressionOperator.Builder()
                            .setOperator(OperatorOr.OR)
                            .setOperands(exprList)
                            .build();
                } else {
                    return new ExpressionOperator.Builder()
                            .setOperator(OperatorAnd.AND)
                            .setOperands(exprList)
                            .build();
                }
            } else if (operator.equals(OperatorNot.NOT)) {
                // Assert.notNull(ops[0]);
                if (sig) {
                    return getNormForm(ops.get(0), stateLabels, false);
                } else {
                    return getNormForm(ops.get(0), stateLabels, true);
                }
            } else if (operator.equals(OperatorEq.EQ)) {
                if (!sig)
                    return prop;
                return newOperator(OperatorNe.NE, ops.get(0), ops.get(1));
            } else if (operator.equals(OperatorNe.NE)) {
                if (!sig)
                    return prop;
                return newOperator(OperatorEq.EQ, ops.get(0), ops.get(1));
            } else if (operator.equals(OperatorIff.IFF)) {
                Expression front = newOperator(OperatorImplies.IMPLIES, ops.get(0),
                        ops.get(1));
                Expression back = newOperator(OperatorImplies.IMPLIES, ops.get(1),
                        ops.get(0));
                if (sig) { // !(a->b) | !(b->a)
                    return newOperator(OperatorOr.OR, getNormForm(front, stateLabels, true),
                            getNormForm(back, stateLabels, true));
                } else { // a->b and b->a
                    return newOperator(OperatorAnd.AND, getNormForm(front, stateLabels, false),
                            getNormForm(back, stateLabels, false));
                }
            } else if (operator.equals(OperatorImplies.IMPLIES)) {
                if (sig) { // a & !b
                    return newOperator(OperatorAnd.AND, getNormForm(ops.get(0), stateLabels, false),
                            getNormForm(ops.get(1), stateLabels, true));
                } else { // !a | b
                    return newOperator(OperatorOr.OR, getNormForm(ops.get(0), stateLabels, true),
                            getNormForm(ops.get(1), stateLabels, false));
                }
            } else {
                return prop;
            }

        }

        return prop;
    }

    private UtilLTL() {
    }
}
