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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import epmc.expression.Expression;
import epmc.modelchecker.Property;
import epmc.modelchecker.RawProperties;
import epmc.modelchecker.RawProperty;
import epmc.prism.expression.PrismExpressionParser;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;

public final class PropertyPRISM implements Property {
    public final static String IDENTIFIER = "prism";
    private final static String SEMICOLON = ";";
    private final static char SEMICOLON_C = ';';
    /** Marker for single-line comment. */
    private final static String COMMENT_MARKER = "//";
    /** Empty string. */
    private final static String EMPTY = "";
    private final static String EQUALS = "=";
    private final static char EQUALS_C = '=';
    private final static String SPACE = " ";
    private final static String CONST = "const";
    private final static String LABEL = "label";
    private final static String INT = "int";
    private final static String DOUBLE = "double";
    private final static char QUOT_C = '"';
    private final static char COLON_C = ':';

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Expression parseExpression(Object part, InputStream stream) {
        assert stream != null;
        String string = null;
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
            string = buffer.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
        PrismExpressionParser parser = new PrismExpressionParser(stream);
        return parser.parseExpressionAsProperty(part, 1, 1, string);
    }

    @Override
    public void writeProperties(RawProperties properties, OutputStream stream) {
        assert properties != null;
        assert stream != null;
        assert stream != null;
        PrintStream out = new PrintStream(stream);
        for (Entry<String, String> entry : properties.getConstants().entrySet()) {
            String name = entry.getKey();
            String definition = entry.getValue();
            out.print(CONST + SPACE + name);
            if (name != null && !definition.equals(EMPTY)) {
                out.print(SPACE + EQUALS + SPACE + entry.getValue());
            }
            out.println(SEMICOLON);
        }
        if (properties.getConstants().size() > 0) {
            out.println();
        }
        for (Entry<String, String> entry : properties.getLabels().entrySet()) {
            out.print(LABEL + SPACE + entry.getKey() + SPACE + EQUALS + SPACE);
            out.println(entry.getValue() + SEMICOLON);
        }
        if (properties.getLabels().size() > 0) {
            out.println();
        }
        for (RawProperty property : properties) {
            out.println(property);
        }
    }

    @Override
    public void readProperties(Object identifier, RawProperties properties, InputStream stream) {
        assert stream != null;
        properties.clear();
        final StringBuilder comment = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream));) {
            comment.setLength(0);
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                line = preprocessLine(line, comment);
                if (isCommentLine(line)) {
                    line = line.substring(COMMENT_MARKER.length()).trim();
                    comment.append(line);
                } else if (isConstLine(line)) {
                    addConstant(properties, line);
                    comment.setLength(0);
                } else if (isLabelLine(line)) {
                    addLabel(properties, line);
                    comment.setLength(0);
                } else if (!line.equals(EMPTY)) {
                    addProperty(properties, line, comment);
                    comment.setLength(0);
                } else {
                    comment.setLength(0);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addProperty(RawProperties properties, String line, StringBuilder comment) {
        assert line != null;
        RawProperty property = new RawProperty();
        String name = null;
        if (line.charAt(0) == QUOT_C) {
            int nameEnd = line.indexOf(QUOT_C, 1);
            name = line.substring(1, nameEnd);
            int colonPlace = line.indexOf(COLON_C, nameEnd);
            line = line.substring(colonPlace + 1);
            line = line.trim();
        }
        property.setName(name);
        property.setDefinition(line);
        property.setDescription(comment.toString());
        properties.addProperty(property);
    }

    private void addLabel(RawProperties properties, String line) {
        line = line.substring(LABEL.length());
        int index = line.indexOf(EQUALS_C); // first = 
        String name  = line.substring(0, index);
        String expr = line.substring(index+1);
        properties.addLabel(name.trim(), expr.trim());
    }

    private void addConstant(RawProperties properties, String line) {
        assert properties != null;
        assert line != null;
        assert line.length() >= 5;
        line = line.substring(CONST.length()).trim();
        String type = null;
        if (line.startsWith(INT)) {
            line = line.substring(INT.length());
            type = INT;
        } else if (line.startsWith(DOUBLE)) {
            line = line.substring(DOUBLE.length());
            type = DOUBLE;
        } else {
            type = DOUBLE;
        }
        String[] lineArr = line.split(EQUALS);
        if (lineArr.length == 2) {
            properties.addConstant(lineArr[0].trim(), type, lineArr[1].trim());
        } else {
            properties.addConstant(lineArr[0].trim(), type, null);
        }
    }

    private boolean isLabelLine(String line) {
        return line.startsWith(LABEL);
    }

    private boolean isConstLine(String line) {
        return line.startsWith(CONST);
    }

    private boolean isCommentLine(String line) {
        return line.startsWith(COMMENT_MARKER);
    }

    /**
     * Removes white space and potential end line semicolon from line and extends current comment.
     * None of the parameters may be {@code null}.
     * 
     * @param line original line to modify
     * @param comment comment to append to
     * @return modified line
     */
    private String preprocessLine(String line, StringBuilder comment) {
        assert line != null;
        assert comment != null;
        line = line.trim();
        if (!isCommentLine(line) && line.contains(COMMENT_MARKER)) {
            int commentStart = line.indexOf(COMMENT_MARKER);
            String commentLine = line.substring(commentStart + COMMENT_MARKER.length());
            commentLine = commentLine.trim();
            comment.append(commentLine);
            line = line.substring(0, commentStart);
        }
        line = line.trim();
        if (line.length() > 0 && line.charAt(line.length() - 1) == SEMICOLON_C) {
            line = line.substring(0, line.length() - 1);
            line = line.trim();
        }
        return line;
    }

    @Override
    public Type parseType(Object identifier, String type) {
        assert type != null;
        switch (type) {
        case INT:
            return TypeInteger.get();
        case DOUBLE:
            return TypeReal.get();
        }
        assert false;
        return null;
    }
}
