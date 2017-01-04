package epmc.expression.standard.evaluatorexplicit.compile;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.options.Options;
import epmc.util.Util;

public final class UtilEvaluatorExplicitCompile {
    private final static String CLASS_NAME = "CompiledExpression";
    
    public static void compile(Class<?> returnType, Expression expression, Expression[] variables) throws EPMCException {
        ExpressionToCode expressionToCode = findExpressionToCode(returnType, expression, variables);
        StringBuilder builder = new StringBuilder();
        builder.append(expressionToCode.generateImports());
        builder.append("public final static class " + CLASS_NAME + "{\n");
        builder.append(expressionToCode.generateFields());
        builder.append(expressionToCode.generateMethods());
        builder.append(expressionToCode.generateTree());
        builder.append("}");
        System.out.println(builder);
    }
    
    private static ExpressionToCode findExpressionToCode(Class<?> returnType, Expression expression, Expression[] variables) throws EPMCException {
    	// TODO    	
//        Options options = expression.getContext().getContextValue().getOptions();
      Options options = null;
        Map<String,Class<? extends ExpressionToCode>> evaluatorsToCode = options.get(OptionsExpressionBasic.EXPRESSION_EXPRESSION_TO_CODE_CLASS);
        for (Class<? extends ExpressionToCode> clazz : evaluatorsToCode.values()) {
            ExpressionToCode instance = Util.getInstance(clazz);
            instance.setExpression(expression);
            instance.setVariables(variables);
            instance.setReturnType(returnType);
            if (instance.canHandle()) {
                return instance;
            }
        }
        assert false : expression;
        return null;
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilEvaluatorExplicitCompile() {
    }
}
