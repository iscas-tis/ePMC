package epmc.jani.model;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;

public class JANIExporter_TimeProgressProcessor implements JANIExporter_Processor {
    private static final String EXP = "exp";
    private static final String COMMENT = "comment";

    private TimeProgress timeProgress = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
        assert component != null;
        assert component instanceof TimeProgress; 

        timeProgress = (TimeProgress) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert timeProgress != null;

        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        builder.add(EXP, JANIExporter_ProcessorRegistrar.getProcessor(timeProgress.getExp())
                .toJSON());
        
        String comment = timeProgress.getComment();
        if (comment != null) {
            builder.add(COMMENT, comment);
        }
        
        return builder.build();
    }
}
