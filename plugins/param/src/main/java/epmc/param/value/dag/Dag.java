package epmc.param.value.dag;

import java.io.IOException;
import java.math.BigInteger;

import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.param.options.OptionsParam;
import epmc.param.value.ParameterSet;
import epmc.param.value.dag.exporter.DagExporter;
import epmc.param.value.dag.exporter.ExporterGraphviz;
import epmc.param.value.dag.simplifier.Simplifier;
import epmc.param.value.dag.simplifier.SimplifierConstant;
import epmc.param.value.dag.simplifier.SimplifierProbabilistic;
import epmc.param.value.dag.simplifier.SimplifierSimpleAlgebraic;

public final class Dag {
    // TODO add optional reference counting
    private final NumberStore numberStore = new NumberStore();
    private final NodeStore nodeStore;
    private final NodeLookup nodeLookup;
    private final ParameterSet parameters;
    private final Simplifier[] simplifiers;
    private final EntryBuffer simplifyBuffer = new EntryBuffer();
    private int maxNode;
    
    Dag(ParameterSet parameters, boolean useReferenceCounting) {
        assert parameters != null;
        this.parameters = parameters;
        NodeStore.Builder nodeStoreBuilder = UtilOptions.getInstance(OptionsParam.PARAM_DAG_NODE_STORE);
        nodeStore = nodeStoreBuilder.build();
        NodeLookup.Builder nodeLookupBuilder = UtilOptions.getInstance(OptionsParam.PARAM_DAG_NODE_LOOKUP);
        nodeLookup = nodeLookupBuilder.setNodeStore(nodeStore).build();
        // TODO simplifiers to be chosen by user
        simplifiers = new Simplifier[3];
        simplifiers[0] = new SimplifierConstant.Builder()
                .setDag(this).build();
        simplifiers[1] = new SimplifierSimpleAlgebraic.Builder()
                .setDag(this).build();
        simplifiers[2] = new SimplifierProbabilistic.Builder()
                .setDag(this).build();
    }

    public int getParameter(String parameter) {
        assert parameter != null;
        assert parameters.isParameter(parameter);
        return checkin(OperatorType.PARAMETER, parameters.getParameterNumber(parameter), 0);
    }
    
    public int getNumber(BigInteger num, BigInteger den) {
        assert num != null;
        assert den != null;
        BigInteger gcd = num.gcd(den);
        num = num.divide(gcd);
        den = den.divide(gcd);
        if (num.compareTo(BigInteger.ZERO) < 0
                && den.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
            den = den.negate();
        } else if (num.compareTo(BigInteger.ZERO) > 0
                && den.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
            den = den.negate();
        }
        int numNr = getNumberOfValue(num);
        int denNr = getNumberOfValue(den);
        return checkin(OperatorType.NUMBER, numNr, denNr);
    }
    
    public int apply(OperatorType type, int operandLeft, int operandRight) {
        assert nodeStore.assertValidNumber(operandLeft) : operandLeft;
        assert nodeStore.assertValidNumber(operandRight) : operandRight;
        assert !type.isSpecial();
        assert type.getArity() == 2 : type;
        return checkin(type, operandLeft, operandRight);
    }

    public int apply(OperatorType type, int operand) {
        assert nodeStore.assertValidNumber(operand) : operand;
        assert !type.isSpecial();
        assert type.getArity() == 1 : type;
        return checkin(type, operand, 0);
    }

    private int checkin(OperatorType type, int operandLeft, int operandRight) {
        int result = nodeLookup.get(type, operandLeft, operandRight);
        if (result != Simplifier.INVALID) {
            return result;
        }
        EntryBuffer buffer = simplify(type, operandLeft, operandRight);
        result = buffer.getResultNode();
        boolean addedNode = false;
        if (result == Simplifier.INVALID) {
            if (type == buffer.getType()
                    && operandLeft == buffer.getOperandLeft()
                    && operandRight == buffer.getOperandRight()) {
                result = nodeStore.add(type, operandLeft, operandRight);
                nodeLookup.put(type, operandLeft, operandRight, result);
                addedNode = true;
            } else {
                result = nodeLookup.get(buffer.getType(), buffer.getOperandLeft(), buffer.getOperandRight());
                if (result == Simplifier.INVALID) {
                    result = nodeStore.add(buffer.getType(), buffer.getOperandLeft(), buffer.getOperandRight());
                    nodeLookup.put(type, operandLeft, operandRight, result);
                    addedNode = true;
                }
            }
        }
        if (addedNode) {
            for (Simplifier simplifier : simplifiers) {
                simplifier.tellNewEntry(result);
            }
        }
        maxNode = Math.max(result, maxNode);
        return result;
    }
    
    private EntryBuffer simplify(OperatorType type, int operandLeft, int operandRight) {
        Simplifier simplifier = null;
        for (Simplifier trySimplifier : simplifiers) {
            if (trySimplify(type, operandLeft, operandRight, trySimplifier)) {
                simplifier = trySimplifier;
                break;
            }
        }
        if (simplifier != null) {
            return simplifierToEntry(simplifier);
        } else {
            return simplifyBuffer
                    .setResultNode(Simplifier.INVALID)
                    .setType(type)
                    .setOperandLeft(operandLeft)
                    .setOperandRight(operandRight);
        }
    }

    private boolean trySimplify(OperatorType type, int operandLeft, int operandRight, Simplifier simplifier) {
        assert simplifier != null;
        simplifier.setType(type);
        simplifier.setOperandLeft(operandLeft);
        simplifier.setOperandRight(operandRight);
        return simplifier.simplify();
    }

    private EntryBuffer simplifierToEntry(Simplifier simplifier) {
        assert simplifier != null;
        return simplifyBuffer
                .setResultNode(simplifier.getResultNode())
                .setType(simplifier.getResultType())
        .setOperandLeft(simplifier.getResultOperandLeft())
        .setOperandRight(simplifier.getResultOperandRight());
    }

    public boolean isOne(int number) {
        OperatorType type = nodeStore.getType(number);
        if (type != OperatorType.NUMBER) {
            return false;
        }
        BigInteger num = numberStore.getNumber(nodeStore.getOperandLeft(number));
        BigInteger den = numberStore.getNumber(nodeStore.getOperandRight(number));
        if (!(BigInteger.ONE.equals(num)
                && BigInteger.ONE.equals(den))) {
            return false;
        }
        return true;
    }

    public boolean isZero(int number) {
        OperatorType type = nodeStore.getType(number);
        if (type != OperatorType.NUMBER) {
            return false;
        }
        BigInteger num = numberStore.getNumber(nodeStore.getOperandLeft(number));
        BigInteger den = numberStore.getNumber(nodeStore.getOperandRight(number));
        if (!(BigInteger.ZERO.equals(num)
                && BigInteger.ONE.equals(den))) {
            return false;
        }
        return true;
    }

    public String toString(int number) {
        DagExporter.Builder builder = UtilOptions.getInstance(OptionsParam.PARAM_DAG_EXPORTER);
        DagExporter exporter = builder
                .setDag(this)
                .addRelevantNode(number)
                .build();
        StringBuilder result = new StringBuilder();
        try {
            exporter.export(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }

    public String toString(int[] numbers) {
        DagExporter.Builder builder = UtilOptions.getInstance(OptionsParam.PARAM_DAG_EXPORTER);
        builder.setDag(this);
        for (int number : numbers) {
            builder.addRelevantNode(number);
        }
        StringBuilder result = new StringBuilder();
        try {
            builder.build().export(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }

    public OperatorType getOperatorType(int number) {
        assert nodeStore.assertValidNumber(number);
        return nodeStore.getType(number);
    }
    
    public BigInteger getNumberNumerator(int number) {
        assert nodeStore.assertValidNumber(number);
        return numberStore.getNumber(nodeStore.getOperandLeft(number));
    }
    
    public BigInteger getNumberDenominator(int number) {
        assert nodeStore.assertValidNumber(number);
        return numberStore.getNumber(nodeStore.getOperandRight(number));
    }
    
    public Object getParameter(int number) {
        assert nodeStore.assertValidNumber(number);
        return parameters.getParameter(nodeStore.getOperandLeft(number));
    }

    public int getParameterNumber(int number) {
        assert nodeStore.assertValidNumber(number);
        return nodeStore.getOperandLeft(number);
    }

    public int getOperand(int number) {
        assert nodeStore.assertValidNumber(number);
        return nodeStore.getOperandLeft(number);
    }
    
    public int getOperandLeft(int number) {
        assert nodeStore.assertValidNumber(number);
        return nodeStore.getOperandLeft(number);
    }
    
    public int getOperandRight(int number) {
        assert nodeStore.assertValidNumber(number);
        return nodeStore.getOperandRight(number);
    }

    public ParameterSet getParameters() {
        return parameters;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        try {
            DagExporter.Builder builder = new ExporterGraphviz.Builder()
                    .setDag(this);
            int size = nodeStore.getNumNodes();
            for (int number = 0; number < size; number++) {
                builder.addRelevantNode(number);
            }            
            builder.build().export(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }
    
    public BigInteger getValueFromNumber(int number) {
        return numberStore.getNumber(number);
    }
    
    public int getNumberOfValue(BigInteger value) {
        return numberStore.getIndex(value);
    }
    
    // TODO
    public void sendStatistics() {
        getLog().send(MessagesParamDag.PARAM_DAG_NUM_NODES, maxNode + 1);
        nodeStore.sendStatistics();
        numberStore.sendStatistics();
        for (Simplifier simplifier : simplifiers) {
            simplifier.sendStatistics();
        }
    }
    
    private static Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }
}
