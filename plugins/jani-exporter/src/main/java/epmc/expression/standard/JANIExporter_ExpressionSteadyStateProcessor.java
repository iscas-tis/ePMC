package epmc.expression.standard;

import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;

public class JANIExporter_ExpressionSteadyStateProcessor implements JANIExporter_Processor {

    private ExpressionSteadyState expressionSteadyState = null;
    
    @Override
    public JANIExporter_Processor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionSteadyState;
        
        expressionSteadyState = (ExpressionSteadyState) obj;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionSteadyState != null;
        
        return JANIExporter_ProcessorRegistrar.getProcessor(expressionSteadyState.getOperand1())
                .toJSON();
    }

}
