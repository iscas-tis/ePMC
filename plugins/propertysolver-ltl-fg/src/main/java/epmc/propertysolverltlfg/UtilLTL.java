package epmc.propertysolverltlfg;

import static epmc.expression.standard.ExpressionPropositional.isPropositional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionTemporal;
import epmc.expression.standard.TemporalType;
import epmc.expression.standard.TimeBound;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.value.ContextValue;
import epmc.value.OperatorAnd;
import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorLe;
import epmc.value.OperatorLt;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;

public final class UtilLTL {
	
	public static boolean isFGLTL(Expression expression) {
		if (isPropositional(expression)) {
			return true;
		}
		if (expression instanceof ExpressionTemporal) {
			ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
			TemporalType type = expressionTemporal.getTemporalType();
			if (type == TemporalType.RELEASE
					|| type == TemporalType.NEXT
					|| type == TemporalType.UNTIL) 
				return false;
			for (Expression inner : expressionTemporal.getOperands()) {
				if (!isFGLTL(inner)) {
					return false;
				}
			}
		} else if (expression instanceof ExpressionOperator) {
			ExpressionOperator expressionOperator = (ExpressionOperator) expression;
			for (Expression inner : expressionOperator.getOperands()) {
				if (!isFGLTL(inner)) {
					return false;
				}
			}
		} else {
			assert false;
		}
		return true;
	}
		
	
	public static Set<Expression> collectLTLInner(Expression expression) {
		if (isPropositional(expression)) {
			if (isOperator(OperatorNot.IDENTIFIER, expression)) {
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
	
	/*
	 * Flatten operator defined in the paper
	 */
	public static Set<Set<Expression>> flatten(ContextValue contextValue, Expression prop,
			Set<Expression> labels) {
		if (prop instanceof ExpressionIdentifier
				|| prop instanceof ExpressionLiteral) {
			// this should not happen
			Set<Expression> inSet = Collections.singleton(prop);
			return Collections.singleton(inSet);
		}

		if (labels.contains(prop)) {
			Set<Expression> inSet = Collections.singleton(prop);
			return Collections.singleton(inSet);
		}

		if (prop instanceof ExpressionOperator) { // AND, OR will be flattened
			ExpressionOperator propOperator = (ExpressionOperator) prop;
			List<? extends Expression> ops = propOperator.getOperands();
			Set<Set<Expression>> set = null;
			ExpressionOperator expressionOperator = (ExpressionOperator) prop;
			switch (expressionOperator.getOperator().getIdentifier()) {
			case OperatorNot.IDENTIFIER: case OperatorLt.IDENTIFIER: case OperatorGt.IDENTIFIER: case OperatorGe.IDENTIFIER: case OperatorLe.IDENTIFIER:
			case OperatorEq.IDENTIFIER: case OperatorNe.IDENTIFIER: // atomic propositions
				set = new HashSet<>();
				Set<Expression> inSet = new HashSet<>();
				inSet.add(prop);
				set.add(inSet);
				return set;
			case OperatorOr.IDENTIFIER:
				Set<Set<Expression>> op1 = flatten(contextValue, ops.get(0), labels);
				op1.addAll(flatten(contextValue, ops.get(1), labels)); 
				return op1;
			case OperatorAnd.IDENTIFIER:
				set = new HashSet<>();
				Set<Set<Expression>> op11 = flatten(contextValue, ops.get(0), labels);
				Set<Set<Expression>> op12 = flatten(contextValue, ops.get(1), labels);
				// CNF => DNF
				for (Set<Expression> p1 : op11) {
					for (Set<Expression> p2 : op12) {
						Set<Expression> tmp = new HashSet<>(p1);
						tmp.addAll(p2); 
						set.add(tmp);
					}
				} 
				return set;
			default:
				assert (false);
			}

		}
		assert prop instanceof ExpressionTemporal;
		ExpressionTemporal expr = (ExpressionTemporal) prop;
		Set<Set<Expression>> set = new HashSet<>();
		switch (expr.getTemporalType()) {
		case FINALLY:
			Set<Set<Expression>> op1 = flatten(contextValue, expr.getOperand1(), labels);
			if (expr.getOperand1() instanceof ExpressionTemporal) {
				return op1;
			}
			for (Set<Expression> inset : op1) {
				Set<Expression> tmp = new HashSet<>();
				Expression conjuncts = null;
				for (Expression p : inset) {
					if (p instanceof ExpressionTemporal) { // F l, G l
						tmp.add(p);
					} else {
						conjuncts = conjuncts == null ? p : UtilExpressionStandard.opAnd(conjuncts, p); // l
					}
				}
				if(conjuncts != null) tmp.add(newFinally(conjuncts));
				set.add(tmp);
			}
			return set;
		case GLOBALLY: // G a = 0 R a
			Set<Set<Expression>> opset = flatten(contextValue, expr.getOperand1(), labels);
			if (expr.getOperand1() instanceof ExpressionTemporal) {
				return opset;
			}
			Set<Set<Expression>> tmp1 = UtilLTL.permute(opset); // to DNF form
			for (Set<Expression> inset : tmp1) {
				Set<Expression> tmp = new HashSet<>();
				Expression disjuncts = null;
				for (Expression p : inset) {
					if (p instanceof ExpressionTemporal) { // G l, F l
						tmp.add(p);
					} else {
						disjuncts = disjuncts == null ? p : or(contextValue, disjuncts, p);
					}
				}
				if (disjuncts != null)
					tmp.add(newGlobally(disjuncts));
				set.add(tmp);
			}
			return UtilLTL.permute(set);
		default:
			break;
		}
		assert (false);
		return set;
	}

	/*
	 * recursive to get product
	 */
	public static Set<Set<Expression>> permute(int index,
			List<Set<Expression>> listOfSets) {
		Set<Set<Expression>> result = new HashSet<>();
		if (index == listOfSets.size()) {
			result.add(new HashSet<Expression>());
		} else {
			for (Expression list : listOfSets.get(index)) {
				for (Set<Expression> set : permute(index + 1, listOfSets)) {
					Set<Expression> tmp = new HashSet<>(set);
					set.add(list);
					result.add(set);
					result.add(tmp);
				}
			}
		}
		return result;
	}

    private static Expression or(ContextValue contextValue, Expression a, Expression b) {
        return new ExpressionOperator.Builder()
            .setOperator(contextValue.getOperator(OperatorOr.IDENTIFIER))
            .setOperands(a, b)
            .build();
    }

    private static boolean isOperator(String operatorName, Expression expression) {
        return expression instanceof ExpressionOperator
        		&& ((ExpressionOperator) expression).getOperator()
        		.getIdentifier().equals(operatorName);
    }

    private static ExpressionTemporal newGlobally(Expression operand) {
        return new ExpressionTemporal
                (operand, TemporalType.GLOBALLY, new TimeBound.Builder()
                        .build(), null);
    }

    private static ExpressionTemporal newFinally(Expression inner) {
        return new ExpressionTemporal
                (inner, TemporalType.FINALLY,
                        new TimeBound.Builder()
                        .build(), null);
    }

	private UtilLTL() {
	}
}
