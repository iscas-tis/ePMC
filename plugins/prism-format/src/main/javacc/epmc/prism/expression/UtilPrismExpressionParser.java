package epmc.prism.expression;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.value.OperatorAnd;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class UtilPrismExpressionParser {
	private static final class ParseValueProviderReal implements ExpressionLiteral.ValueProvider {
		private final String string;

		private ParseValueProviderReal(String string) {
			assert string != null;
			this.string = string;
		}
		
		@Override
		public Value provideValue() throws EPMCException {
			return UtilValue.newValue(TypeReal.get(), string);
		}
	}

	private static final class ParseValueProviderInteger implements ExpressionLiteral.ValueProvider {
		private final String string;

		private ParseValueProviderInteger(String string) {
			assert string != null;
			this.string = string;
		}
		
		@Override
		public Value provideValue() throws EPMCException {
			return UtilValue.newValue(TypeInteger.get(), string);
		}
	}

	public static ExpressionLiteral.ValueProvider newParseValueProviderReal(String string) {
		assert string != null;
		return new ParseValueProviderReal(string);
	}

	public static ExpressionLiteral.ValueProvider newParseValueProviderInteger(String string) {
		assert string != null;
		return new ParseValueProviderInteger(string);
	}

	public static Expression and(Expression a, Expression b, Positional positional) {
		return new ExpressionOperator.Builder()
				.setOperator(OperatorAnd.AND)
				.setOperands(a, b)
				.setPositional(positional)
				.build();
	}

	private UtilPrismExpressionParser() {
	}
}
