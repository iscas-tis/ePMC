package epmc.expression.standard;

import javax.json.JsonValue;

import epmc.jani.exporter.operatorprocessor.JANIExporter_OperatorProcessorRegistrar;
import epmc.jani.exporter.processor.JANIExporter_Processor;

public class JANIExporter_ExpressionOperatorProcessor implements JANIExporter_Processor {

    private ExpressionOperator expressionOperator = null;
    
    @Override
    public JANIExporter_Processor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionOperator;
        
        expressionOperator = (ExpressionOperator) obj;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionOperator != null;
        
        return JANIExporter_OperatorProcessorRegistrar.getOperatorProcessor(expressionOperator)
                .toJSON();
    }

}
