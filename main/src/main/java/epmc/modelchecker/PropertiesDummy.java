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

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.expression.Expression;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.value.Type;

// TODO documentation

// TODO check whether works properly - interfaces might change

/**
 * Properties for {@link ModelDummy}.
 * 
 * @author Ernst Moritz Hahn
 */
public final class PropertiesDummy implements Properties {
    /** Set of properties stored. */
    private final Map<RawProperty,Expression> properties = new LinkedHashMap<>();
    /** Constants stored in these options. */
    private final Map<String,Expression> constants = new LinkedHashMap<>();
    /** Types of constants. */
    private final Map<String,Type> constantTypes = new LinkedHashMap<>();
    /** Labels stored. */
    private final Map<String,Expression> labels = new LinkedHashMap<>();

    @Override
    public void parseProperties(Object object, InputStream... inputs) {
        assert inputs != null;
        for (InputStream input : inputs) {
            assert input != null;
        }
        for (InputStream input : inputs) {
            parseProperties(object, input);
        }
    }

    /**
     * Parse properties from a single input stream.
     * The input stream parameter must not be {@code null}.
     * 
     * @param input input string to parse from
     */
    private void parseProperties(Object identifier, InputStream input) {
        assert input != null;
        Property property = UtilOptions.getInstance(OptionsModelChecker.PROPERTY_INPUT_TYPE);
        RawProperties properties = new RawProperties();
        property.readProperties(identifier, properties, input);
        parseProperties(identifier, properties);
    }

    /**
     * Parse raw properties.
     * The raw properties parameter must not be {@code null}.
     * 
     * @param rawProperties raw properties to parse
     */
    private void parseProperties(Object identifier, RawProperties rawProperties) {
        assert rawProperties != null;
        Options options = Options.get();
        Map<String,Object> optionsConsts = options.getMap(OptionsModelChecker.CONST);
        if (optionsConsts == null) {
            optionsConsts = new LinkedHashMap<>();
        }
        for (RawProperty prop : rawProperties.getProperties()) {
            String definition = prop.getDefinition();
            if (definition == null) {
                continue;
            }
            Expression parsed = UtilModelChecker.parseExpression(identifier, definition);
            properties.put(prop, parsed);
        }
        for (Entry<String,String> entry : rawProperties.getConstants().entrySet()) {
            String name = entry.getKey();
            Object definition = entry.getValue();
            if (definition == null) {
                definition = optionsConsts.get(name);
            }
            Expression expr = null;
            if (definition != null && definition instanceof String) {
                expr = UtilModelChecker.parseExpression(identifier, ((String) definition));
            } else if (definition != null && definition instanceof Expression) {
                expr = (Expression) definition;
            } else if (definition != null) {
                assert false : definition;
            }
            constants.put(name, expr);
            Type type = UtilModelChecker.parseType(identifier, rawProperties.getConstantType(name));
            assert type != null;
            constantTypes.put(name, type);
        }
        for (Entry<String,String> entry : rawProperties.getLabels().entrySet()) {
            String name = entry.getKey();
            String definition = entry.getValue();
            Expression expr = null;
            if (definition != null) {
                expr = UtilModelChecker.parseExpression(identifier, definition);
            }
            labels.put(name, expr);
        }
    }

    @Override
    public List<RawProperty> getRawProperties() {
        return Collections.list(Collections.enumeration(properties.keySet()));
    }

    @Override
    public Expression getParsedProperty(RawProperty property) {
        return properties.get(property);
    }
}
