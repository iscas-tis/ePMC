package epmc.rddl.model;

import java.io.InputStream;
import java.io.OutputStream;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.modelchecker.Property;
import epmc.modelchecker.RawProperties;
import epmc.value.ContextValue;
import epmc.value.Type;

public final class PropertyRDDL implements Property {
	public final static String IDENTIFIER = "rddl";
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
//        ExpressionParser parser = new QMCExpressionParser(stream);
  //      return parser.parseExpressionAsProperty(context);
		return null;
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
		assert false;
		// TODO Auto-generated method stub
		return null;
	}

}
