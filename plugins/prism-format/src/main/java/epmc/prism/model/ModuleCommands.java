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

package epmc.prism.model;

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.jani.model.type.JANIType;
import epmc.prism.error.ProblemsPRISM;

/**
 * Represents a single guarded commands module of a model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModuleCommands implements Module {
    private final String name;
    private final Map<Expression,JANIType> variables;
    private final Map<Expression,JANIType> publicVariables;
    private final Map<Expression,Expression> initValues;
    private final ArrayList<Command> commands = new ArrayList<>();
    private final Positional positional;
    private Expression invariants;

    /**
     * Constructs a new guarded commands module.
     * None of the parameters may be null. If no name is needed, set it to the
     * empty string. If collections are not needed, use empty collections.
     * 
     * @param name name of the module
     * @param variables variables of the module and their types
     * @param initValues initial values of module variables
     * @param commands list of commands of the module
     * @param positional position where module was defined
     */
    public ModuleCommands(String name,
            Map<Expression,JANIType> variables,
            Map<Expression,Expression> initValues,
            List<Command> commands,
            Expression invariants,
            Positional positional) {
        assert name != null;
        assert variables != null;
        this.positional = positional;
        if (invariants == null) {
            invariants = ExpressionLiteral.getTrue();
        }
        this.invariants = invariants;
        for (Entry<Expression, JANIType> entry : variables.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        assert initValues != null;
        for (Entry<Expression, Expression> entry : initValues.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        assert commands != null;
        for (Command command : commands) {
            assert command != null;
        }
        for (Entry<Expression,JANIType> entry : variables.entrySet()) {
            assert entry.getKey() instanceof ExpressionIdentifier;
        }
        for (Entry<Expression,Expression> entry : initValues.entrySet()) {
            assert entry.getKey() instanceof ExpressionIdentifier;
        }
        this.name = name;
        this.variables = new HashMap<>();
        this.publicVariables = Collections.unmodifiableMap(this.variables);
        this.initValues = new HashMap<>();
        this.variables.putAll(variables);
        this.commands.addAll(commands);
        this.initValues.putAll(initValues);
    }

    /**
     * Constructs a new module with a new name in which all parts (variables,
     * synchronisation labels, etc.) are renamed according to <code>map</code>.
     * 
     * @param name name of new module
     * @param map map of old parts to replacements
     * @return new module
     */
    ModuleCommands rename(String name, Map<Expression, Expression> map)
    {
        assert name != null;
        assert map != null;
        for (Entry<Expression, Expression> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        Map<Expression,JANIType> newVariables = new HashMap<>();
        Map<Expression,Expression> newInitValues = new HashMap<>();

        List<Command> newCommands = new ArrayList<>();

        for (Entry<Expression,JANIType> entry : this.variables.entrySet()) {
            ensure(map.containsKey(entry.getKey()), ProblemsPRISM.VAR_NOT_RENAMED,
                    entry.getKey(), name);
            Expression varReplace = map.get(entry.getKey());
            newVariables.put(varReplace, entry.getValue().replace(map));
        }
        for (Entry<Expression,Expression> entry : this.initValues.entrySet()) {
            Expression varReplace = map.get(entry.getKey());
            newInitValues.put(varReplace, UtilExpressionStandard.replace(entry.getValue(), map));
        }

        for (Command origCommand : this.commands) {
            Expression newLabel = UtilExpressionStandard.replace(origCommand.getLabel(), map);
            Expression newGuard = UtilExpressionStandard.replace(origCommand.getGuard(), map);
            ArrayList<Alternative> newAlternatives = new ArrayList<>();
            for (Alternative origAlternative : origCommand.getAlternatives()) {
                Expression newWeight = UtilExpressionStandard.replace(origAlternative.getWeight(), map);
                Map<Expression,Expression> newEffects = new HashMap<>();
                for (Entry<Expression,Expression> entry : origAlternative.getEffect().entrySet()) {
                    newEffects.put(UtilExpressionStandard.replace(entry.getKey(), map),
                            UtilExpressionStandard.replace(entry.getValue(), map));
                }
                Alternative newAlternative = new Alternative(newWeight, newEffects, null);
                newAlternatives.add(newAlternative);
            }
            Command newCommand = new Command(newLabel, newGuard, newAlternatives, null);
            newCommands.add(newCommand);
        }
        Expression newInvariants = UtilExpressionStandard.replace(invariants, map);

        return new ModuleCommands(name, newVariables, newInitValues, newCommands, newInvariants, null);
    }

    ModuleCommands renameActions(Map<Expression,Expression> map) {
        assert map != null;
        ArrayList<Command> newCommands = new ArrayList<>();

        for (Entry<Expression,JANIType> entry : this.variables.entrySet()) {
            Expression varReplace = map.get(entry.getKey());
            this.variables.put(varReplace, entry.getValue());
            // TODO throw exception if varReplace == null
        }
        for (Command origCommand : this.commands) {
            Expression newLabel = UtilExpressionStandard.replace(origCommand.getLabel(), map);
            Expression newGuard = UtilExpressionStandard.replace(origCommand.getGuard(), map);
            ArrayList<Alternative> newAlternatives = new ArrayList<>();
            for (Alternative origAlternative : origCommand.getAlternatives()) {
                Expression newWeight = UtilExpressionStandard.replace(origAlternative.getWeight(), map);
                Map<Expression,Expression> newEffects = new HashMap<>();
                for (Entry<Expression,Expression> entry : origAlternative.getEffect().entrySet()) {
                    newEffects.put(UtilExpressionStandard.replace(entry.getKey(), map),
                            UtilExpressionStandard.replace(entry.getValue(), map));
                }
                Alternative newAlternative = new Alternative(newWeight, newEffects, null);
                newAlternatives.add(newAlternative);
            }
            Command newCommand = new Command(newLabel, newGuard, newAlternatives, null);
            newCommands.add(newCommand);
        }
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(name + "{");
        int labelNr = 0;
        for (Entry<Expression,Expression> entry : map.entrySet()) {
            nameBuilder.append(entry.getKey() + "_REN_" + entry.getValue());
            if (labelNr < map.size() - 1) {
                nameBuilder.append("_");
            }
            labelNr++;
        }
        nameBuilder.append("_REN_");

        return new ModuleCommands(nameBuilder.toString(), this.variables, this.initValues, newCommands, invariants, null);
    }

    ModuleCommands hideActions(Set<Expression> labels) {
        assert labels != null;
        for (Expression label : labels) {
            assert label != null;
        }
        Expression tau = new ExpressionIdentifierStandard.Builder()
                .setName("")
                .build();
        Map<Expression,Expression> rename = new HashMap<>();
        for (Expression label : labels) {
            rename.put(label, tau);
        }
        return renameActions(rename);
    }

    /**
     * Returns the alphabet of the module, that is all synchronisation labels.
     * 
     * @return all sychronisation labels of any command
     */
    @Override
    public Set<Expression> getAlphabet() {
        Set<Expression> result = new HashSet<>();
        for (Command command : commands) {
            result.add(command.getLabel());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("module " + name + "\n");
        for (Entry<Expression,JANIType> entry : variables.entrySet()) {
            builder.append("  " + entry.getKey() + " : ");
            builder.append(entry.getValue());
            if (initValues.containsKey(entry.getKey())) {
                builder.append(" init " + initValues.get(entry.getKey()));
            }
            builder.append(";\n");
        }
        builder.append("\n");
        for (Command command : commands) {
            builder.append("  " + command + ";\n");
        }
        builder.append("endmodule\n");
        return builder.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    @Override
    public Map<Expression,JANIType> getVariables() {
        return publicVariables;
    }

    @Override
    public Map<Expression, Expression> getInitValues() {
        return Collections.unmodifiableMap(initValues);
    }

    @Override
    public ModuleCommands replaceFormulas(Map<Expression,Expression> map) {
        assert map != null;
        for (Entry<Expression, Expression> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        ArrayList<Command> newCommands = new ArrayList<>(); 
        for (Command command : commands) {
            newCommands.add(command.replaceFormulas(map));
        }
        Map<Expression,Expression> newInitValues = new HashMap<>();
        for (Entry<Expression,Expression> entry : this.initValues.entrySet()) {
            newInitValues.put(entry.getKey(), UtilExpressionStandard.replace(entry.getValue(), map));
        }
        Map<Expression,JANIType> newVariables = new HashMap<>();
        for (Entry<Expression,JANIType> entry : variables.entrySet()) {
            newVariables.put(entry.getKey(), entry.getValue().replace(map));
        }
        Expression newInvariants = UtilExpressionStandard.replace(invariants, map);
        return new ModuleCommands(this.name, newVariables, newInitValues, newCommands, newInvariants, getPositional());
    }

    ModuleCommands replaceVariables(Map<Expression,JANIType> variables) {
        assert variables != null;
        return new ModuleCommands(name, variables, initValues, commands, invariants, positional);
    }

    @Override
    public Positional getPositional() {
        return positional;
    }

    public Expression getInvariants() {
        return invariants;
    }
}
