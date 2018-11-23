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

package epmc.command;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.automaton.Automaton;
import epmc.automaton.AutomatonExporter;
import epmc.automaton.AutomatonExporterDot;
import epmc.automaton.AutomatonExporterFormat;
import epmc.automaton.AutomatonExporterFormatDOT;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionPropositional;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.Log;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.ModelCheckerResult;
import epmc.modelchecker.Properties;
import epmc.modelchecker.RawProperty;
import epmc.options.Options;
import epmc.options.UtilOptions;

public class CommandTaskExpression2Automaton implements CommandTask {
    public final static String IDENTIFIER = "expression2automaton";
    private ModelChecker modelChecker;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        this.modelChecker = modelChecker;
    }

    @Override
    public void executeInServer() {
        Model model = modelChecker.getModel();
        Properties properties = model.getPropertyList();
        Log log = getLog();
        for (RawProperty property : properties.getRawProperties()) {
            Expression expression = properties.getParsedProperty(property);
            if (ExpressionQuantifier.is(expression)) {
                ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
                expression = expressionQuantifier.getQuantified();
            }

            Set<Expression> identifiers = UtilExpressionStandard.collectIdentifiers(expression);
            boolean fail = false;
            for (Expression rel : identifiers) {
                if (ExpressionIdentifier.is(rel)) {
                    /*
                    try {
						if (rel.getType() == null) {
						    ContextValue.get().registerType(rel, TypeBoolean.get(contextValue));
						}
					} catch (EPMCException e) {
						log.send(new ModelCheckerResult(property, e));
						fail = true;
					}
                     */
                }
            }
            if (fail) {
                continue;
            }
            Set<Expression> relevantExpressions = collectLTLInner(expression);
            fail = false;
            for (Expression rel : relevantExpressions) {
                /*
                try {
					if (rel.getType() == null) {
					    ContextValue.get().registerType(rel, TypeBoolean.get(contextValue));
					}
				} catch (EPMCException e) {
					log.send(new ModelCheckerResult(property, e));
					fail = true;
				}
                 */
            }
            if (fail) {
                continue;
            }
            Automaton automaton = null;
            try {
                Automaton.Builder builder;
                builder = UtilOptions.getInstance(OptionsCommandExpression2Automaton.AUTOMATON_EXPRESSION2TYPE);
                builder.setExpression(expression);
                automaton = builder.build();
            } catch (EPMCException e) {
                log.send(new ModelCheckerResult(property,  e));
                continue;
            }
            AutomatonExporter exporter = new AutomatonExporterDot();
            exporter.setAutomaton(automaton);
            exporter.setFormat(AutomatonExporterFormatDOT.DOT);
            ModelCheckerResult result = new ModelCheckerResult(property, exporter.exportToString());
            log.send(result);
        }
    }

    private static Set<Expression> collectLTLInner(Expression expression) {
        assert expression != null;
        if (ExpressionPropositional.is(expression)) {
            return Collections.singleton(expression);
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalGlobally.is(expression)) {
            ExpressionTemporalGlobally expressionTemporal = ExpressionTemporalGlobally.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalRelease.is(expression)) {
            ExpressionTemporalRelease expressionTemporal = ExpressionTemporalRelease.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectLTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectLTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionOperator.is(expression)) {
            ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression inner : expressionOperator.getOperands()) {
                result.addAll(collectLTLInner(inner));
            }
            return result;
        } else {
            return Collections.singleton(expression);			
        }
    }

    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }
}
