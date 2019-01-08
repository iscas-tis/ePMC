/**
 * 
 */
package epmc.expression.standard;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.UtilModelParser;

public class JANIExporter_TimeBoundProcessor implements JANIExporter_Processor {
    private static final String LOWER = "lower";
    private static final String LOWER_EXCLUSIVE = "lower-exclusive";
    private static final String UPPER = "upper";
    private static final String UPPER_EXCLUSIVE = "upper-exclusive";

    private TimeBound timeBound = null;
    
    @Override
    public JANIExporter_Processor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof TimeBound;
   
        timeBound = (TimeBound) obj;

        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert timeBound != null;
        
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        if (timeBound.isLeftBounded()) {
            builder.add(LOWER, JANIExporter_ProcessorRegistrar.getProcessor(timeBound.getLeft())
                    .toJSON());
            builder.add(LOWER_EXCLUSIVE, timeBound.isLeftOpen());
        }
        
        if (timeBound.isRightBounded()) {
            builder.add(UPPER, JANIExporter_ProcessorRegistrar.getProcessor(timeBound.getRight())
                    .toJSON());
            builder.add(UPPER_EXCLUSIVE, timeBound.isRightOpen());
        }

        UtilModelParser.addPositional(builder, timeBound.getPositional());
        
        return builder.build();
    }
}
