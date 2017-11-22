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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.error.Positional;

public final class PlayerDefinition {
    private final String name;
    private final Set<String> modules = new LinkedHashSet<>();
    private final Set<String> labels = new LinkedHashSet<>();
    private final Set<String> externalModules = Collections.unmodifiableSet(modules);
    private final Set<String> externalLabels = Collections.unmodifiableSet(labels);
    private final Positional positional;

    public PlayerDefinition(String name, Set<String> modules, Set<String> labels,
            Positional positional) {
        assert name != null;
        assert modules != null;
        for (String module : modules) {
            assert module != null;
        }
        assert labels != null;
        for (String label : labels) {
            assert label != null;
        }
        this.name = name;
        this.modules.addAll(modules);
        this.labels.addAll(labels);
        this.positional = positional;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("player " + name + "\n  ");
        int moduleNumber = 0;
        for (String module : modules) {
            builder.append(module);
            if (moduleNumber < modules.size() - 1 || labels.size() > 0) {
                builder.append(", ");
            }
            moduleNumber++;
        }
        int labelNumber = 0;
        for (String label : labels) {
            builder.append(label);
            if (labelNumber < labels.size() - 1) {
                builder.append(", ");
            }
            moduleNumber++;
        }
        builder.append("\nendplayer");
        return builder.toString();
    }

    public String getName() {
        return name;
    }

    public Set<String> getModules() {
        return externalModules;
    }

    public Set<String> getLabels() {
        return externalLabels;
    }

    public Positional getPositional() {
        return positional;
    }

}
