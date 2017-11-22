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

import com.google.common.base.MoreObjects;

/**
 * Class to store unparsed properties.
 * This class stores
 * <ul>
 * <li>the definition of the property as a string,<li>
 * <li>the name of the property,</li>
 * <li>comments about the property,</li>
 * <li>the type (e.g. PRISM, MRMC, etc.) of the property,</li>
 * <li>the chosen editor type of the property.</li>
 * </ul>
 * The property definition should be parsable by the class referred to in its
 * type. Comments are not interpreted by EPMC and are thus free-form. The
 * editor is a visual component for a given property type to allow the user
 * to change the property. All these parts may be {@code null}. If the
 * definition is {@code null}, no analysis will be performed for the property.
 * 
 * @author Ernst Moritz Hahn
 */
public final class RawProperty implements Serializable, Cloneable {
    /** Name of definition property (for usage as JavaBean and for {@link #toString()}). */
    public static String DEFINITION = "definition";
    /** Name of name property (for usage as JavaBean). */
    public static String NAME = "name";
    /** Name of description property (for usage as JavaBean and for {@link #toString()}) .*/
    public static String DESCRIPTION = "description";
    /** Name of type property (for usage as JavaBean and for {@link #toString()}). */
    public static String TYPE = "type";
    /** Name of editor property (for usage as JavaBean and for {@link #toString()}). */
    public static String EDITOR = "editor";
    /** Serial version number  - 1L so far, as I dont know any better. */
    private static final long serialVersionUID = 1L;
    /** property definition */
    private String definition;
    /** name of the property */
    private String name;
    /** description of the property */
    private String description;
    /** type of the property */
    private String type;
    /** editor chosen to change the property */
    private String editor;

    /**
     * Set property definition.
     * 
     * @param definition property definition
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /**
     * Obtain property definition
     * 
     * @return property definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Set the name of the property
     * 
     * @param name name of the property
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtain the name of the property
     * 
     * @return name of the property
     */
    public String getName() {
        return name;
    }

    /**
     * Set description of the property
     * 
     * @param description description of the property
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Obtain description of the property
     * 
     * @return description of the property
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the type of the property
     * 
     * @param type type of the property
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Obtain type of the property
     * 
     * @return type of the property
     */
    public String getType() {
        return type;
    }

    /**
     * Set the editor to change the property
     * 
     * @param editor editor to change the property
     */
    public void setEditor(String editor) {
        this.editor = editor;
    }

    /**
     * Obtain the editor to change the property
     * 
     * @return editor to change the property
     */
    public String getEditor() {
        return editor;
    }

    /**
     * Obtain a string representation of the property
     * 
     * @return string representation of the property
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add(DEFINITION, definition)
                .add(NAME, name)
                .add(DESCRIPTION, description)
                .add(TYPE, type)
                .add(EDITOR, editor)
                .toString();
    }

    @Override
    public RawProperty clone() {
        RawProperty clone = new RawProperty();
        clone.setDefinition(definition);
        clone.setName(name);
        clone.setDescription(description);
        clone.setType(type);
        clone.setEditor(editor);
        return clone;
    }
}
