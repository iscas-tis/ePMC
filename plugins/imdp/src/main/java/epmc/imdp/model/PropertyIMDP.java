package epmc.imdp.model;

import java.io.InputStream;
import java.io.OutputStream;

import epmc.expression.Expression;
import epmc.modelchecker.Property;
import epmc.modelchecker.RawProperties;
import epmc.prism.model.PropertyPRISM;
import epmc.value.Type;

public final class PropertyIMDP implements Property {
    public final static String IDENTIFIER = "imdp";
    private final PropertyPRISM propertyPRISM = new PropertyPRISM();

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Expression parseExpression(Object identifier, InputStream stream) {
        assert stream != null;
        return propertyPRISM.parseExpression(identifier, stream);
    }

    @Override
    public void writeProperties(RawProperties properties, OutputStream stream) {
        assert properties != null;
        assert stream != null;
        assert stream != null;
        propertyPRISM.writeProperties(properties, stream);
    }

    @Override
    public void readProperties(Object identifier, RawProperties properties, InputStream stream) {
        assert stream != null;
        propertyPRISM.readProperties(identifier, properties, stream);
    }

    @Override
    public Type parseType(Object identifier, String type) {
        return propertyPRISM.parseType(identifier, type);
    }
}
