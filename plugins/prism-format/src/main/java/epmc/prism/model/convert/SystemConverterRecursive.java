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

package epmc.prism.model.convert;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.jani.model.Action;
import epmc.jani.model.Actions;
import epmc.jani.model.Automata;
import epmc.jani.model.Automaton;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;
import epmc.jani.model.component.ComponentParallel;
import epmc.jani.model.component.ComponentRename;
import epmc.prism.model.ModelPRISM;
import epmc.prism.model.SystemAlphaParallel;
import epmc.prism.model.SystemAsyncParallel;
import epmc.prism.model.SystemDefinition;
import epmc.prism.model.SystemHide;
import epmc.prism.model.SystemModule;
import epmc.prism.model.SystemRename;
import epmc.prism.model.SystemRestrictedParallel;

final class SystemConverterRecursive implements SystemConverter {
    /** Empty string. */
    final static String EMPTY = "";

    private ModelPRISM modelPRISM;
    private ModelJANI modelJANI;
    /** Identifier for the silent action. */
    private Expression silentActionIdentifier;

    @Override
    public void setPRISMModel(ModelPRISM modelPrism) {
        this.modelPRISM = modelPrism;
    }

    @Override
    public void setJANIModel(ModelJANI modelJani) {
        this.modelJANI = modelJani;
    }

    @Override
    public void convert() {
        this.silentActionIdentifier = new ExpressionIdentifierStandard.Builder()
                .setName(EMPTY)
                .build();
        Automata automata = modelJANI.getAutomata();
        Actions actions = modelJANI.getActions();
        Component system = convertSystem(modelPRISM.getSystem(), automata, actions);
        modelJANI.setSystem(system);
    }

    private Component convertSystem(SystemDefinition system,
            Map<String,Automaton> automata,
            Actions actions) {
        assert system != null;
        assert automata != null;
        if (system.isModule()) {
            return convertSystemModule(system.asModule(), automata, actions);
        } else if (system.isAlphaParallel()) {
            return convertSystemAlphaParallel(system.asAlphaParallel(), automata, actions);
        } else if (system.isAsyncParallel()) {
            return convertSystemAsyncParallel(system.asAsyncParallel(), automata, actions);
        } else if (system.isHide()) {
            return convertSystemHide(system.asHide(), automata, actions);
        } else if (system.isRename()) {
            return convertSystemRename(system.asRename(), automata, actions);
        } else if (system.isRestrictedParallel()) {
            return convertSystemRestrictedParallel(system.asRestrictedParallel(), automata, actions);
        } else {
            assert false; // TODO
            return null;
        }
    }

    private Component convertSystemModule(SystemModule systemModule, Map<String, Automaton> automata, Actions actions) {
        String instance = systemModule.getModule();
        Automaton automaton = automata.get(instance);
        ComponentAutomaton result = new ComponentAutomaton();
        result.setAutomaton(automaton);
        return result;
    }

    private Component convertSystemAlphaParallel(SystemAlphaParallel systemAlphaParallel, Map<String, Automaton> automata, Actions actions) {
        SystemDefinition left = systemAlphaParallel.getLeft();
        SystemDefinition right = systemAlphaParallel.getRight();
        Component leftComponent = convertSystem(left, automata, actions);
        Component rightComponent = convertSystem(right, automata, actions);
        ComponentParallel result = new ComponentParallel();
        result.setLeft(leftComponent);
        result.setRight(rightComponent);
        Set<Expression> mid = new HashSet<>();
        mid.addAll(left.getAlphabet());
        mid.retainAll(right.getAlphabet());
        mid.remove(silentActionIdentifier);
        result.addActions(mapActions(mid, actions));
        return result;
    }

    private Component convertSystemAsyncParallel(SystemAsyncParallel systemAsyncParallel, Map<String, Automaton> automata,
            Actions actions) {
        SystemDefinition left = systemAsyncParallel.getLeft();
        SystemDefinition right = systemAsyncParallel.getRight();
        Component leftComponent = convertSystem(left, automata, actions);
        Component rightComponent = convertSystem(right, automata, actions);
        ComponentParallel result = new ComponentParallel();
        result.setLeft(leftComponent);
        result.setRight(rightComponent);
        return result;
    }

    private Component convertSystemHide(SystemHide systemHide, Map<String, Automaton> automata, Actions actions) {
        ComponentRename result = new ComponentRename();
        SystemDefinition inner = systemHide.getInner();
        Component innerComponent = convertSystem(inner, automata, actions);
        result.setRenamed(innerComponent);
        result.addRenamings(hideActions(systemHide.getHidden(), actions));
        return result;
    }

    private Component convertSystemRename(SystemRename systemRename, Map<String, Automaton> automata,
            Actions actions) {
        SystemDefinition inner = systemRename.getInner();
        Component innerComponent = convertSystem(inner, automata, actions);
        ComponentRename result = new ComponentRename();
        result.setRenamed(innerComponent);
        result.addRenamings(renameActions(systemRename.getRenaming(), actions));
        return result;
    }

    private Component convertSystemRestrictedParallel(SystemRestrictedParallel systemRestrictedParallel,
            Map<String, Automaton> automata, Actions actions) {
        SystemDefinition left = systemRestrictedParallel.getLeft();
        SystemDefinition right = systemRestrictedParallel.getRight();
        Component leftComponent = convertSystem(left, automata, actions);
        Component rightComponent = convertSystem(right, automata, actions);
        ComponentParallel result = new ComponentParallel();
        result.addActions(mapActions(systemRestrictedParallel.getSync(), actions));
        result.setLeft(leftComponent);
        result.setRight(rightComponent);
        return result;
    }

    private Map<Action, Action> renameActions(Map<Expression, Expression> renaming, Actions actions) {
        Map<Action,Action> result = new LinkedHashMap<>();
        for (Entry<Expression,Expression> entry : renaming.entrySet()) {
            result.put(actions.get(entry.getKey()), actions.get(entry.getValue()));
        }
        return result;
    }

    private Map<Action, Action> hideActions(Set<Expression> hidden, Actions actions) {
        Map<Action,Action> result = new LinkedHashMap<>();
        for (Expression expression : hidden) {
            Action from = actions.get(expression);
            result.put(from, getSilentAction());
        }
        return result;
    }

    private Collection<Action> mapActions(Collection<Expression> expressions, Actions allActions) {
        assert expressions != null;
        for (Expression expression : expressions) {
            assert expression != null;
        }
        Set<Action> result = new LinkedHashSet<>();
        for (Expression expression : expressions) {
            ExpressionIdentifierStandard expressionIdentifier = (ExpressionIdentifierStandard) expression;
            result.add(allActions.get(expressionIdentifier.getName()));
        }
        return result;
    }

    private Action getSilentAction() {
        return modelJANI.getSilentAction();
    }
}
