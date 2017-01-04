package epmc.sexpression;

import org.junit.Test;

import epmc.error.EPMCException;
import epmc.sexpression.SExpressionParser;
import epmc.sexpression.UtilSExpression;

public class SExpressionParserTest {
	@Test
	public void parserText() throws EPMCException {
		SExpressionParser parser = UtilSExpression.newParser();
		System.out.println(parser.parse(" ( 4.5 (a ddb( ö	𤽜  )(a())) ((asdfs)c(d e)) )"));
		System.out.println(parser.parse("asdf"));
//		System.out.println(parser.parse(""));
	}
}
