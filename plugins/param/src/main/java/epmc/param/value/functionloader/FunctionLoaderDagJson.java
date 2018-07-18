package epmc.param.value.functionloader;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorMultiplyInverse;
import epmc.param.value.ParameterSet;
import epmc.param.value.TypeFunction;
import epmc.param.value.ValueFunction;
import epmc.param.value.dag.OperatorType;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeArrayAlgebra;
import epmc.value.UtilValue;
import epmc.value.ValueArray;

public final class FunctionLoaderDagJson implements FunctionLoader {
    public final static class Builder implements FunctionLoader.Builder {

        @Override
        public FunctionLoader build() {
            return new FunctionLoaderDagJson(this);
        }
        
    }
    
    public final static String IDENTIFIER = "dag-json";

    private final static String PARAMETERS = "parameters";
    private final static String TYPE = "type";
    private final static String NUM_NODES = "num-nodes";
    private final static String DAG = "dag";
    private final static String FUNCTIONS = "functions";
    private final static String OPERATOR = "operator";
    private final static String NUMBER = "number";
    private final static String PARAMETER = "parameter";
    private final static String ADD_INVERSE = "add-inverse";
    private final static String MULTIPLY_INVERSE = "multiply-inverse";
    private final static String ADD = "add";
    private final static String MULTIPLY = "multiply";
    private final static String NUMERATOR = "numerator";
    private final static String DENOMINATOR = "denominator";
    private final static String OPERAND = "operand";
    private final static String OPERAND_LEFT = "operand-left";
    private final static String OPERAND_RIGHT = "operand-right";
    private final static Map<String, OperatorType> OPERATOR_MAP;
    static {
        Map<String,OperatorType> map = new HashMap<String,OperatorType>();
        map.put(NUMBER, OperatorType.NUMBER);
        map.put(PARAMETER, OperatorType.PARAMETER);
        map.put(ADD_INVERSE, OperatorType.ADD_INVERSE);
        map.put(MULTIPLY_INVERSE, OperatorType.MULTIPLY_INVERSE);
        map.put(ADD, OperatorType.ADD);
        map.put(MULTIPLY, OperatorType.MULTIPLY);
        OPERATOR_MAP = Collections.unmodifiableMap(map);
    }

    private FunctionLoaderDagJson(Builder builder) {
        assert builder != null;
    }

    @Override
    public List<ValueFunction> readFunctions(InputStream... inputs) {
        assert inputs != null;
        for (InputStream input : inputs) {
            assert input != null;
        }
        ArrayList<ValueFunction> result = new ArrayList<>();
        for (InputStream input : inputs) {
            readFunctions(result, input);
        }
        return result;
    }
    
    private void readFunctions(ArrayList<ValueFunction> result, InputStream input) {
        assert result != null;
        assert input != null;
        TypeFunction typeFunction = TypeFunction.get();
        TypeArrayAlgebra typeFunctionArray = typeFunction.getTypeArray();
        JsonParser parser = Json.createParser(input);
        ValueArray dag = null;
        switch (parser.next()) {
        case START_OBJECT:
            break;
        default:
            assert false;
            break;
        }
        // TODO change most assert statements to user error messages
        while (parser.hasNext()) {
            switch (parser.next()) {
            case KEY_NAME:
                switch (parser.getString()) {
                case TYPE:
                    Event next = parser.next();
                    assert next == Event.VALUE_STRING;
                    assert parser.getString().equals(DAG);
                    break;
                case PARAMETERS:
                    readParameters(parser);
                    break;
                case NUM_NODES:
                    next = parser.next();
                    assert next == Event.VALUE_NUMBER;
                    int arraySize = parser.getInt();
                    dag = UtilValue.newArray(typeFunctionArray, arraySize);
                    break;
                case DAG:
                    readDag(parser, dag);
                    break;
                case FUNCTIONS:
                    readFunctions(result, parser, dag);
                    break;
                default:
                    assert false : parser.getString();
                }
                break;
            case END_OBJECT:
                break;
            default:
                assert false;
                break;
            }
        }
    }

    private void readParameters(JsonParser parser) {
        assert parser != null;
        Event next = parser.next();
        assert next == Event.START_ARRAY;
        Event event = parser.next();
        int index = 0;
        ParameterSet parameters = TypeFunction.get().getParameterSet();
        while (event != Event.END_ARRAY) {
            assert event == Event.VALUE_STRING;
            String parameter = parser.getString();
            event = parser.next();
            if (!parameters.isParameter(parameter)) {
                parameters.addParameter(parameter);
            } else {
                assert parameters.getParameterNumber(parameter) == index;
            }
            index++;
        }
    }

    private void readFunctions(ArrayList<ValueFunction> result, JsonParser parser, ValueArray dag) {
        assert result != null;
        assert parser != null;
        assert dag != null;
        Event event = parser.next();
        assert event == Event.START_ARRAY;
        while (event != Event.END_ARRAY) {
            assert event == Event.START_ARRAY || event == Event.VALUE_NUMBER;
            if (event == Event.START_ARRAY) {
                event = parser.next();
                continue;
            }
            int functionNumber = parser.getInt();
            ValueFunction function = TypeFunction.get().newValue();
            dag.get(function, functionNumber);
            result.add(function);
            event = parser.next();
        }
    }

    private void readDag(JsonParser parser, ValueArray dag) {
        Event next = parser.next();
        assert next == Event.START_ARRAY;
        Event nextEvent = parser.next();
        ValueFunction function = TypeFunction.get().newValue();
        ValueFunction functionNum = TypeFunction.get().newValue();
        ValueFunction functionDen = TypeFunction.get().newValue();
        ValueFunction operandFunction = TypeFunction.get().newValue();
        ValueFunction operandLeftFunction = TypeFunction.get().newValue();
        ValueFunction operandRightFunction = TypeFunction.get().newValue();
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeFunction.get(), TypeFunction.get());
        OperatorEvaluator addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, TypeFunction.get());
        OperatorEvaluator multiplyInverse = ContextValue.get().getEvaluator(OperatorMultiplyInverse.MULTIPLY_INVERSE, TypeFunction.get());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeFunction.get(), TypeFunction.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeFunction.get(), TypeFunction.get());
        int index = 0;
        while (nextEvent != Event.END_ARRAY) {
            assert nextEvent == Event.START_OBJECT;
            OperatorType type = null;
            BigInteger numerator = null;
            BigInteger denominator = null;
            int operand = -1;
            int operandLeft = -1;
            int operandRight = -1;
            String parameter = null;
            while (nextEvent != Event.END_OBJECT) {
                if (nextEvent == Event.START_OBJECT) {
                    nextEvent = parser.next();
                    continue;
                }
                assert nextEvent == Event.KEY_NAME : nextEvent;
                switch (parser.getString()) {
                case OPERATOR:
                    nextEvent = parser.next();
                    assert nextEvent == Event.VALUE_STRING;
                    type = OPERATOR_MAP.get(parser.getString());
                    break;
                case NUMERATOR:
                    nextEvent = parser.next();
                    assert nextEvent == Event.VALUE_NUMBER;
                    numerator = parser.getBigDecimal().toBigIntegerExact();
                    break;
                case DENOMINATOR:
                    nextEvent = parser.next();
                    assert nextEvent == Event.VALUE_NUMBER;
                    denominator = parser.getBigDecimal().toBigIntegerExact();
                    break;
                case PARAMETER:
                    nextEvent = parser.next();
                    assert nextEvent == Event.VALUE_STRING;
                    parameter = parser.getString();
                    break;
                case OPERAND:
                    nextEvent = parser.next();
                    assert nextEvent == Event.VALUE_NUMBER;
                    operand = parser.getInt();
                    break;
                case OPERAND_LEFT:
                    nextEvent = parser.next();
                    assert nextEvent == Event.VALUE_NUMBER;
                    operandLeft = parser.getInt();
                    break;
                case OPERAND_RIGHT:
                    nextEvent = parser.next();
                    assert nextEvent == Event.VALUE_NUMBER;
                    operandRight = parser.getInt();
                    break;
                default:
                    assert false;
                    break;
                }
                nextEvent = parser.next();
            }
            assert type != null;
            switch (type) {
            case NUMBER:
                assert numerator != null;
                assert denominator != null;
                functionNum.set(numerator.toString());
                functionDen.set(denominator.toString());
                divide.apply(function, functionNum, functionDen);
                break;
            case PARAMETER:
                assert parameter != null;
                function.setParameter(parameter);
                break;
            case ADD_INVERSE:
                assert operand != -1;
                dag.get(operandFunction, operand);
                addInverse.apply(function, operandFunction);
                break;
            case MULTIPLY_INVERSE:
                assert operand != -1;
                dag.get(operandFunction, operand);
                multiplyInverse.apply(function, operandFunction);
                break;
            case ADD:
                dag.get(operandLeftFunction, operandLeft);
                dag.get(operandRightFunction, operandRight);
                add.apply(function, operandLeftFunction, operandRightFunction);
                break;
            case MULTIPLY:
                dag.get(operandLeftFunction, operandLeft);
                dag.get(operandRightFunction, operandRight);
                multiply.apply(function, operandLeftFunction, operandRightFunction);
                break;
            default:
                break;
            }
            dag.set(function, index);
            index++;
            nextEvent = parser.next();
        }
    }

}
