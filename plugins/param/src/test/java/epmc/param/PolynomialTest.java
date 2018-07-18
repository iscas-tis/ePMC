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
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.param.options.OptionsParam;
import epmc.param.plugin.BeforeModelLoadingPARAM;
import epmc.param.value.ParameterSet;
import epmc.param.value.TypeFunction;
import epmc.param.value.ValueFunction;
import epmc.param.value.dag.exporter.ExporterGraphviz;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.param.value.polynomialfraction.TypePolynomialFraction;
import epmc.param.value.polynomialfraction.exporter.PolynomialFractionExporterDag;
import epmc.param.value.polynomialfraction.exporter.PolynomialToDag;
import epmc.util.StopWatch;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeDouble;
import epmc.value.UtilValue;
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
        processBeforeModelLoading(options);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);
        List<String> parameters = new ArrayList<>();
        parameters.add("x");
        parameters.add("y");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        processAfterModelLoading(options);

        ParameterSet parameterSet = options.get(BeforeModelLoadingPARAM.PARAM_CONTEXT_VALUE_PARAM);
        parameterSet.addParameter("x");
        parameterSet.addParameter("y");
        assert parameterSet != null;
        TypePolynomial typePoly = new TypePolynomial(parameterSet);
        TypeFunction.set(typePoly);
        TypePolynomial typeFunction = typePoly;
        ValuePolynomial a = typeFunction.newValue();
        ValuePolynomial b = typeFunction.newValue();
        a.set("x^7+y^7-10*x^2+7*x^6*y^3-7*x^3*y^7");
        b.set("x^2*y^7+7*x");
        
//        b.set(a.floorInt());
        System.out.println("FLOOR " + b);
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, typeFunction, typeFunction);
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, typeFunction, typeFunction);
        multiply.apply(b, b, b);
        add.apply(b, b, b);
        System.out.println("FLOOR " + b);
     //   OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeReal.get(), TypeReal.get());
        ValueReal eval = TypeDouble.get().newValue();
        OperatorEvaluator evaluate = ContextValue.get().getEvaluator(a, TypeDouble.get(), TypeDouble.get());
        OperatorEvaluator addDouble = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeDouble.get(), TypeDouble.get());
        OperatorEvaluator divideDouble = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeDouble.get(), TypeDouble.get());
        int numBoxes = 100;
        ValueReal v1 = UtilValue.newValue(TypeDouble.get(), "0");
        ValueReal v2 = UtilValue.newValue(TypeDouble.get(), "0");
        ValueReal one = UtilValue.newValue(TypeDouble.get(), "1");
        ValueReal zero = UtilValue.newValue(TypeDouble.get(), "0");
        ValueReal delta = TypeDouble.get().newValue();
        delta.set(numBoxes);
        divideDouble.apply(delta, one, delta);
        OperatorEvaluator setDouble = ContextValue.get().getEvaluator(OperatorSet.SET, TypeDouble.get(), TypeDouble.get());
        System.out.println("beginEvalLoop");
        StopWatch watch = new StopWatch(true);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= numBoxes; i++) {
            setDouble.apply(v2, zero);
            for (int j = 0; j <= numBoxes; j++) {
                evaluate.apply(eval, v1, v2);
                builder.append(v1.getDouble());
                builder.append(' ');
                builder.append(v2.getDouble());
                builder.append(' ');
                builder.append(eval.getDouble());
                builder.append('\n');
                addDouble.apply(v2, v2, delta);                
            }
            builder.append('\n');
            addDouble.apply(v1, v1, delta);
        }
        System.out.println("endEvalLoop " + watch.getTime());
        System.out.println(builder.toString());
        
        a.set(5);
       // divide.apply(b,  b, a);
        evaluate = ContextValue.get().getEvaluator(b, TypeDouble.get(), TypeDouble.get());
        evaluate.apply(eval, v1, v2);
        System.out.println("A " + v1 + " " + v2);
        System.out.println("eval " + b + "  " + eval);
        System.out.println("a " + a);
        System.out.println("b " + b);
        PolynomialToDag to = new PolynomialToDag(typePoly);
//        options.set(OptionsParam.PARAM_FRACTION_EXPORTER, PolynomialFractionExporterDag);
//        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterGraphviz.IDENTIFIER);
        System.out.println("ZZ " + to.convert(b));
        /*
        typeFunction.cancelCommonFactors(a, b);
        System.out.println("a " + a);
        System.out.println("b " + b);
        a.subtract(a, a);
        System.out.println("a " + a);
        */
    }    
    
    @Test
    public void evalFractionTest() throws EPMCException {
        Options options = preparePARAMOptions();
        processBeforeModelLoading(options);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);
        List<String> parameters = new ArrayList<>();
        parameters.add("x");
        parameters.add("y");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        processAfterModelLoading(options);

        ParameterSet parameterSet = options.get(BeforeModelLoadingPARAM.PARAM_CONTEXT_VALUE_PARAM);
        parameterSet.addParameter("p");
        parameterSet.addParameter("q");
        assert parameterSet != null;
        TypePolynomial typePoly = new TypePolynomial(parameterSet);
        TypePolynomialFraction typeFraction = new TypePolynomialFraction(typePoly);
        TypeFunction.set(typeFraction);
        TypeFunction typeFunction = typeFraction;
        ValueFunction function = typeFunction.newValue();
        function.set("-p^19*q -p^18*q -p^17*q -p^16*q -p^15*q -p^14*q -p^13*q -p^12*q -p^11*q -p^10*q -p^9*q -p^8*q -p^7*q -p^6*q -p^5*q -p^4*q -p^3*q -p^2*q -p*q +18*q -20/-p^20*q +q -1");

        
        ValueReal eval = TypeDouble.get().newValue();
        OperatorEvaluator evaluate = ContextValue.get().getEvaluator(function, TypeDouble.get(), TypeDouble.get());
        OperatorEvaluator divideDouble = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeDouble.get(), TypeDouble.get());
        int numBoxes = 100;
        ValueReal v1 = UtilValue.newValue(TypeDouble.get(), "0");
        ValueReal v2 = UtilValue.newValue(TypeDouble.get(), "0");
        ValueReal one = UtilValue.newValue(TypeDouble.get(), "1");
        ValueReal delta = TypeDouble.get().newValue();
        delta.set(numBoxes);
        divideDouble.apply(delta, one, delta);
        System.out.println("beginEvalLoop");
        StopWatch watch = new StopWatch(true);
        StringBuilder builder = new StringBuilder();
        ValueReal doubleI = TypeDouble.get().newValue();
        ValueReal doubleJ = TypeDouble.get().newValue();
        ValueReal numBoxesReal = TypeDouble.get().newValue();
        numBoxesReal.set(numBoxes);
        for (int i = 0; i <= numBoxes; i++) {
            doubleI.set(i);
            divideDouble.apply(v1, doubleI, numBoxesReal);
            for (int j = 0; j <= numBoxes; j++) {
                doubleJ.set(j);
                divideDouble.apply(v2, doubleJ, numBoxesReal);
                evaluate.apply(eval, v1, v2);
                builder.append(v1.getDouble());
                builder.append(' ');
                builder.append(v2.getDouble());
                builder.append(' ');
                builder.append(eval.getDouble());
                builder.append('\n');
            }
            builder.append('\n');
        }
        System.out.println("endEvalLoop " + watch.getTime());
        System.out.println(builder.toString());
    }    

}
