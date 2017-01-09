package epmc.sexpression;

public final class UtilSExpression {
	public static SExpression newSExpression(String atomic) {
		assert atomic != null;
		return new SExpression(null, atomic);
	}

	public static SExpression newSExpression(SExpression... children) {
		assert children != null;
		for (SExpression child : children) {
			assert child != null;
		}
		return new SExpression(children.clone(), null);
	}
	
	public static SExpressionParser newParser() {
		return new SExpressionParser();
	}

	private UtilSExpression() {
	}
}
