/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.automaton;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.operator.OperatorEq;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnumerable;
import epmc.value.Value;
import epmc.value.ValueEnumerable;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class AutomatonExporterDot implements AutomatonExporter {
    public static final String IDENTIFIER = "dot-general";
    private Automaton automaton;
    private OutputStream outStream;
    private AutomatonExporterFormat format;
    private Value[][] validInputs;

    @Override
    public AutomatonExporterDot setAutomaton(Automaton automaton) {
        this.automaton = automaton;
        return this;
    }

    @Override
    public AutomatonExporterDot setOutput(OutputStream out) {
        this.outStream = out;
        return this;
    }

    @Override
    public AutomatonExporterDot setFormat(AutomatonExporterFormat format) {
        this.format = format;
        return this;
    }

    @Override
    public void export() {
        assert automaton != null;
        assert outStream != null;
        assert format != null;
        this.validInputs = computeValidInputs(automaton);
        PrintStream out = new PrintStream(outStream);
        out.println("digraph {");
        Int2ObjectOpenHashMap<Object> states = exploreStates();
        for (Entry<Integer, Object> entry : states.entrySet()) {
            int key = entry.getKey();
            Object value = entry.getValue();
            out.println("  s" + key + " [label=\"" + value + "\"];");
        }
        out.println();
        states.forEach((node,b) -> {
            for (Value[] input : validInputs) {
                automaton.queryState(input, node);
                int succ = automaton.getSuccessorState();
                Object label = automaton.numberToLabel(automaton.getSuccessorLabel());
                out.print("  s" + node + " -> s" + succ + " [label=\"");
                printInput(input, out);
                out.print(" : ");
                out.println(label + "\"];");
            }
        });
        out.println("}");
    }

    private void printInput(Value[] input, PrintStream out) {
        out.print("(");
        for (int i = 0; i < input.length; i++) {
            out.print(input[i]);
            if (i < input.length - 1) {
                out.print(",");
            }
        }
        out.print(")");
    }

    private Value[][] computeValidInputs(Automaton automaton)
    {
        assert automaton != null;
        ContextDD contextDD = ContextDD.get();
        Expression[] expressions = automaton.getExpressions();
        Set<Expression> identifiers = new HashSet<>();
        for (Expression expression : expressions) {
            identifiers.addAll(UtilExpressionStandard.collectIdentifiers(expression));
        }
        Map<Expression,VariableDD> variables = new HashMap<>();
        for (Expression identifier : identifiers) {
            variables.put(identifier, contextDD.newVariable(identifier.toString(), TypeBoolean.get(), 1));
        }
        ExpressionToDD checkE2D = new ExpressionToDD(variables);

        List<Value[]> values = new ArrayList<>();
        int maxNumValues = (int) Math.pow(TypeBoolean.get().getNumValues(),
                expressions.length);
        for (int entryNr = 0; entryNr < maxNumValues; entryNr++) {
            int usedNr = entryNr;
            DD check = contextDD.newConstant(true);
            boolean invalid = false;
            Value[] entry = new Value[expressions.length];
            for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
                Expression expression = expressions[exprNr];
                TypeEnumerable type = TypeBoolean.get();
                int numValues = type.getNumValues();
                int valueNr = usedNr % numValues;
                usedNr /= numValues;
                ValueEnumerable value = type.newValue();
                value.setValueNumber(valueNr);
                entry[exprNr] = value;
                DD expressionDD = checkE2D.translate(expression);
                DD eq = ContextDD.get().applyWith(OperatorEq.EQ,
                        expressionDD,
                        ContextDD.get().newConstant(value));
                check = check.andWith(eq);
                if (check.isFalse()) {
                    invalid = true;
                    break;
                }
            }
            check.dispose();
            if (!invalid) {
                values.add(entry);
            }
        }
        checkE2D.close();
        Value[][] result = new Value[values.size()][];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private Int2ObjectOpenHashMap<Object> exploreStates() {
        IntArrayList todo = new IntArrayList();
        assert automaton.getInitState() == 0;
        todo.push(0);
        BitSet exploredNodes = UtilBitSet.newBitSetUnbounded();
        exploredNodes.set(0);
        Int2ObjectOpenHashMap<Object> result = new Int2ObjectOpenHashMap<>();
        while (todo.size() > 0) {
            int node = todo.popInt();
            result.put(node, automaton.numberToState(node));
            for (Value[] input : validInputs) {
                automaton.queryState(input, node);
                int succ = automaton.getSuccessorState();
                if (!exploredNodes.get(succ)) {
                    exploredNodes.set(succ);
                    todo.push(succ);
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return exportToString();
    }

    @Override
    public boolean canHandle() {
        if (format != AutomatonExporterFormatDOT.DOT) {
            return false;
        }
        return true;
    }
}
