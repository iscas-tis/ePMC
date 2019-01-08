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

package epmc.jani.model;

import static epmc.error.UtilError.ensure;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.graph.SemanticsDTMC;
import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorMultiply;
import epmc.prism.exporter.JANIComponentRegistrar;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_EdgeProcessor implements PRISMExporter_ProcessorStrict {

	private static ExpressionOperator newOperator(Operator operator, Expression operand1, Expression operand2) {
        return new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(operand1, operand2)
                .build();
    }

	
    private Edge edge = null;
    private String prefix = null;
    private Automaton automaton = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof Edge; 

        edge = (Edge) obj;

    	Map<Variable, Expression> rewards = new HashMap<>();

        Action action = edge.getActionOrSilent();
        for (Destination destination: edge.getDestinations()) {
            Expression prob = destination.getProbabilityExpressionOrOne();
            for (AssignmentSimple assignment : destination.getAssignmentsOrEmpty()) {
                Variable reward = assignment.getRef();
                if (reward.isTransient()) {
	                Expression cumulativeExp = rewards.get(reward);
	                Expression weightedExp = newOperator(OperatorMultiply.MULTIPLY, prob, assignment.getValue());
	                if (cumulativeExp == null) {
	                	rewards.put(reward, weightedExp);
	                } else {
	                	rewards.put(reward, newOperator(OperatorAdd.ADD, cumulativeExp, weightedExp));
	                }
                }
            }
        }
        for (Entry<Variable, Expression> entry : rewards.entrySet()) {
            JANIComponentRegistrar.registerTransitionRewardExpression(entry.getKey(), action, entry.getValue());
        }
        return this;
    }

    @Override
    public PRISMExporter_ProcessorStrict setAutomaton(Automaton automaton) {
        this.automaton = automaton;
        return this;
    }

    @Override
    public PRISMExporter_ProcessorStrict setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String toPRISM() {
        assert edge != null;

        Rate edgeRate = edge.getRate();

        if (edgeRate != null && SemanticsDTMC.isDTMC(edge.getModel().getSemantics())) {
            Rate rateOne = new Rate();
            rateOne.setExp(ExpressionLiteral.getOne());
            ensure(rateOne.equals(edgeRate), 
                    ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_EDGE_RATE_NOT_ONE);
        }

        StringBuilder prism = new StringBuilder();

        String comment = edge.getComment();
        if (comment != null) {
            if (prefix != null) {
                prism.append(prefix);
            }
            prism.append("// ")
                .append(comment)
                .append("\n");
        }

        if (prefix != null)	{
            prism.append(prefix);
        }
        prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(edge.getActionOrSilent())
                .toPRISM())
            .append(" ");

        if (automaton.getLocations().size() > 1) {
            prism.append("(")
                .append(JANIComponentRegistrar.getLocationName(automaton))
                .append(" = ")
                .append(JANIComponentRegistrar.getLocationIdentifier(automaton, edge.getLocation()))
                .append(") & ");
        }
        Guard guard = edge.getGuard();
        if (guard == null) {
            prism.append("true");
        } else {
            prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(edge.getGuard())
                    .toPRISM());
        }
        prism.append(" -> ")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(edge.getDestinations())
                .setAutomaton(automaton)
                .toPRISM())
            .append(";\n");

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert edge != null;

        Guard guard = edge.getGuard();
        if (guard != null) {
            PRISMExporter_ProcessorRegistrar.getProcessor(guard)
                .validateTransientVariables();
        }
        PRISMExporter_ProcessorRegistrar.getProcessor(edge.getDestinations())
            .validateTransientVariables();
    }

    @Override
    public boolean usesTransientVariables() {
        assert edge != null;

        boolean usesTransient = false;
        Guard guard = edge.getGuard();
        if (guard != null) {
            usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(guard)
                    .usesTransientVariables();
        }
        usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(edge.getDestinations())
                .usesTransientVariables();

        return usesTransient;
    }	
}
