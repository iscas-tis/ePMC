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

import static epmc.error.UtilError.ensure;

import java.util.List;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.extensions.derivedoperators.OperatorAbs;
import epmc.jani.extensions.derivedoperators.OperatorExp;
import epmc.jani.extensions.derivedoperators.OperatorSgn;
import epmc.jani.extensions.derivedoperators.OperatorTrunc;
import epmc.jani.extensions.hyperbolicfunctions.OperatorAcosh;
import epmc.jani.extensions.hyperbolicfunctions.OperatorAsinh;
import epmc.jani.extensions.hyperbolicfunctions.OperatorAtanh;
import epmc.jani.extensions.hyperbolicfunctions.OperatorCosh;
import epmc.jani.extensions.hyperbolicfunctions.OperatorSinh;
import epmc.jani.extensions.hyperbolicfunctions.OperatorTanh;
import epmc.jani.extensions.trigonometricfunctions.OperatorAcos;
import epmc.jani.extensions.trigonometricfunctions.OperatorAsin;
import epmc.jani.extensions.trigonometricfunctions.OperatorAtan;
import epmc.jani.extensions.trigonometricfunctions.OperatorCos;
import epmc.jani.extensions.trigonometricfunctions.OperatorSin;
import epmc.jani.extensions.trigonometricfunctions.OperatorTan;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;
import epmc.prism.value.OperatorPRISMPow;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorAnd;
import epmc.value.OperatorCeil;
import epmc.value.OperatorDivideIgnoreZero;
import epmc.value.OperatorFloor;
import epmc.value.OperatorGe;
import epmc.value.OperatorIff;
import epmc.value.OperatorImplies;
import epmc.value.OperatorIte;
import epmc.value.OperatorLe;
import epmc.value.OperatorLog;
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

public class ExpressionOperatorProcessor implements JANI2PRISMProcessorStrict {

	private ExpressionOperator expressionOperator = null;
	private String prefix = null;
	
	@Override
	public JANI2PRISMProcessorStrict setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof ExpressionOperator; 
		
		expressionOperator = (ExpressionOperator) obj;
		return this;
	}

	@Override
	public JANI2PRISMProcessorStrict setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	@Override
	public String toPRISM() throws EPMCException {
		assert expressionOperator != null;
		
		StringBuilder prism = new StringBuilder();
		
		if (prefix != null) {
			prism.append(prefix);
		}
		
		Operator operator = expressionOperator.getOperator();
		if (operator.equals(OperatorNot.NOT)
			|| operator.equals(OperatorFloor.FLOOR)
			|| operator.equals(OperatorCeil.CEIL)) {
        	prism.append(operator);
            prism.append("(");
    		prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
    		prism.append(")");
		} else if (operator.equals(OperatorAddInverse.ADD_INVERSE)) {
            prism.append("-(");
    		prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
            prism.append(")");
		} else if (operator.equals(OperatorIte.ITE)) {
        	boolean needBraces = true;
            prism.append("(");
    		prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
            prism.append(")");
            prism.append(" ? ");
    		Expression exp = expressionOperator.getOperand2();
            if (exp instanceof ExpressionLiteral || exp instanceof ExpressionIdentifier) {
            	needBraces = false;
            }
            if (needBraces) {
            	prism.append("(");
            }
    		prism.append(ProcessorRegistrar.getProcessor(exp).toPRISM());
            if (needBraces) {
            	prism.append(")");
            }
            prism.append(" : ");
            needBraces = true;
    		exp = expressionOperator.getOperand3();
            if (exp instanceof ExpressionLiteral || exp instanceof ExpressionIdentifier) {
            	needBraces = false;
            }
            if (needBraces) {
            	prism.append("(");
            }
    		prism.append(ProcessorRegistrar.getProcessor(exp).toPRISM());
            if (needBraces) {
            	prism.append(")");
            }
		} else if (operator.equals(OperatorMin.MIN)
				|| operator.equals(OperatorMax.MAX)
				|| operator.equals(OperatorPow.POW)
				|| operator.equals(OperatorLog.LOG)) {
            prism.append(operator);
            prism.append("(");
    		prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
            prism.append(", ");
    		prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand2()).toPRISM());
            prism.append(")");
		} else if (operator.equals(OperatorPRISMPow.PRISM_POW)) {
            prism.append("pow(");
    		prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
            prism.append(", ");
    		prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand2()).toPRISM());
            prism.append(")");
		} else if (operator.equals(OperatorMod.MOD)) {
            prism.append("mod(");
    		prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
            prism.append(", ");
    		prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand2()).toPRISM());
            prism.append(")");
		} else {
        	List<Expression> children = expressionOperator.getChildren();
        	String operatorSymbol = null;
        	if (operator.equals(OperatorAnd.AND)) {
    			operatorSymbol = "&";
        	} else if (operator.equals(OperatorDivideIgnoreZero.DIVIDE_IGNORE_ZERO)) {
    			operatorSymbol = "/";
        	} else if (operator.equals(OperatorGe.GE)) {
    			operatorSymbol = ">=";
        	} else if (operator.equals(OperatorLe.LE)) {
    			operatorSymbol = "<=";
        	} else if (operator.equals(OperatorIff.IFF)) {
    			operatorSymbol = "<=>";
        	} else if (operator.equals(OperatorImplies.IMPLIES)) {
    			operatorSymbol = "=>";
        	} else if (operator.equals(OperatorOr.OR)) {
    			operatorSymbol = "|";
        	} else if (operator.equals(OperatorMultiplyInverse.MULTIPLY_INVERSE)) {
    			operatorSymbol = "1/";
        	} else if (operator.equals(OperatorNe.NE)) {
    			operatorSymbol = "!=";
        	} else {
    			//TODO: Maybe the following operators can be recovered
        		/*
    		case OperatorAbs.IDENTIFIER:
    		case OperatorExp.IDENTIFIER:
    		case OperatorSgn.IDENTIFIER:
    		case OperatorTrunc.IDENTIFIER:
    		*/
    			//these no.
        		/*
    		case OperatorAcosh.IDENTIFIER:
    		case OperatorAsinh.IDENTIFIER:
    		case OperatorAtanh.IDENTIFIER:
    		case OperatorCosh.IDENTIFIER:
    		case OperatorSinh.IDENTIFIER:
    		case OperatorTanh.IDENTIFIER:
    		case OperatorAcos.IDENTIFIER:
    		case OperatorAsin.IDENTIFIER:
    		case OperatorAtan.IDENTIFIER:
    		case OperatorCos.IDENTIFIER:
    		case OperatorSin.IDENTIFIER:
    		case OperatorTan.IDENTIFIER:
    			ensure(false, ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_UNKNOWN_OPERATOR, operator);
    			*/
    			operatorSymbol = operator.toString();
        	}
            if (children.size() == 1) {
                prism.append(operatorSymbol)
                	 .append("(")
                	 .append(ProcessorRegistrar.getProcessor(children.get(0)).toPRISM())
                	 .append(")");
            } else {
            	boolean remaining = false;
	            for (Expression child : children) {
	                boolean needBraces = true;
	                if (remaining) {
	                    prism.append(" " + operatorSymbol + " ");
	                } else {
	                	remaining = true;
	                }
	                if (child instanceof ExpressionOperator) {
	                    ExpressionOperator childOp = (ExpressionOperator) child;
	                    if (operator.equals(childOp.getOperator())) {
	                        needBraces = false;
	                    }
	                    if ((OperatorAdd.ADD.equals(expressionOperator.getOperator())
	                    		|| OperatorSubtract.SUBTRACT.equals(expressionOperator.getOperator()))
	                    		&& (OperatorMultiply.MULTIPLY.equals(childOp.getOperator())
	                            		|| OperatorDivideIgnoreZero.DIVIDE_IGNORE_ZERO.equals(childOp.getOperator()))) {
	                        needBraces = false;
	                    }
	                }
	                if (child instanceof ExpressionLiteral || child instanceof ExpressionIdentifier) {
	                	needBraces = false;
	                }
	                if (needBraces) {
	                    prism.append("(");
	                }
	        		prism.append(ProcessorRegistrar.getProcessor(child)
	        									   .toPRISM());
	                if (needBraces) {
	                    prism.append(")");
	                }
	            }
            }
        }

        return prism.toString();
	}

	@Override
	public void validateTransientVariables() throws EPMCException {
		assert expressionOperator != null;
		
		for (Expression child : expressionOperator.getChildren()) {
			ProcessorRegistrar.getProcessor(child).validateTransientVariables();
		}
	}
	
	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert expressionOperator != null;
		
		boolean usesTransient = false;
		for (Expression child : expressionOperator.getChildren()) {
			usesTransient |= ProcessorRegistrar.getProcessor(child).usesTransientVariables();
		}
		
		return usesTransient;
	}	
}
