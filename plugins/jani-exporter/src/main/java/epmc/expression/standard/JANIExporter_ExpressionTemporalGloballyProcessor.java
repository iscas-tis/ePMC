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

package epmc.expression.standard;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;
import epmc.jani.model.UtilModelParser;

public class JANIExporter_ExpressionTemporalGloballyProcessor implements JANIProcessor {
    private final static String OP = "op";
    private final static String G = "G";
    private final static String EXP = "exp";
    private final static String W = "W";
    private final static String LEFT = "left";
    private final static String RIGHT = "right";
    private final static String STEP_BOUNDS = "step-bounds";
    private final static String TIME_BOUNDS = "time-bounds";

    private ExpressionTemporalGlobally expressionTemporalGlobally = null;

    @Override
    public JANIProcessor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionTemporalGlobally; 

        expressionTemporalGlobally = (ExpressionTemporalGlobally) obj;
        
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionTemporalGlobally != null;
        
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        if (ProcessorRegistrar.useDerivedOperators()) {
            builder.add(OP, G);
            builder.add(EXP, ProcessorRegistrar.getProcessor(expressionTemporalGlobally.getOperand())
                    .toJSON());
        } else {
            //G op = op W false
            builder.add(OP, W);
            builder.add(LEFT, ProcessorRegistrar.getProcessor(expressionTemporalGlobally.getOperand())
                    .toJSON());
            builder.add(RIGHT, false);
        }
        
        TimeBound timeBound = expressionTemporalGlobally.getTimeBound();
        if (timeBound != null && (timeBound.isLeftBounded() || timeBound.isRightBounded())) {
            if (ProcessorRegistrar.isTimedModel()) {
                builder.add(TIME_BOUNDS, ProcessorRegistrar.getProcessor(timeBound)
                        .toJSON());
            } else {
                builder.add(STEP_BOUNDS, ProcessorRegistrar.getProcessor(timeBound)
                        .toJSON());
            }
        }

        UtilModelParser.addPositional(builder, expressionTemporalGlobally.getPositional());

        return builder.build();
    }
}
