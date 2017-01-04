package epmc.qmc.model;

import java.io.InputStream;
import java.io.OutputStream;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.modelchecker.Property;
import epmc.modelchecker.RawProperties;
import epmc.qmc.expression.QMCExpressionParser;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;

public final class PropertyPRISMQMC implements Property {
	public final static String IDENTIFIER = "prism-qmc";
	private final static String INT = "int";
	private final static String DOUBLE = "double";
	private ContextValue context;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setContext(ContextValue context) {
		assert context != null;
		this.context = context;
	}

	@Override
	public Expression parseExpression(InputStream stream) throws EPMCException {
		assert stream != null;
		QMCExpressionParser parser = new QMCExpressionParser(stream);
        return parser.parseExpressionAsProperty(context);
	}

	@Override
	public void writeProperties(RawProperties properties, OutputStream stream) throws EPMCException {
		assert properties != null;
		assert stream != null;
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readProperties(RawProperties properties, InputStream stream) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ContextValue getContext() {
		return context;
	}

	@Override
	public Type parseType(String type) throws EPMCException {
		assert type != null;
		switch (type) {
		case INT:
			return TypeInteger.get(getContextValue());
		case DOUBLE:
			return TypeReal.get(getContextValue());
		}
		assert false;
		return null;
	}
	
	private ContextValue getContextValue() {
		return context;
	}

}
