package epmc.param.value.dag.microcode;

import java.math.BigInteger;

import epmc.param.value.ParameterSet;
import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.UtilDag;

public final class Microcode {
    private final Dag dag;
    private final int[] depending;
    private final VariableAssignment variableAssignment;
    private final int[] start;

    public Microcode(TypeDag typeDag, int[] start) {
        this(getDag(typeDag), start);
    }
    
    private static Dag getDag(TypeDag typeDag) {
        assert typeDag != null;
        return typeDag.getDag();
    }
    
    public Microcode(Dag dag, int[] start) {
        assert dag != null;
        assert start != null;
        this.dag = dag;
        this.start = start;
        depending = UtilDag.findDepending(dag, start);
        variableAssignment = UtilMicrocode.assignVariables(dag, depending, start);
    }
    
    public int getNumStatements() {
        return depending.length;
    }
    
    public int getNumVariables() {
        return variableAssignment.getNumVariables();
    }
    
    public OperatorType getOperator(int statement) {
        assert statement >= 0 : statement;
        assert statement < depending.length : statement;
        int node = depending[statement];
        return dag.getOperatorType(node);
    }
    
    public BigInteger getNumberNumerator(int statement) {
        assert statement >= 0 : statement;
        assert statement < depending.length : statement;
        int node = depending[statement];
        return dag.getNumberNumerator(node);
    }
    
    public BigInteger getNumberDenominator(int statement) {
        assert statement >= 0 : statement;
        assert statement < depending.length : statement;
        int node = depending[statement];
        return dag.getNumberDenominator(node);
    }

    public ParameterSet getParameters() {
        return dag.getParameters();
    }
    
    public int getParameter(int statement) {
        assert statement >= 0 : statement;
        assert statement < depending.length : statement;
        int node = depending[statement];
        return dag.getParameterNumber(node);
    }
    
    public int getAssignedTo(int statement) {
        assert statement >= 0 : statement;
        assert statement < depending.length : statement;
        int node = depending[statement];
        return variableAssignment.getAssignedTo(node);
    }
    
    public int getOperand(int statement) {
        assert statement >= 0 : statement;
        assert statement < depending.length : statement;
        int node = depending[statement];
        int operandNode = dag.getOperand(node);
        return variableAssignment.getAssignedTo(operandNode);
    }
    
    public int getOperandLeft(int statement) {
        assert statement >= 0 : statement;
        assert statement < depending.length : statement;
        int node = depending[statement];
        int operandNode = dag.getOperandLeft(node);
        return variableAssignment.getAssignedTo(operandNode);
    }

    public int getOperandRight(int statement) {
        assert statement >= 0 : statement;
        assert statement < depending.length : statement;
        int node = depending[statement];
        int operandNode = dag.getOperandRight(node);
        return variableAssignment.getAssignedTo(operandNode);
    }
    
    public int getNumResultVariables() {
        return start.length;
    }
    
    public int getResultVariable(int number) {
        assert number >= 0 : number;
        assert number < start.length : number;
        return variableAssignment.getAssignedTo(start[number]);
    }
}
