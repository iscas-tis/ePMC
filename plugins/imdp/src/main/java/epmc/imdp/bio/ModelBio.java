package epmc.imdp.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.graph.CommonProperties;
import epmc.graph.LowLevel;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.modelchecker.Model;
import epmc.modelchecker.Properties;
import epmc.modelchecker.PropertiesDummy;
import epmc.operator.OperatorIsZero;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeObject;
import epmc.value.TypeWeightTransition;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueArrayInteger;
import epmc.value.ValueBoolean;
import epmc.value.ValueSetString;

public final class ModelBio implements Model {
    private final static int UNDEFINED = -1;
    private final static int NUM_ACTIONS = 2;
    
    private final static class ParseData {
        private int numGenes = UNDEFINED;
        private int targetGene = UNDEFINED;
        private int controlGene = UNDEFINED;
        private int numStates = UNDEFINED;
        private BitSet uStates;
        private BitSet dStates;
        private BitSet daStates;
        private GraphExplicitSparseAlternate graph;
    }

    @FunctionalInterface
    private static interface ParsePart {
        void parse(StreamTokenizer tokenizer, ParseData data) throws IOException;
    }
    
    public final static String IDENTIFIER = "imdp-bio";
    private final static String N_GENES = "n_genes";
    private final static String TARGET_GENE = "targetGene";
    private final static String CONTROL_GENE = "controlGene";
    private final static String N_STATES ="n_states";
    private final static String U_STATES = "U_states";
    private final static String D_STATES = "D_states";
    private final static String DA_STATES = "Da_states";
    private final static String INTERLEAVED_TRANSITION_MATRICES_0_1 = "interleaved_transition_matrices_0_1";
    private final static String UNSAFE = "unsafe";
    private final static String AMBIGUOUS = "ambiguous";
    
    private final static Map<String,ParsePart> PARSERS;
    static {
        Map<String,ParsePart> map = new HashMap<>();
        map.put(N_GENES, ModelBio::parseNGenes);
        map.put(TARGET_GENE, ModelBio::parseTargetGene);
        map.put(CONTROL_GENE, ModelBio::parseControlGene);
        map.put(N_STATES, ModelBio::parseNumStates);
        map.put(U_STATES, ModelBio::parseUStates);
        map.put(D_STATES, ModelBio::parseDStates);
        map.put(DA_STATES, ModelBio::parseDaStates);
        map.put(INTERLEAVED_TRANSITION_MATRICES_0_1, ModelBio::parseTransitions);
        PARSERS = Collections.unmodifiableMap(map);
    }
    
    private Properties properties;
    private GraphExplicitSparseAlternate graph;
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void read(Object identifier, InputStream... inputs) {
        assert inputs != null;
        assert inputs.length == 1;
        for (InputStream input : inputs) {
            assert input != null;
        }
        try {
            readStuff(inputs[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        properties = new PropertiesDummy();
    }

    private void readStuff(InputStream input) throws IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(new InputStreamReader(input));
        tokenizer.resetSyntax();
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('A', 'Z');
        tokenizer.wordChars('.', '.');
        tokenizer.wordChars('+', '+');
        tokenizer.wordChars('-', '-');
        tokenizer.whitespaceChars(' ', ' ');
        tokenizer.whitespaceChars(',', ',');
        tokenizer.whitespaceChars('\r', '\r');
        tokenizer.whitespaceChars('\n', '\n');
        tokenizer.wordChars('_', '_');
        ParseData parseData = new ParseData();
        for (int tokenType = tokenizer.nextToken(); tokenType != StreamTokenizer.TT_EOF; tokenType = tokenizer.nextToken()) {
            assert tokenType == StreamTokenizer.TT_WORD : tokenizer;
            ParsePart call = PARSERS.get(tokenizer.sval);
            assert call != null : tokenizer.sval;
            call.parse(tokenizer, parseData);
        }
        this.graph = parseData.graph;
    }
    
    private static void parseNGenes(StreamTokenizer tokenizer, ParseData data) throws IOException {
        assert tokenizer != null;
        assert data != null;
        int tokenType = tokenizer.nextToken();
        assert tokenType == StreamTokenizer.TT_WORD;
        int numGenes = Integer.parseInt(tokenizer.sval);
        assert numGenes > 0;
        data.numGenes = numGenes;
    }

    private static void parseTargetGene(StreamTokenizer tokenizer, ParseData data) throws IOException {
        assert tokenizer != null;
        assert data != null;
        int tokenType = tokenizer.nextToken();
        assert tokenType == StreamTokenizer.TT_WORD;
        int targetGene = Integer.parseInt(tokenizer.sval);
        assert targetGene > 0;
        targetGene--;
        data.targetGene = targetGene;
    }

    private static void parseControlGene(StreamTokenizer tokenizer, ParseData data) throws IOException {
        assert tokenizer != null;
        assert data != null;
        int tokenType = tokenizer.nextToken();
        assert tokenType == StreamTokenizer.TT_WORD;
        int controlGene = Integer.parseInt(tokenizer.sval);
        assert controlGene > 0;
        controlGene--;
        data.controlGene = controlGene;
    }

    private static void parseNumStates(StreamTokenizer tokenizer, ParseData data) throws IOException {
        assert tokenizer != null;
        assert data != null;
        int tokenType = tokenizer.nextToken();
        assert tokenType == StreamTokenizer.TT_WORD;
        int numStates = Integer.parseInt(tokenizer.sval);
        assert numStates > 0;
        data.numStates = numStates;
    }

    private static void parseUStates(StreamTokenizer tokenizer, ParseData data) throws IOException {
        assert tokenizer != null;
        assert data != null;
        assert data.numStates != UNDEFINED;
        data.uStates = UtilBitSet.newBitSetBounded(data.numStates);
        for (int tokenType = tokenizer.nextToken(); tokenType == StreamTokenizer.TT_WORD; tokenType = tokenizer.nextToken()) {
            int state;
            try {
                state = Integer.parseInt(tokenizer.sval);
            } catch (NumberFormatException e) {
                break;
            }
            assert state > 0;
            assert state <= data.numStates;
            state--;
            data.uStates.set(state);
        }
        tokenizer.pushBack();
    }

    private static void parseDStates(StreamTokenizer tokenizer, ParseData data) throws IOException {
        assert tokenizer != null;
        assert data != null;
        assert data.numStates != UNDEFINED;
        data.dStates = UtilBitSet.newBitSetBounded(data.numStates);
        for (int tokenType = tokenizer.nextToken(); tokenType == StreamTokenizer.TT_WORD; tokenType = tokenizer.nextToken()) {
            int state;
            try {
                state = Integer.parseInt(tokenizer.sval);
            } catch (NumberFormatException e) {
                break;
            }
            assert state > 0;
            assert state <= data.numStates;
            state--;
            data.dStates.set(state);
        }
        tokenizer.pushBack();
    }

    private static void parseDaStates(StreamTokenizer tokenizer, ParseData data) throws IOException {
        assert tokenizer != null;
        assert data != null;
        assert data.numStates != UNDEFINED;
        data.daStates = UtilBitSet.newBitSetBounded(data.numStates);
        for (int tokenType = tokenizer.nextToken(); tokenType == StreamTokenizer.TT_WORD; tokenType = tokenizer.nextToken()) {
            int state;
            try {
                state = Integer.parseInt(tokenizer.sval);
            } catch (NumberFormatException e) {
                break;
            }
            assert state > 0;
            assert state <= data.numStates;
            state--;
            data.daStates.set(state);
        }
        tokenizer.pushBack();
    }

    private static void parseTransitions(StreamTokenizer tokenizer, ParseData data) throws IOException {
        assert tokenizer != null;
        assert data != null;
        assert data.numStates != UNDEFINED;
        GraphExplicitSparseAlternate graph = new GraphExplicitSparseAlternate(data.numStates, data.numStates * NUM_ACTIONS, data.numStates * data.numStates * NUM_ACTIONS);
        graph.getInitialNodes().set(0);
        ValueArrayInteger stateBounds = graph.getStateBounds();
        ValueArrayInteger nondetBounds = graph.getNondetBounds();
        ValueArrayInteger successors = graph.getTargets();
        int totalNumActions = 0;
        int totalNumSuccessors = 0;
        ValueAlgebra succProb = TypeWeightTransition.get().newValue();
        ValueArrayAlgebra weights = TypeWeightTransition.get().getTypeArray().newValue();
        weights.setSize(data.numStates * data.numStates * NUM_ACTIONS);
        OperatorEvaluator isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, TypeWeightTransition.get());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        for (int fromState = 0; fromState < data.numStates; fromState++) {
            stateBounds.set(fromState * NUM_ACTIONS, fromState);
            for (int action = 0; action < NUM_ACTIONS; action++) {
                nondetBounds.set(totalNumSuccessors, totalNumActions);
                for (int toState = 0; toState < data.numStates; toState++) {
                    int tokenType = tokenizer.nextToken();
                    assert tokenType == StreamTokenizer.TT_WORD : tokenType;
                    ValueSetString.as(succProb).set(tokenizer.sval);
                    isZero.apply(cmp, succProb);
                    if (!cmp.getBoolean()) {
                        successors.set(toState, totalNumSuccessors);
                        weights.set(succProb, totalNumSuccessors);
                        totalNumSuccessors++;
                    }
                }
                totalNumActions++;
            }
        }
        stateBounds.set(data.numStates * NUM_ACTIONS, data.numStates);
        nondetBounds.set(totalNumSuccessors, totalNumActions);
        graph.addSettableGraphProperty(CommonProperties.SEMANTICS, new TypeObject.Builder().setClazz(Semantics.class).build());
        graph.setGraphProperty(CommonProperties.SEMANTICS, SemanticsMDP.MDP);
        graph.registerNodeProperty(CommonProperties.STATE, new NodePropertyStateBio(graph));
        graph.registerNodeProperty(CommonProperties.PLAYER, new NodePropertyPlayerBio(graph));
        graph.registerEdgeProperty(CommonProperties.WEIGHT, new GraphExplicitSparseAlternate.EdgePropertySparseNondetOnlyNondet(graph, weights));
        graph.registerNodeProperty(new ExpressionIdentifierStandard.Builder()
                .setName(UNSAFE)
                .build(), new NodePropertyStateBitSetBio(graph, data.uStates));
        graph.registerNodeProperty(new ExpressionIdentifierStandard.Builder()
                .setName(AMBIGUOUS)
                .build(), new NodePropertyStateBitSetBio(graph, data.daStates));
        data.graph = graph;
    }
    
    @Override
    public Semantics getSemantics() {
        return SemanticsMDP.MDP;
    }

    LowLevel newGraphExplicit(Set<Object> graphProperties, Set<Object> nodeProperties,
            Set<Object> edgeProperties) {
        return graph;
    }

    @Override
    public Properties getPropertyList() {
        return properties;
    }

}
