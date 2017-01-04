package epmc.param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.param.options.OptionsParam;
import epmc.param.plugin.BeforeModelLoadingPARAM;
import epmc.param.value.ContextValuePARAM;
import epmc.param.value.Point;
import epmc.param.value.TypeFunction;
import epmc.param.value.ValueFunction;
import epmc.value.TypeReal;
import epmc.value.ValueReal;

import static epmc.modelchecker.TestHelper.*;
import static epmc.param.PARAMTestHelper.*;

public final class PolynomialTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }
    
    @Test
    public void evalTest() throws EPMCException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);
        List<String> parameters = new ArrayList<>();
        parameters.add("x");
        parameters.add("y");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        
        processAfterModelLoading(options);

        ContextValuePARAM contextValuePARAM = options.get(BeforeModelLoadingPARAM.PARAM_CONTEXT_VALUE_PARAM);        
        assert contextValuePARAM != null;
        TypeFunction typeFunction = contextValuePARAM.getTypeFunction();
        ValueFunction a = typeFunction.newValue();
        ValueFunction b = typeFunction.newValue();
        a.set("x^7*y^9+49*x^2+7*x^6*y^2+7*x^3*y^7");
        b.set("x^2*y^7+7*x");
        b.set(a.floorInt());
        System.out.println("FLOOR " + b);
        b.multiply(b, b);
        b.add(b, b);
        System.out.println("FLOOR " + b);
        Point point = new Point(typeFunction);
        /// TODO fix
//        point.setDimension("2", 0);
  ///      point.setDimension("0.2", 1);
        ValueReal eval = TypeReal.get(contextValuePARAM.getContextValue()).newValue();
        a.evaluate(eval, point);
        System.out.println("eval " + a + "  " + eval);
        a.set(5);
        b.divide(b, a);
        b.evaluate(eval, point);
        System.out.println("eval " + b + "  " + eval);
        System.out.println("a " + a);
        System.out.println("b " + b);
        /*
        typeFunction.cancelCommonFactors(a, b);
        System.out.println("a " + a);
        System.out.println("b " + b);
        a.subtract(a, a);
        System.out.println("a " + a);
        */
    }    
}
