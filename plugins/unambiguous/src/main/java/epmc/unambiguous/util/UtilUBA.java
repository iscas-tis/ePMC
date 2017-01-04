package epmc.unambiguous.util;

import static epmc.value.OperatorNames.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.expression.TimeBound;
import epmc.modelchecker.UtilModelChecker;
import epmc.options.Options;
import epmc.unambiguous.options.OptionsLTLUBA;
import epmc.value.OperatorNames;

public class UtilUBA {
	/**
	 * we suppose all atomic propositions will be in expressionSeen 
	 * and compute elementary set of an LTL formula as follows:  
	 * el(p) = empty if p in AP
	 * el(! f) = el(f)
	 * el(f1 & f2) = el(f1) U el(f2)
	 * el(Xf) = {Xf} U el(f)
	 * el(f1 U f2) = {X(f1 U f2)} U el(f1) U el(f2)
	 * @param expression this is the given formula, already in the specific form
	 * @param expressionSeen atomic proposition set AP
	 * */
	public static Set<Expression> getElementarySet(Expression expression, Set<Expression> expressionSeen) {
		Set<Expression> elemSet = null;
		if (expression.isPropositional()) {
			return new HashSet<Expression>();
		}
		if (expression.isTemporal()) {
			elemSet = getElementarySet(expression.getOperand1(), expressionSeen);
			switch (expression.getTemporalType()) {
			case NEXT:
                elemSet.add(expression);
				break;
			case UNTIL:
				elemSet.addAll(getElementarySet(expression.getOperand2(), expressionSeen));
                elemSet.add(expression.getContext().newNext(expression));
				break;
			default:
				assert false : "unvalid temporal modality";
				break;
			}
		}else if(expression.isOperator()) {
			elemSet = getElementarySet(expression.getOperand1(), expressionSeen);
			switch(expression.getOperatorIdentifier()) {
			case     NOT:
				break;
			case     AND :
				elemSet.addAll(getElementarySet(expression.getOperand2(), expressionSeen));
				break;
			default:
				assert false : "unvalid operator";
			    break;
			}
		}
		assert elemSet != null;
		return elemSet;
	}
	
	/**
	 * we transform the formula to a elementary form which only contain NOT, NEXT, UNTIL and AND modalities, 
	 * transform procedure as follows: <BR/>
	 * f1 | f2 =>  ! (! f1 & ! f2) <BR/>
	 * f1 R f2 =>  ! (! f1 \U !f2) <BR/>
	 * F f => true U f             <BR/>
	 * G f => ! ( true U ! f )     <BR/>
	 * */
	
	public static Expression elementaryForm(Expression expression) {
		ContextExpression context = expression.getContext();
		Expression op1, op2;
		if (expression.isPropositional()) {
			return expression;
		}
		
		if (expression.isTemporal()) {
			op1 = elementaryForm(expression.getOperand1());
			switch (expression.getTemporalType()) {
			case NEXT:
                return context.newNext(op1);
			case UNTIL:
				op2 = elementaryForm(expression.getOperand2());
				return context.newUntil(op1, op2);
			case FINALLY:
				return context.newUntil(context.getTrue(), op1);
			case GLOBALLY:
				if(op1.isNot()) op1 = op1.getOperand1();
				else op1 = op1.not();
				op2 = context.newUntil(context.getTrue(), op1);
				return op2.not();
			case RELEASE:
				op2 = elementaryForm(expression.getOperand2());
				if(op1.isNot()) op1 = op1.getOperand1();
				else op1 = op1.not();
				if(op2.isNot()) op2 = op2.getOperand1();
				else op2 = op2.not();
				
				return context.newOperator(OperatorNames.NOT, context.newUntil(op1, op2));
			default:
				System.err.println("unvalid  temporal modality: " + expression.getTemporalType());
				System.exit(-1);
				break;
			}
		}else if(expression.isOperator()) {
			op1 = elementaryForm(expression.getOperand1());
			switch(expression.getOperatorIdentifier()) {
			case     NOT:
				if(op1.isNot()) return op1.getOperand1();
				return op1.not();
			case     AND :
				op2 = elementaryForm(expression.getOperand2());
				return context.newOperator(AND, op1, op2);
			case      OR:
				op2 = elementaryForm(expression.getOperand2());
				if(op1.isNot()) op1 = op1.getOperand1();
				else op1 = op1.not();
				if(op2.isNot()) op2 = op2.getOperand1();
				else op2 = op2.not();
				return context.newOperator(AND, op1, op2).not();
			case      IMPLIES:
				op2 = elementaryForm(expression.getOperand2());
				if(op2.isNot()) op2 = op2.getOperand1();
				else op2 = op2.not();
				return context.newOperator(AND, op1, op2).not();
			default:
				System.err.println("unvalid operator: " + expression.getTemporalType());
				System.exit(-1);
				assert false : "unvalid operator";
			    break;
			}
		}
		/* will not happen */
		return expression;
	}
    public static Expression replaceNeOperator(Expression expression)
            throws EPMCException {
        assert expression != null;
        List<Expression> newChildren = new ArrayList<>();
        for (Expression child : expression.getChildren()) {
            newChildren.add(replaceNeOperator(child));
        }
        if (!expression.isNe()) {
            return expression.replaceChildren(newChildren);
        } else {
            ContextExpression context = expression.getContext();
            return context.newOperator(OperatorNames.EQ, newChildren).not();
        }
    }
    
	public static Expression removeNot(Expression expression) {
		ContextExpression context = expression.getContext();
		Expression op1, op2;
		if (expression.isPropositional()) {
			return expression;
		}
		
		if (expression.isTemporal()) {
			op1 = removeNot(expression.getOperand1());
			switch (expression.getTemporalType()) {
			case NEXT:
                return context.newNext(op1);
			case UNTIL:
				op2 = removeNot(expression.getOperand2());
				return context.newUntil(op1, op2);
			default:
				System.err.println("unvalid  temporal modality: " + expression.getTemporalType());
				System.exit(-1);
				break;
			}
		}else if(expression.isOperator()) {
			op1 = removeNot(expression.getOperand1());
			switch(expression.getOperatorIdentifier()) {
			case     NOT:
				if(op1.isOperator() && op1.getOperatorIdentifier() == NOT)
					return op1.getOperand1();
				return op1.not();
			case     AND :
				op2 = removeNot(expression.getOperand2());
				return context.newOperator(AND, op1, op2);
			case      OR:
				op2 = removeNot(expression.getOperand2());
				return context.newOperator(AND, op1.not(), op2.not()).not();
			case      IMPLIES:
				op2 = removeNot(expression.getOperand2());
				return context.newOperator(AND, op1, op2.not()).not();
			default:
				System.err.println("unvalid operator: " + expression.getTemporalType());
				System.exit(-1);
				assert false : "unvalid operator";
			    break;
			}
		}
		/* will not happen */
		return expression;
	}
	
	/** syntax satisfaction relation according to the paper 
	 * (V, a) ||- p if p \in a in the case p \in \AP              <BR/>
	 * (V, a) ||- ! f if (V, a) ||-/ f                            <BR/>
	 * (V, a) ||- f1 & f2 if (V, a) ||- f1 and (V, a) ||- f2      <BR/>
	 * (V, a) ||- X f if X f in V ; and                           <BR/>
	 * (V, a) ||- f1 U f2 if (V, a) ||- f2 or (V, a) ||- f1 and (V, a) ||- X (f1 U f2) 
	 * 
	 * @param V the set of formulas
	 * @param a the set of input atomic propositions
	 * @param formula input formula
	 * @param apSet the set of atomic propositions
	 * */
	public static boolean isSat(Set<Expression> V, Set<Expression> a
			, Expression formula, Set<Expression> apSet) {
		ContextExpression context = formula.getContext();
		/* a only contains atomic propositions in AP,
		 * formula may be in form p or !p and p is in AP */
		if(formula.isPropositional()) { /* p is atomic proposition */
			if(formula.isTrue()) return true;
			// pure atomic propositions
			if(apSet.contains(formula) && a.contains(formula)) return true;
			// !p is not in AP, so a does not contain p
			if(!apSet.contains(formula) && ! a.contains(formula.getOperand1())) return true;
			return false;
		}
		
		if(formula.isTemporal()) {
			switch (formula.getTemporalType()) {
			case NEXT:
                if(V.contains(formula)) 
                	return true;
                return false;
			case UNTIL:
				if(isSat(V, a, formula.getOperand2(), apSet))
					return true;
				if(isSat(V, a, formula.getOperand1(), apSet) && V.contains(context.newNext(formula)))
						return true;
				return false;
		    default:
					assert false : "unvalid temporal modality";
			}
		}
		if(formula.isOperator()) {
			switch(formula.getOperatorIdentifier()) {
			case     NOT:
				return isSat(V, a, formula.getOperand1(), apSet) == false;
			case     AND :
				return isSat(V, a, formula.getOperand1(), apSet) &&
						isSat(V, a, formula.getOperand2(), apSet);
			default:
				assert false : "unvalid operator";
			    break;
			}
		}
		assert false : "can not reach here";
		return false;
	}
	/**
	 * Get all subformula which has form \phi_1 \U \phi_2
	 * suppose we have normalized form 
	 */
	public static void getUntilFormulas(Expression expression, Set<Expression> untilSet) {
		if(expression.isPropositional()) {
			return ;
		}
		
		if(expression.isTemporal()) {
			getUntilFormulas(expression.getOperand1(), untilSet);
			switch (expression.getTemporalType()) {
			case NEXT:
                 break;
			case RELEASE:
				getUntilFormulas(expression.getOperand2(), untilSet);
				break;
			case UNTIL:
				getUntilFormulas(expression.getOperand2(), untilSet);
				untilSet.add(expression);
				break;
		    default:
					assert false : "unvalid temporal modality";
			}
			return ;
		}
		
		if(expression.isOperator()) {
			getUntilFormulas(expression.getOperand1(), untilSet);
			switch(expression.getOperatorIdentifier()) {
			case     NOT:
				break;
			case     OR:
			case     AND :
				getUntilFormulas(expression.getOperand2(), untilSet);
				break;
			default:
				assert false : "unvalid operator";
			    break;
			}
		}
	}
	
	/** 
	 * transform set of expressions (word) to single expression 
	 * we should notice that there may be true and false in apSet
	 * @param apSet all atomic propositions in expression, includes true and false
	 * */ 
	public static Expression letter2expr(ContextExpression context, Set<Expression> word
			, Set<Expression> apSet) {
		Expression expr = null;
		if(word.isEmpty()) { 
			/* empty word, conjunctions of all atomic propositions */
			for(Expression ap : apSet) {
				if(ap.isTrue()) return context.getFalse();
				else if(ap.isFalse()) continue;
				else ap = ap.not();
				expr = expr == null ? ap : expr.and(ap);
			}
//			assert false : "word empty";
			/** could not happen in UBA construction*/
		}else {
			Set<Expression> coWord = new HashSet<Expression>( apSet);
			for(Expression w : word) {
				coWord.remove(w);
				if(w.isTrue()) continue;
				else expr = expr == null ? w : expr.and(w);
			}
			/* word may contain just true, then expr == true*/
			for(Expression w: coWord) {
				if(w.isTrue()) {
					expr = context.getFalse();
					return expr;
				}
				else if(w.isFalse()) continue;
				else expr = expr == null ? w.not() : expr.and(w.not());
			}
		}
		/* may contan only false */
		if(expr == null) return context.getTrue();
		return expr;
	}
	/** disjunct all expression in context */
	public static Expression setToExpression(ContextExpression context, Set<Expression> word
			, Set<Expression> apSet) {
		Expression expr = null;
		if(word.isEmpty()) {
			return context.getFalse();
		}else {
			for(Expression w : word) {
				if(w.isTrue()) {
					expr = context.getTrue();
					break;
				}else {
					expr = expr == null ? expr : expr.or(w);
				}
			}
		}
		return expr;
	}
	
	public static Set<Expression> getAps(Expression expression, Set<Expression> apSet) {
		if(expression.isPropositional()) {
			apSet.add(expression);
			return apSet;
		}
		
		if(expression.isTemporal()) {
			getAps(expression.getOperand1(), apSet);
			switch (expression.getTemporalType()) {
			case NEXT:
			case FINALLY:
			case GLOBALLY:
                 break;
			case UNTIL:
			case RELEASE:
				getAps(expression.getOperand2(), apSet);
				break;
		    default:
					assert false : "unvalid temporal modality";
			}
			return apSet;
		}
		
		if(expression.isOperator()) {
			getAps(expression.getOperand1(), apSet);
			switch(expression.getOperatorIdentifier()) {
			case     NOT:
				break;
			case      OR :
			case     AND :
				getAps(expression.getOperand2(), apSet);
				break;
			case     IMPLIES :
				getAps(expression.getOperand2(), apSet);
				break;
			default:
				assert false : "unvalid operator";
			    break;
			}
		}
		return apSet;
	}
	
	public static Set<Expression> getVars(Expression expression, Set<Expression> varSet) {
		if(expression.isPropositional()) {
			if(expression.isNot()) varSet.add(expression.getOperand1());
			else varSet.add(expression);
			return varSet;
		}
		
		if(expression.isTemporal()) {
			getVars(expression.getOperand1(), varSet);
			switch (expression.getTemporalType()) {
			case NEXT:
			case FINALLY:
			case GLOBALLY:
                 break;
			case UNTIL:
			case RELEASE:
				getVars(expression.getOperand2(), varSet);
				break;
		    default:
					assert false : "unvalid temporal modality";
			}
			return varSet;
		}
		
		if(expression.isOperator()) {
			getVars(expression.getOperand1(), varSet);
			switch(expression.getOperatorIdentifier()) {
			case     NOT:
				break;
			case      OR :
			case     AND :
				getVars(expression.getOperand2(), varSet);
				break;
			case     IMPLIES :
				getVars(expression.getOperand2(), varSet);
				break;
			default:
				assert false : "unvalid operator";
			    break;
			}
		}
		return varSet;
	}
	
	public static Expression negNormForm(Expression formula, Set<Expression> apSet) {
	    ContextExpression context = formula.getContext();
	    Expression op1, op2;
		if(apSet.contains(formula)) {
			return formula;
		}
		
		if(formula.isTemporal()) {
			op1 = negNormForm(formula.getOperand1(), apSet);
			switch (formula.getTemporalType()) {
			case NEXT:
                 return context.newNext(op1);
			case UNTIL:
				op2 = negNormForm(formula.getOperand2(), apSet);
				return context.newUntil(op1, op2);
			case FINALLY:
				return context.newUntil(context.getTrue(), op1);
			case GLOBALLY:
				return context.newRelease(context.getFalse(), op1);
			case RELEASE:
				op2 = negNormForm(formula.getOperand2(), apSet);
				return context.newRelease(op1, op2);
		    default:
					assert false : "unvalid temporal modality";
			}
		}
		if(formula.isOperator()) {
			switch(formula.getOperatorIdentifier()) {
			case     AND :
			case     OR :
				op1 = negNormForm(formula.getOperand1(), apSet);
				op2 = negNormForm(formula.getOperand2(), apSet);
				return context.newOperator(formula.getOperator(), op1, op2);
			case     NOT: /* consider more*/
				return pushDownNot(formula.getOperand1(), apSet);
			default:
				assert false : "unvalid operator";
			    break;
			}
		}
		return formula;
	}
	/** assume formula is operand 1 from not operator */
	private static Expression pushDownNot(Expression formula, Set<Expression> apSet) {
		Expression op1, op2;
		ContextExpression context = formula.getContext();
		if(apSet.contains(formula)) return formula.not();
		if(formula.isOperator()) {
			switch(formula.getOperatorIdentifier()) {
			case     AND :
				op1 = negNormForm(formula.getOperand1().not(), apSet);
				op2 = negNormForm(formula.getOperand2().not(), apSet);
				return context.newOperator(OR, op1, op2);
			case     OR :
				op1 = negNormForm(formula.getOperand1().not(), apSet);
				op2 = negNormForm(formula.getOperand2().not(), apSet);
				return context.newOperator(AND, op1, op2);
			case     NOT: /* !! a = a*/
				return negNormForm(formula.getOperand1(), apSet);
			default:
				assert false : "unvalid operator";
			    break;
			}
		}
		if(formula.isTemporal()) {
			op1 = negNormForm(formula.getOperand1().not(), apSet);
			switch (formula.getTemporalType()) {
			case NEXT:
                 return context.newNext(op1);
			case UNTIL:
				op2 = negNormForm(formula.getOperand2().not(), apSet);
				return context.newRelease(op1, op2);
			case FINALLY:
				return context.newRelease(context.getFalse(), op1);
			case GLOBALLY:
				return context.newUntil(context.getTrue(), op1);
			case RELEASE:
				op2 = negNormForm(formula.getOperand2().not(), apSet);
				return context.newUntil(op1, op2);
		    default:
					assert false : "unvalid temporal modality";
			}
		}
	    return formula;
	}
	
	public static void closure(Expression expression, Set<Expression> clSet, Set<Expression> apSet) {
		
		if(expression.isTrue()) return ;
		if(expression.isFalse()) return ;
		
		clSet.add(expression);
		if(apSet.contains(expression)) { /* first atomic propostition */
			clSet.add(expression.not());
			return ;
		}else
		if(expression.isOperator() ) { /* operator */
			closure(expression.getOperand1(), clSet, apSet);
			switch(expression.getOperatorIdentifier()) {
			case     NOT: /* should be in front of atomic propositions */
				break;
			case     AND :
			case     OR :
				clSet.add(expression.not());
			    closure(expression.getOperand2(), clSet, apSet);
				break;
			default:
					assert false : "unvalid operator";
			}
		}else {
			if(expression.isTemporal()) {
				clSet.add(expression.not());
				closure(expression.getOperand1(), clSet, apSet);
				switch(expression.getTemporalType()) {
				case     NEXT:
					break;
				case     UNTIL :
				case     RELEASE :
					closure(expression.getOperand2(), clSet, apSet);
					break;
				default:
					assert false : "unvalid operator";
				}
			}
		}
		
	}
	
    public static Expression bounded2next(Expression expression)
            throws EPMCException {
    	ContextExpression context = expression.getContext();
        ArrayList<Expression> newChildren = new ArrayList<>();
        for (Expression child : expression.getChildren()) {
            newChildren.add(bounded2next(child));
        }
        expression = expression.replaceChildren(newChildren);

        TimeBound timeBound = null;
        if (expression.isUntil() || expression.isRelease()) {
            timeBound = expression.getTimeBound();
        }
        if (timeBound != null && (timeBound.isLeftBounded() || timeBound.isRightBounded())) {
            // TODO handle multi-until
            Expression expressionTemporal = expression;
            Expression leftExpr = expressionTemporal.getOperand1();
            Expression rightExpr = expressionTemporal.getOperand2();
            int boundLeft = timeBound.getLeftInt();
            int boundRight = timeBound.getRightInt();
            int bound;
            Expression result;
            if (timeBound.isRightBounded()) {
                result = rightExpr;
                bound = boundRight;
            } else {
                result = expression;
                bound = boundLeft;
            }
            bound--;
            while (bound >= 0) {
                Expression nextResult = context.newNext(result, expression.getPositional());
                if (expression.isUntil()) {
                    result = leftExpr.and(nextResult, expression.getPositional());
                } else if (expression.isRelease()) {
                    result = leftExpr.or(nextResult, expression.getPositional());
                }
                if (bound - boundLeft >= 0) {
                    if (expression.isUntil()) {
                        result = result.or(rightExpr, expression.getPositional());
                    } else if (expression.isRelease()) {
                        result = result.and(rightExpr, expression.getPositional());
                    }
                }
                bound--;
            }
            return result;
        } else {
            return expression;
        }
    }
    
//    public static void outputGraphDD(GraphDD graphDD
//    		, DD target, OutputStream stream) throws EPMCException {
//        List<DD> sinks = new ArrayList<>();
//        Semantics semantics = graphDD.getGraphPropertyObject(CommonProperties.SEMANTICS);
//        @SuppressWarnings("resource")
//		GraphBuilderDD converter = new GraphBuilderDD(graphDD, sinks, semantics.isNonDet());
//
//        GraphExplicit graph = converter.buildGraph();
//        DD nodeSpace = graphDD.getNodeSpace().clone();
//        DD someNodes = ComponentsDD.reachMaxSome(graphDD, target, nodeSpace).andNotWith(target.clone());
//        DD zeroNodes = nodeSpace.clone().andNotWith(someNodes).andNotWith(target.clone());
//        DD init = graphDD.getInitial().clone();
//        BitSet inits = converter.ddToBitSet(init);
//        BitSet targets = converter.ddToBitSet(target);
//        BitSet zero = converter.ddToBitSet(zeroNodes);
//        PrintStream out = new PrintStream(stream);
//        out.println("init: \n" + inits);
//        out.println("target: \n" + targets);
//        out.println("zero: \n" + zero);
//        GraphExporter.export(graph, stream);
//        nodeSpace.dispose();
//        zeroNodes.dispose();
//        try {
//			stream.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
    
//    public static void outputGraphDD(GraphDD graph, DD target, String name) {
//    	File graphFile = new File("/home/liyong/projects/test/dotfile/" + name);
//    	try {
//			outputGraphDD(graph, target, new FileOutputStream(graphFile));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (EPMCException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
    
    public static void openDDXDot(DD dd) {
    	String graph = dd.toString();
    	File dotFile = new File("/home/liyong/projects/test/dotfile/tmp.dot");
    	
    	FileOutputStream out;
		try {
			out = new FileOutputStream(dotFile);
			out.write(graph.getBytes());
			out.close();
			Runtime.getRuntime().exec("xdot " + dotFile.getAbsolutePath());
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static String expr2string(Expression expression,
            Map<Expression, String> expr2str, int[] numAPs) {
        String result = expr2str.get(expression);
        if (result != null) {
            return result;
        }
        if (expression.isLiteral()) {
            assert expression.isTrue() || expression.isFalse();
            result = expression.toString();
        } else if (expression.isOperator()) {
            Expression op =  expression;
            if (op.isAnd() || op.isOr() || op.isIff() || op.isImplies()
                    || op.isIte()) {
                String left = expr2string(op.getOperand1(), expr2str, numAPs);
                String right = expr2string(op.getOperand2(), expr2str, numAPs);
                if (left != null && right != null ) {
                    result = "ap" + numAPs[0];
                    numAPs[0]++;            
                }
            } else if (op.isNot()) {
                expr2string(op.getOperand1(), expr2str, numAPs);
            } else { /* may be eq */
            	if(! expr2str.containsKey(op)){
                    result = "ap" + numAPs[0];
                    numAPs[0]++;      
            	}/* not include */
            }
        } else if (expression.isIdentifier()) {
            result = "ap" + numAPs[0];
            numAPs[0]++;            
        } else if (expression.isTemporal()) {
            Expression temp = expression;
            if (temp.isUntil() || temp.isRelease()) {
                expr2string(temp.getOperand1(), expr2str, numAPs);
                expr2string(temp.getOperand2(), expr2str, numAPs);
            } else if (temp.isNext() || temp.isFinally() || temp.isGlobally()) {
                expr2string(temp.getOperand1(), expr2str, numAPs);
            } else {
                assert false : expression;
            }
        } else if (expression.isQuantifier()) {
            result = "ap" + numAPs[0];
            numAPs[0]++;
        }
        if (result != null) {
            expr2str.put(expression,  result);
        }
        return result;
    }
    
    public static String expr2spot(Expression expression,
            Map<Expression, String> expr2str) {
        String result = expr2str.get(expression);
        if (result != null) {
            return result;
        }
        if (expression.isLiteral()) {
            // must be true or false
            result = expression.toString();
        } else if (expression.isOperator()) {
            Expression op =  expression;
            if (op.isAnd()) {
                String left = expr2spot(op.getOperand1(), expr2str);
                String right = expr2spot(op.getOperand2(), expr2str);
                result = "(" + left + " & " + right + ")";
            } else if (op.isOr()) {
                String left = expr2spot(op.getOperand1(), expr2str);
                String right = expr2spot(op.getOperand2(), expr2str);
                result = "(" + left + " | " + right + ")";
            } else if (op.isNot()) {
                String left = expr2spot(op.getOperand1(), expr2str);
                result =  "(! " + left + ")";
            } else if (op.isIff()) {
                String left = expr2spot(op.getOperand1(), expr2str);
                String right = expr2spot(op.getOperand2(), expr2str);
                result = "(" + left + " <=> " + right + ")";
            } else if (op.isImplies()) {
                String left = expr2spot(op.getOperand1(), expr2str);
                String right = expr2spot(op.getOperand2(), expr2str);
                result = "(" + left + " => " + right + ")";                
            } else if (op.isIte()) {
                String ifStr = expr2spot(op.getOperand1(), expr2str);
                String thenStr = expr2spot(op.getOperand2(), expr2str);
                String elseStr = expr2spot(op.getOperand3(), expr2str);
                result = "(" + ifStr + " & " + thenStr + " | !" + ifStr +
                        " & " + elseStr + ")";
            } else {
                assert false : expression;
            }
        } else if (expression.isIdentifier()) {
            assert false;
        } else if (expression.isTemporal()) {
            Expression temp =  expression;
            if (temp.isUntil()) {
                String left = expr2spot(temp.getOperand1(), expr2str);
                String right = expr2spot(temp.getOperand2(), expr2str);
                if (temp.getOperand1().isTrue()) {
                    result = "(F " + right + ")";
                } else {
                    result = "(" + left + " U " + right + ")";
                }
            } else if (temp.isRelease()) {
                String left = expr2spot(temp.getOperand1(), expr2str);
                String right = expr2spot(temp.getOperand2(), expr2str);
                if (temp.getOperand1().isFalse()) {
                    result = "(G " + right + ")";
                } else {
                    result = "(" + left + " R " + right + ")";
                }
            } else if (temp.isNext()) {
                String left = expr2spot(temp.getOperand1(), expr2str);
                result = "(X " + left + ")";
            } else if (temp.isFinally()) {
                String inner = expr2spot(temp.getOperand1(), expr2str);
                result = "(F " + inner + ")";
            } else if (temp.isGlobally()) {
                String inner = expr2spot(temp.getOperand1(), expr2str);
                result = "(G " + inner + ")";
            } else {
                assert false;
            }
        } else if (expression.isQuantifier()) {
            assert false;
        }
        expr2str.put(expression,  result);
        return result;
    }
    
    public static Expression spotReduceExpression(Expression expression) throws EPMCException {
        assert expression != null;
        ContextExpression contextExpression = expression.getContext();
        Map<Expression,String> expr2str = contextExpression.newMap();
        int[] numAPs = new int[1];
        expr2string(expression, expr2str, numAPs);
        String spotFn = expr2spot(expression, expr2str);
        Options options = expression.getOptions();
        String ltlfilt = options.get(OptionsLTLUBA.LTL_UBA_LTLFILT_CMD);
        LinkedHashMap<Expression,Expression> ap2expr = new LinkedHashMap<>();
        for (Entry<Expression,String> entry : expr2str.entrySet()) {
            ap2expr.put(contextExpression.newIdentifier(entry.getValue()), entry.getKey());
        }
        try {
            final String[] autExecArgs = {ltlfilt, "--full-parentheses", "--simplify", "--remove-wm", "-f", spotFn};
            final Process autProcess = Runtime.getRuntime().exec(autExecArgs);
            final BufferedReader autIn = new BufferedReader
                    (new InputStreamReader(autProcess.getInputStream()));
            String rStr = "(" + autIn.readLine().trim() + ")";
            if (rStr.equals("(0)")) {
                rStr = "(false)";
            } else if (rStr.equals("(1)")) {
                rStr = "(true)";
            }
            Expression raw = null;//UtilModelChecker.parse(options, rStr);
            return raw.replace(ap2expr);
//            raw.replace(ap2expr);
//            SpotParser spotParser = new SpotParser(autIn);
//            return spotParser.parseExpression(options, ap2expr);
        } catch (IOException e) {
            return null;
        }
    }
    
    public static Expression ltlfiltSimplify(Expression expression) throws EPMCException {
        assert expression != null;
        ContextExpression contextExpression = expression.getContext();
        Map<Expression,String> expr2str = contextExpression.newMap();
        int[] numAPs = new int[1];
        expr2string(expression, expr2str, numAPs);
        String spotFn = expr2spot(expression, expr2str);
        String ltlfilt = expression.getOptions().get(OptionsLTLUBA.LTL_UBA_LTLFILT_CMD);
        try {
            final String[] autExecArgs = {ltlfilt, "-f", spotFn, "-p", "-r", "--remove-wm"};
            final Process autProcess = Runtime.getRuntime().exec(autExecArgs);
            final BufferedReader autIn = new BufferedReader
                    (new InputStreamReader(autProcess.getInputStream()));
            String line = "(" + autIn.readLine() + ")";
            Expression result = UtilModelChecker.parse(contextExpression, line);
            if (result.isLiteral() && result.getValue().isOne()) {
                result = contextExpression.getTrue();
            } else if (result.isLiteral() && result.getValue().isZero()) {
                result = contextExpression.getFalse();                
            }
            Map<Expression,Expression> inverse = contextExpression.newMap();
            for (Entry<Expression, String> entry : expr2str.entrySet()) {
                inverse.put(contextExpression.newIdentifier(entry.getValue()), entry.getKey());
            }
            result = result.replace(inverse);
            return result;
        } catch (IOException e) {
            return null;
        }
    }
    
	public static Set<Expression> collectLTLInner(Expression expression) {
		if (expression.isPropositional()) {
			if(expression.isNot()) expression = expression.getOperand1();
			return Collections.singleton(expression);
		} else if (expression.isTemporal() || expression.isOperator()) {
			Set<Expression> result = new LinkedHashSet<>();
			for (Expression inner : expression.getOperands()) {
				result.addAll(collectLTLInner(inner));
			}
			return result;
		} else {
			return Collections.singleton(expression);			
		}
	}

}
