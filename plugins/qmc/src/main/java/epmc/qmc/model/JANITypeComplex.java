package epmc.qmc.model;

import java.util.Map;

import javax.json.JsonValue;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.type.JANIType;
import epmc.qmc.expression.ContextExpressionQMC;
import epmc.qmc.value.TypeComplex;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class JANITypeComplex implements JANIType {
    private transient ModelJANI model;

    JANITypeComplex(ContextExpressionQMC contextExpression, Positional positional) {
    }

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    @Override
    public JANIType replace(Map<Expression, Expression> map) {
        return this;
    }

    public void checkExpressionConsistency(Map<Expression, Type> types) {
    }

    @Override
    public TypeAlgebra toType() {
        return TypeComplex.get();
    }



    @Override
    public JANINode parse(JsonValue value) {
        return parseAsJANIType(value);
    }

    @Override 
    public JANIType parseAsJANIType(JsonValue value) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public JsonValue generate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Value getDefaultValue() {
        return UtilValue.newValue(toType(), 0);
    }
}
