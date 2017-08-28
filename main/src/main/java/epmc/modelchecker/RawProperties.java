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

package epmc.modelchecker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Collection of unparsed properties.
 * Objects of this class are serialisable, because they will be sent from the
 * client to the server as part of an analysis request.
 * 
 * @author Ernst Moritz Hahn
 */
public final class RawProperties implements Serializable,Iterable<RawProperty> {
    /** 1L, as I don't know any better. */
    private static final long serialVersionUID = 1L;
    /** String "Properties:\n". */
    private static String PROPERTIES = "Properties:\n";
    /** String containing end of line. */
    private static String END_LINE = "\n";
    /** String "Constants:\n". */
    private static String CONSTANTS = "Constants:\n";
    /** String " = ". */
    private static String EQUALS = " = ";
    /** String "Labels:\n". */
    private static String LABELS = "Labels:\n";
    /** list of properties */
    private final List<RawProperty> properties = new ArrayList<>();
    /** maps each constant to its definition (or to null) */
    private final Map<String,String> constants = new LinkedHashMap<>();
    /** maps each constant to its type */
    private final Map<String,String> constantTypes = new LinkedHashMap<>();
    /** maps each constant to its definition (or to null) - external version, read-only */
    private final Map<String,String> constantsExternal = Collections.unmodifiableMap(constants);
    /** maps each label name to its definition */
    private final Map<String,String> labels = new LinkedHashMap<>();
    /** maps each label name to its definition - for external usage, read only */
    private final Map<String,String> labelsExternal = Collections.unmodifiableMap(labels);

    /**
     * Delete all properties and other entities from this object.
     */
    public void clear() {
        properties.clear();
        constants.clear();
        labels.clear();
    }

    /**
     * Adds a raw property to this object.
     * @{code property} must not be {@code null}.
     * 
     * @param property property to be added
     * @return 
     */
    public boolean addProperty(RawProperty property) {
        assert property != null;
        return properties.add(property);
    }

    /**
     * Add a new constant.
     * {@code definition} may be {@code null} to indicate undefined constants
     * used in the property specification. {@code name} must not be
     * {@code null}.
     * 
     * @param name name of constant to be added
     * @param definition definition of constant to be added
     */
    public void addConstant(String name, String type, String definition) {
        assert name != null;
        assert type != null;
        constants.put(name, definition);
        constantTypes.put(name, type);
    }

    /**
     * Add a new label.
     * None of the parameter may be null.
     * 
     * @param name name of label to be added
     * @param definition definition of label to be added
     */
    public void addLabel(String name, String definition) {
        assert name != null;
        assert definition != null;
        labels.put(name, definition);
    }

    /**
     * Obtain list of properties.
     * 
     * @return list of properties
     */
    public List<RawProperty> getProperties() {
        return properties;
    }

    /**
     * Obtain map mapping constants to definition (or {@code null}).
     * Note that the map returned is read-only.
     * 
     * @return map of constants
     */
    public Map<String,String> getConstants() {
        return constantsExternal;
    }

    /**
     * Get type description of given constant.
     * The constant parameter must not be {@code null}.
     * 
     * @param constant constant to get type of
     * @return type of constant
     */
    public String getConstantType(String constant) {
        assert constant != null;
        return constantTypes.get(constant);
    }

    /**
     * Obtain map mapping labels to definitions.
     * Note that the map returned is read-only.
     * 
     * @return map of labels
     */
    public Map<String,String> getLabels() {
        return labelsExternal;
    }

    @Override
    public Iterator<RawProperty> iterator() {
        return properties.iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(PROPERTIES);
        for (RawProperty property : properties) {
            builder.append(property);
            builder.append(END_LINE);
        }
        builder.append(CONSTANTS);
        for (Entry<String, String> entry : constants.entrySet()) {
            builder.append(entry.getKey());
            builder.append(EQUALS);
            builder.append(entry.getValue());
            builder.append(END_LINE);
        }
        builder.append(LABELS);
        for (Entry<String, String> entry : labels.entrySet()) {
            builder.append(entry.getKey());
            builder.append(EQUALS);
            builder.append(entry.getValue());
            builder.append(END_LINE);
        }
        return builder.toString();
    }
}
