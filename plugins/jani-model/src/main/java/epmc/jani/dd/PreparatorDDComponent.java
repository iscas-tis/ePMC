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

package epmc.jani.dd;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;
import epmc.jani.model.component.ComponentParallel;
import epmc.jani.model.component.ComponentRename;

/**
 * Translate a JANI system component to a DD component.
 * For this, for each class implementing {@link Component}, the according class
 * implementing {@link DDComponent} has to be found and instantiated.
 * Afterwards, the instantiated DD component will build a symbolic
 * representation of the component.
 * 
 * @author Ernst Moritz Hahn
 */
final class PreparatorDDComponent {
    /** Map from component class to corresponding component explorer class. */
    private final static Map<Class<? extends Component>, Class<? extends DDComponent>> MAP;
    static {
        Map<Class<? extends Component>, Class<? extends DDComponent>> map = new LinkedHashMap<>();
        map.put(ComponentAutomaton.class, DDComponentAutomaton.class);
        map.put(ComponentParallel.class, DDComponentParallel.class);
        map.put(ComponentRename.class, DDComponentRename.class);
        MAP = Collections.unmodifiableMap(map);
    }

    /**
     * Prepare DD component.
     * None of the parameters may be {@code null}.
     * 
     * @param graph graph to which component to be converted belongs
     * @param component component for which to obtain an explorer
     * @return DD representation of given component
     */
    DDComponent prepare(GraphDDJANI graph, Component component) {
        assert graph != null;
        assert component != null;
        Class<? extends DDComponent> clazz = MAP.get(component.getClass());
        DDComponent instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            assert false;
            return null;
        }
        instance.setGraph(graph);
        instance.setComponent(component);
        instance.build();
        return instance;
    }
}
