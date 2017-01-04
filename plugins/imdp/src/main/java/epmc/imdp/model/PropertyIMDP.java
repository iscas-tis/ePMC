package epmc.imdp.model;

import java.io.InputStream;
import java.io.OutputStream;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.modelchecker.Property;
import epmc.modelchecker.RawProperties;
import epmc.prism.model.PropertyPRISM;
import epmc.value.ContextValue;
import epmc.value.Type;

public final class PropertyIMDP implements Property {
	public final static String IDENTIFIER = "imdp";
	private final PropertyPRISM propertyPRISM = new PropertyPRISM();

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setContext(ContextValue context) {
		assert context != null;
		propertyPRISM.setContext(context);
	}

	@Override
	public Expression parseExpression(InputStream stream) throws EPMCException {
		assert stream != null;
		return propertyPRISM.parseExpression(stream);
	}

	@Override
	public void writeProperties(RawProperties properties, OutputStream stream) throws EPMCException {
		assert properties != null;
		assert stream != null;
        assert stream != null;
		propertyPRISM.writeProperties(properties, stream);
	}

	@Override
	public void readProperties(RawProperties properties, InputStream stream) throws EPMCException {
        assert stream != null;
        propertyPRISM.readProperties(properties, stream);
	}

	@Override
	public ContextValue getContext() {
		return propertyPRISM.getContext();
	}

	@Override
	public Type parseType(String type) throws EPMCException {
		return propertyPRISM.parseType(type);
	}
}
