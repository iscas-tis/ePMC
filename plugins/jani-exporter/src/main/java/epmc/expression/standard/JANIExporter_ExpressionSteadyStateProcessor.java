package epmc.expression.standard;

import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;

public class JANIExporter_ExpressionSteadyStateProcessor implements JANIProcessor {

    private ExpressionSteadyState expressionSteadyState = null;
    
    @Override
    public JANIProcessor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionSteadyState;
        
        expressionSteadyState = (ExpressionSteadyState) obj;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionSteadyState != null;
        
        return ProcessorRegistrar.getProcessor(expressionSteadyState.getOperand1())
                .toJSON();
    }

}
