package epmc.propertysolverltlfg;

import java.io.FileNotFoundException;

import epmc.error.EPMCException;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.Properties;
import epmc.modelchecker.PropertiesImpl;
import epmc.modelchecker.RawProperties;
import epmc.modelchecker.RawProperty;
import epmc.modelchecker.TestHelper;
import epmc.options.Options;
import epmc.value.ContextValue;

public class LTLTest {
    
    public static void main(String[] args) throws EPMCException, FileNotFoundException {
        String file = "./examples/dice/nihao.pctl";
        Options options = UtilOptionsEPMC.newOptions();
        RawProperties props = LTLHelper.readProperties(options, file);
//        options.set(OptionsExpression.CONTEXT_EXPRESSION, context);
//        options.set(OptionsValue.CONTEXT_VALUE, context.getContextValue());
        
        ContextValue context = TestHelper.getContextValue(options);
        Properties pList = new PropertiesImpl(context);
        // TODO
        //        pList.parseProperties(props);
        Expression expression = null;
        
        for(RawProperty prop : props.getProperties()) {
            expression = pList.getParsedProperty(prop);
            System.out.println(expression);
            ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
            Expression opera = expressionQuantifier.getQuantified();
        }
    }

}
