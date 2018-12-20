package epmc.expression.standard;

import javax.json.JsonValue;

import epmc.jani.exporter.operatorprocessor.OperatorProcessorRegistrar;
import epmc.jani.exporter.processor.JANIProcessor;

public class JANIExporter_ExpressionOperatorProcessor implements JANIProcessor {

    private ExpressionOperator expressionOperator = null;
    
    @Override
    public JANIProcessor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionOperator;
        
        expressionOperator = (ExpressionOperator) obj;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionOperator != null;
        
        return OperatorProcessorRegistrar.getOperatorProcessor(expressionOperator)
                .toJSON();
    }

}
