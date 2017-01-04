package epmc.propertysolverltlfairness;

import java.io.FileNotFoundException;

import epmc.dd.ContextDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.PropertiesImpl;
import epmc.modelchecker.RawProperties;
import epmc.modelchecker.RawProperty;
import epmc.modelchecker.TestHelper;
import epmc.options.Options;
import epmc.value.ContextValue;

public class LTLTest {
    
    public static void main(String[] args) throws EPMCException, FileNotFoundException {
        String file = "/home/liyong/projects/test/mdptt.pctl";
        Options options = UtilOptionsEPMC.newOptions();
        RawProperties props = LTLHelper.readProperties(options, file);
        ContextValue context = new ContextValue(options);
        options.set(TestHelper.CONTEXT_VALUE, context);
        
        PropertiesImpl pList = new PropertiesImpl(context);
        pList.parseProperties(props);
        Expression expression = null;
        
        for (RawProperty prop : props.getProperties()) {
            expression = pList.getParsedProperty(prop);
            System.out.println(expression);
            ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
            Expression opera = expressionQuantifier.getQuantified();
//            Set<Expression> labels = opera.collectIdentifiers();
//            if (PropertySolverExplicitLTLFairness.canHandleProp(opera)) {
//                Expression normForm = PropertySolverExplicitLTLFairness.getNormForm(opera, labels);
//                System.out.println("normal form:\n" + normForm);
//                System.out.println("flatten: \n" + PropertySolverExplicitLTLFairness.flatten(normForm, labels));
//            }else {
//                System.out.println("can not handle");
//            }
//            
            
                        
        }
        ContextDD.close(context);

        
        
    }

}
