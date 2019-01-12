package epmc.param.value.dag.exporter;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import com.google.common.io.CharStreams;

import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import epmc.param.value.dag.UtilDag;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class ExporterJson implements DagExporter {
    public final static String IDENTIFIER = "json";

    public final static class Builder implements DagExporter.Builder {
        private Dag dag;
        private final IntArrayList nodes = new IntArrayList();

        @Override
        public Builder setDag(Dag dag) {
            this.dag = dag;
            return this;
        }

        @Override
        public Builder addRelevantNode(int node) {
            nodes.add(node);
            return this;
        }

        @Override
        public DagExporter build() {
            return new ExporterJson(this);
        }
    }
    
    private final static String TYPE = "type";
    private final static String PARAMETERS = "parameters";
    private final static String DAG = "dag";
    private final static String NUM_NODES = "num-nodes";
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
    private final static String FUNCTIONS = "functions";
    private final static Map<OperatorType, String> OPERATOR_MAP;
    static {
        EnumMap<OperatorType, String> map = new EnumMap<OperatorType,String>(OperatorType.class);
        map.put(OperatorType.NUMBER, NUMBER);
        map.put(OperatorType.PARAMETER, PARAMETER);
        map.put(OperatorType.ADD_INVERSE, ADD_INVERSE);
        map.put(OperatorType.MULTIPLY_INVERSE, MULTIPLY_INVERSE);
        map.put(OperatorType.ADD, ADD);
        map.put(OperatorType.MULTIPLY, MULTIPLY);
        OPERATOR_MAP = Collections.unmodifiableMap(map);
    }
    
    private final Dag dag;
    private final int[] start;

    private ExporterJson(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        dag = builder.dag;
        start = builder.nodes.toIntArray();
    }

    @Override
    public void export(Appendable result) throws IOException {
        Map<String,Object> configuration = new HashMap<>();
        configuration.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonGeneratorFactory factory = Json.createGeneratorFactory(configuration);
        Writer writer = CharStreams.asWriter(result);
        JsonGenerator generator = factory.createGenerator(writer);
        generator.writeStartObject();
        writeType(generator);
        writeParameters(generator);
        int[] depending = UtilDag.findDepending(dag, start);
        IntArrayList remappedIndex = remapIndex(depending);
        generator.write(NUM_NODES, depending.length);
        appendNodes(generator, depending, remappedIndex);
        appendFunctions(generator, start, remappedIndex);
        generator.writeEnd();
        generator.flush();
    }

    private void writeParameters(JsonGenerator generator) {
        assert generator != null;
        generator.writeStartArray(PARAMETERS);
        for (Object parameter : dag.getParameters().getParameters()) {
            generator.write(parameter.toString());
        }
        generator.writeEnd();
    }

    private void writeType(JsonGenerator generator) {
        assert generator != null;
        generator.write(TYPE, DAG);
    }

    private void appendFunctions(JsonGenerator generator, int[] start, IntArrayList remappedIndex) {
        assert generator != null;
        assert start != null;
        generator.writeStartArray(FUNCTIONS);
        for (int function : start) {
            generator.write(remappedIndex.getInt(function));
        }
        generator.writeEnd();
    }

    private IntArrayList remapIndex(int[] depending) {
        IntArrayList result = new IntArrayList();
        for (int index = 0; index < depending.length; index++) {
            while (result.size() <= depending[index]) {
                result.add(-1);
            }
            result.set(depending[index], index);
        }
        return result;
    }

    private void appendNodes(JsonGenerator generator, int[] depending, IntArrayList remappedIndex) throws IOException {
        assert generator != null;
        generator.writeStartArray(DAG);
        for (int index = 0; index < depending.length; index++) {
            generator.writeStartObject();
            int number = depending[index];
            OperatorType type = dag.getOperatorType(number);
            generator.write(OPERATOR, OPERATOR_MAP.get(type));
            switch (type) {
            case NUMBER:
                generator.write(NUMERATOR, dag.getNumberNumerator(number));
                generator.write(DENOMINATOR, dag.getNumberDenominator(number));
                break;
            case PARAMETER:
                generator.write(PARAMETER, dag.getParameter(number).toString());
                break;
            case ADD_INVERSE:
            case MULTIPLY_INVERSE: {
                int operandNode = dag.getOperand(number);
                int operand = remappedIndex.getInt(operandNode);
                generator.write(OPERAND, operand);
                break;
            }
            case ADD:
            case MULTIPLY: {
                int operandLeftNode = dag.getOperandLeft(number);
                int operandLeft = remappedIndex.getInt(operandLeftNode);
                generator.write(OPERAND_LEFT, operandLeft);
                int operandRightNode = dag.getOperandRight(number);
                int operandRight = remappedIndex.getInt(operandRightNode);
                generator.write(OPERAND_RIGHT, operandRight);
                break;
            }
            default:
                assert false;
                break;
            }
            generator.writeEnd();
        }
        generator.writeEnd();
    }
}
