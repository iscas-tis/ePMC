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

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.exporter.expressionprocessor.ExpressionProcessorRegistrar;
import epmc.jani.exporter.processor.JANIProcessor;

public class JANIExporter_AssignmentSimpleProcessor implements JANIProcessor {
    /** String specifying to which variable to assign to. */
    private final static String REF = "ref";
    /** String specifying expression of value to be assigned. */
    private final static String VALUE = "value";
    /** String identifying index of this assignment. */
    private final static String INDEX = "index";
    /** String specifying comment for this assignment. */
    private final static String COMMENT = "comment";

    private AssignmentSimple assignment = null;

    @Override
    public JANIProcessor setElement(Object component) {
        assert component != null;
        assert component instanceof AssignmentSimple; 

        assignment = (AssignmentSimple) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert assignment != null;

        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add(REF, assignment.getRef().getName());

        builder.add(VALUE, ExpressionProcessorRegistrar.getExpressionProcessor(assignment.getValue())
                .toJSON());

        Integer index = assignment.getIndex();
        if (index != null)
            builder.add(INDEX, index);
        
        String comment = assignment.getComment();
        if (comment != null)
            builder.add(COMMENT, comment);

        return builder.build();
    }
}
