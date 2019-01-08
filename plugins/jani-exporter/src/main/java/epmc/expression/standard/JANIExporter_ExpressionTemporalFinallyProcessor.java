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

import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.UtilModelParser;

public class JANIExporter_ExpressionTemporalFinallyProcessor implements JANIExporter_Processor {
    private static final String OP = "op";
    private static final String F = "F";
    private static final String EXP = "exp";
    private static final String U = "U";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String STEP_BOUNDS = "step-bounds";
    private static final String TIME_BOUNDS = "time-bounds";

    private ExpressionTemporalFinally expressionTemporalFinally = null;

    @Override
    public JANIExporter_Processor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionTemporalFinally; 

        expressionTemporalFinally = (ExpressionTemporalFinally) obj;
        
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionTemporalFinally != null;
        
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        if (JANIExporter_ProcessorRegistrar.useDerivedOperators()) {
            builder.add(OP, F);
            builder.add(EXP, JANIExporter_ProcessorRegistrar.getProcessor(expressionTemporalFinally.getOperand())
                    .toJSON());
        } else {
            //F op = true U op
            builder.add(OP, U);
            builder.add(LEFT, true);
            builder.add(RIGHT, JANIExporter_ProcessorRegistrar.getProcessor(expressionTemporalFinally.getOperand())
                    .toJSON());
        }
        
        TimeBound timeBound = expressionTemporalFinally.getTimeBound();
        if (timeBound != null && (timeBound.isLeftBounded() || timeBound.isRightBounded())) {
            if (JANIExporter_ProcessorRegistrar.isDiscreteTimeModel()) {
                builder.add(STEP_BOUNDS, JANIExporter_ProcessorRegistrar.getProcessor(timeBound)
                        .toJSON());
            }
            if (JANIExporter_ProcessorRegistrar.isContinuousTimeModel() || JANIExporter_ProcessorRegistrar.isTimedModel()){
                builder.add(TIME_BOUNDS, JANIExporter_ProcessorRegistrar.getProcessor(timeBound)
                        .toJSON());
            }
        }

        UtilModelParser.addPositional(builder, expressionTemporalFinally.getPositional());

        return builder.build();
    }
}
