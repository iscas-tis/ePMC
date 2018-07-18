package epmc.param.value.polynomial;

import java.util.ArrayList;
import java.util.List;

import epmc.param.value.Term;

/**
 * Parser for polynomials.
 * 
 * @author Ernst Moritz Hahn
 */
public final class PolynomialParser {	
	public static void parse(ValuePolynomial result, String string) {
		assert result != null;
		assert string != null;
		TypePolynomial type = result.getType();
		string = string.trim();
		string = string.replaceAll(" ", "");
		if (string.equals("0")) {
		    result.set(0);
		    return;
		}
		string = string.replace("-", "+-");
		String[] parts = string.split("\\+");
		List<Term> terms = new ArrayList<>();
		
		for (String part : parts) {
			Term term = new Term(type);
			if (part.length() == 0) {
				continue;
			}
			int termPos = 0;
			if (part.charAt(0) == '-') {
				termPos++;
			}
			while (termPos < part.length() && Character.isDigit(part.charAt(termPos))) {
				termPos++;
			}
			String coefficientString = part.substring(0, termPos);
			if (coefficientString.length() == 0) {
				coefficientString = "1";
			} else if (coefficientString.equals("-")) {
                coefficientString = "-1";			    
			}
			term.setCoefficient(coefficientString);
			if (termPos < part.length() && part.charAt(termPos) == '*') {
				termPos++;
			}
			while (termPos < part.length()) {
				int parameterStart = termPos;
				while (termPos < part.length()
						&& Character.isAlphabetic(part.charAt(termPos))) {
					termPos++;
				}
				String parameter = part.substring(parameterStart, termPos);
				int parameterNr = type.getParameterSet().getParameterNumber(parameter);
				int exponent = 1;
				if (termPos < part.length() && part.charAt(termPos) == '^') {
					termPos++;
					int exponentStart = termPos;
					while (termPos < part.length() && Character.isDigit(part.charAt(termPos))) {
						termPos++;
					}
					String exponentString = part.substring(exponentStart, termPos);
					exponent = Integer.parseInt(exponentString);
				}
				term.setExponent(parameterNr, exponent);
				if (termPos < part.length() && part.charAt(termPos) == '*') {
					termPos++;
				}
			}
			terms.add(term);
		}
		Term.toPolynomial(result, terms);
	}

	private PolynomialParser() {
	}   
}
