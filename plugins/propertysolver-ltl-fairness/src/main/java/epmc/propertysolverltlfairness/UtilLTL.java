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
import epmc.expression.standard.ExpressionTemporal;
import epmc.expression.standard.TemporalType;
import epmc.expression.standard.TimeBound;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorAnd;
import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorIff;
import epmc.value.OperatorImplies;
import epmc.value.OperatorLe;
import epmc.value.OperatorLt;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.ValueBoolean;

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
		} else if (expression instanceof ExpressionTemporal) {
			ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
			Set<Expression> result = new LinkedHashSet<>();
			for (Expression inner : expressionTemporal.getOperands()) {
				result.addAll(collectLTLInner(inner));
			}
			return result;
		} else if (expression instanceof ExpressionOperator) {
			ExpressionOperator expressionOperator = (ExpressionOperator) expression;
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
                .getIdentifier()
                .equals(OperatorNot.IDENTIFIER);
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
		if (prop instanceof ExpressionTemporal) {
			ExpressionTemporal propTemporal = (ExpressionTemporal)prop;
			switch (propTemporal.getTemporalType()) {
			case RELEASE: // do not allow R
				return false;
			case UNTIL: // not UXLTL, should not be valid
				return false;
			case FINALLY:
				return isFairLTL(propTemporal.getOperand1(), isStable, true);
			case GLOBALLY:
				return isFairLTL(propTemporal.getOperand1(), true, isAbsolute);
			default: // default is X, X p
				return false;
			}
		} else if (prop instanceof ExpressionOperator) {
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
		if(expression instanceof ExpressionPropositional) return true;
		
		if(expression instanceof ExpressionTemporal) {
			ExpressionTemporal expressionTemporal = (ExpressionTemporal)expression;
			switch(expressionTemporal.getTemporalType()) {
			case UNTIL:
			case NEXT:
				return isValidLTL(expressionTemporal.getOperand1(), true)
						&& isValidLTL(expressionTemporal.getOperand2(), true);
			case FINALLY:
			case GLOBALLY:
				if(flag) return false;
				return isValidLTL(expressionTemporal.getOperand1(), false);
			case RELEASE:
				return false;				
			}

		}else {
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
		
		if(expression instanceof ExpressionPropositional) return true;
		
		if(expression instanceof ExpressionTemporal) {
			ExpressionTemporal expressionTemporal = (ExpressionTemporal)expression;
			switch(expressionTemporal.getTemporalType()) {
			case NEXT:
				return isUXLTL(expressionTemporal.getOperand1());
			case UNTIL:
				return isUXLTL(expressionTemporal.getOperand1())
						&& isUXLTL(expressionTemporal.getOperand2());
			case RELEASE:
			case FINALLY:
			case GLOBALLY:
				return false;
			}
		}else {
			for(Expression op : expression.getChildren()) {
				if(! isUXLTL(op)) return false;
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
        return ValueBoolean.isFalse(expressionLiteral.getValue());
    }
    
    public static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return ValueBoolean.isTrue(expressionLiteral.getValue());
    }
    
    public static Expression newOperator(ContextValue contextValue, String operatorId, Expression... operands) {
        Operator operator = contextValue.getOperator(operatorId);
        return new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(Arrays.asList(operands))
                .build();
    }
    
    public static ExpressionTemporal newTemporal
    (TemporalType type, Expression operand, Positional positional) {
        assert type != null;
        return new ExpressionTemporal
                (operand, type, positional);
    }
    
    public static ExpressionTemporal newFinally(Expression inner, Positional positional) {
        return new ExpressionTemporal
                (inner, TemporalType.FINALLY,
                        new TimeBound.Builder()
                                .build(), positional);
    }
    
    public static ExpressionTemporal newGlobally(Expression operand, Positional positional) {
        return new ExpressionTemporal
                (operand, TemporalType.GLOBALLY, new TimeBound.Builder()
                        .build(), positional);
    }
    
    public static ExpressionTemporal newGlobally(Expression operand) {
        return new ExpressionTemporal
                (operand, TemporalType.GLOBALLY, new TimeBound.Builder()
                        .build(), null);
    }
    
    public static ExpressionTemporal newFinally(Expression inner) {
        return new ExpressionTemporal
                (inner, TemporalType.FINALLY,
                        new TimeBound.Builder()
                        .build(), null);
    }
    
	/*
	 * Transform a LTL formula to positive normal form 
	 * !(G a) = F !a !(F a) = G !a
	 */
	public static Expression getNormForm(ContextValue contextValue, Expression prop,
			Set<Expression> stateLabels) {
		return getNormForm(contextValue, prop, stateLabels, false);
	}


	// only allowed AND,OR
	private static Expression getNormForm(ContextValue contextValue, Expression prop,
			Set<Expression> stateLabels, boolean sig) {
		if (prop instanceof ExpressionIdentifier || prop instanceof ExpressionLiteral) {
			return prop; // this could not happen
		}

		if (stateLabels.contains(prop)) {
			if (!sig) {
				return prop;
			}
			// NOT has been pushed down here
			return newOperator(contextValue, OperatorNot.IDENTIFIER, prop);
		}

		if (prop instanceof ExpressionTemporal) { //
			ExpressionTemporal ltlExpr = (ExpressionTemporal) prop;
			TemporalType type = ltlExpr.getTemporalType();

			if ((type == TemporalType.RELEASE && sig) // F a = 1 U a
					|| (type == TemporalType.UNTIL && !sig)) {
				Expression op2 = getNormForm(contextValue, ltlExpr.getOperand2(),
						stateLabels, sig);
				return newFinally(op2, prop.getPositional()); // F !b
			} else if ((type == TemporalType.RELEASE && !sig) // F a = 1 U a
					|| (type == TemporalType.UNTIL && sig)) { // G b = 0 R b
				Expression op2 = getNormForm(contextValue, ltlExpr.getOperand2(),
						stateLabels, sig);
				return newTemporal(TemporalType.GLOBALLY, op2,
						prop.getPositional());
			} else if ((type == TemporalType.FINALLY && !sig) // F a = 1 U a
					|| (type == TemporalType.GLOBALLY && sig)) {
				Expression op1 = getNormForm(contextValue, ltlExpr.getOperand1(),
						stateLabels, sig);
				return newFinally(op1, prop.getPositional()); // F !b
			} else {
				Expression op1 = getNormForm(contextValue, ltlExpr.getOperand1(),
						stateLabels, sig);
				return newGlobally(op1, prop.getPositional()); // F !b
			}
		} else if (prop instanceof ExpressionOperator) { /* only AND , NOT and OR allowed */
			ExpressionOperator expressionOperator = (ExpressionOperator) prop;
			List<? extends Expression> ops = expressionOperator.getOperands();
			List<Expression> exprList = new ArrayList<>();
			
			switch (expressionOperator.getOperator().getIdentifier()) {
			case OperatorAnd.IDENTIFIER: // sig
				exprList.clear();
				for (int i = 0; i < ops.size(); i++) {
					exprList.add(getNormForm(contextValue, ops.get(i), stateLabels, sig));
				}
				if (sig) {
				    return new ExpressionOperator.Builder()
				            .setOperator(contextValue.getOperator(OperatorOr.IDENTIFIER))
				            .setOperands(exprList)
				            .build();
				} else {
                    return new ExpressionOperator.Builder()
                            .setOperator(contextValue.getOperator(OperatorAnd.IDENTIFIER))
                            .setOperands(exprList)
                            .build();
				}
			case OperatorNot.IDENTIFIER:
				// Assert.notNull(ops[0]);
				if (sig) {
					return getNormForm(contextValue, ops.get(0), stateLabels, false);
				} else {
					return getNormForm(contextValue, ops.get(0), stateLabels, true);
				}
			case OperatorOr.IDENTIFIER:
				exprList.clear();
				for (int i = 0; i < ops.size(); i++) {
					exprList.add(getNormForm(contextValue, ops.get(i), stateLabels, sig));
				}
				if (sig) {
				    return new ExpressionOperator.Builder()
				            .setOperator(contextValue.getOperator(OperatorAnd.IDENTIFIER))
				            .setOperands(exprList)
				            .build();
				} else {
                    return new ExpressionOperator.Builder()
                            .setOperator(contextValue.getOperator(OperatorOr.IDENTIFIER))
                            .setOperands(exprList)
                            .build();
				}
			default:
				assert false;
			}
			ExpressionOperator propOp = (ExpressionOperator) prop;
			switch (propOp.getOperator().getIdentifier()) {
			case OperatorGt.IDENTIFIER: // >
				if (!sig)
					return prop;
				return newOperator(contextValue, OperatorLe.IDENTIFIER, ops.get(0),
						ops.get(1));

			case OperatorGe.IDENTIFIER:
				if (!sig)
					return prop;
				return newOperator(contextValue, OperatorLt.IDENTIFIER, ops.get(0),
						ops.get(1));

			case OperatorLt.IDENTIFIER:
				if (!sig)
					return prop;
				return newOperator(contextValue, OperatorGe.IDENTIFIER, ops.get(0),
						ops.get(1));

			case OperatorLe.IDENTIFIER:
				if (!sig)
					return prop;
				return newOperator(contextValue, OperatorGt.IDENTIFIER, ops.get(0),
						ops.get(1));
			case OperatorAnd.IDENTIFIER: // sig
				exprList.clear();
				for (int i = 0; i < ops.size(); i++) {
					exprList.add(getNormForm(contextValue, ops.get(i), stateLabels, sig));
				}
				if (sig) {
				    return new ExpressionOperator.Builder()
				            .setOperator(contextValue.getOperator(OperatorOr.IDENTIFIER))
				            .setOperands(exprList)
				            .build();
				} else {
                    return new ExpressionOperator.Builder()
                            .setOperator(contextValue.getOperator(OperatorAnd.IDENTIFIER))
                            .setOperands(exprList)
                            .build();
				}
			case OperatorNot.IDENTIFIER:
				// Assert.notNull(ops[0]);
				if (sig) {
					return getNormForm(contextValue, ops.get(0), stateLabels, false);
				} else {
					return getNormForm(contextValue, ops.get(0), stateLabels, true);
				}
			case OperatorEq.IDENTIFIER: //
				if (!sig)
					return prop;
				return newOperator(contextValue, OperatorNe.IDENTIFIER, ops.get(0),
						ops.get(1));
			case OperatorNe.IDENTIFIER: //
				if (!sig)
					return prop;
				return newOperator(contextValue, OperatorEq.IDENTIFIER, ops.get(0),
						ops.get(1));
			case OperatorIff.IDENTIFIER: // a <-> b = a->b & b -> a
				Expression front = newOperator(contextValue, OperatorImplies.IDENTIFIER,
						ops.get(0), ops.get(1));
				Expression back = newOperator(contextValue, OperatorImplies.IDENTIFIER,
						ops.get(1), ops.get(0));
				if (sig) { // !(a->b) | !(b->a)
					return newOperator(contextValue, OperatorOr.IDENTIFIER,
							getNormForm(contextValue, front, stateLabels, true),
							getNormForm(contextValue, back, stateLabels, true));
				} else { // a->b and b->a
					return newOperator(contextValue, OperatorAnd.IDENTIFIER,
							getNormForm(contextValue, front, stateLabels, false),
							getNormForm(contextValue, back, stateLabels, false));
				}
			case OperatorImplies.IDENTIFIER: // a -> b = !a | b
				if (sig) { // a & !b
					return newOperator(contextValue, OperatorAnd.IDENTIFIER,
							getNormForm(contextValue, ops.get(0), stateLabels, false),
							getNormForm(contextValue, ops.get(1), stateLabels, true));
				} else { // !a | b
					return newOperator(contextValue, OperatorOr.IDENTIFIER,
							getNormForm(contextValue, ops.get(0), stateLabels, true),
							getNormForm(contextValue, ops.get(1), stateLabels, false));
				}
			default:
				return prop;
			}

		}

		return prop;
	}
	
	private UtilLTL() {
	}
}
