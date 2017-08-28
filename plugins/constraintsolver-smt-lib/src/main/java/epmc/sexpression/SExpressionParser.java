/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.sexpression;

import static epmc.error.UtilError.*;

import java.util.ArrayList;
import java.util.Arrays;

public final class SExpressionParser {
    private final static char CHAR_END_STRING = '\0';
    private final static char CHAR_LIST_OPEN = '(';
    private final static char CHAR_LIST_CLOSE = ')';

    private String string;
    private char[] currentToken = new char[1];
    private int tokenIndex;
    private char nextChar;
    private final ArrayList<SExpression> childrenStack = new ArrayList<>();

    SExpressionParser() {
    }

    public SExpression parse(String string) {
        this.string = string;
        tokenIndex = 0;
        nextChar = nextChar();
        SExpression result = parseInternal(nextToken());
        while (nextChar != CHAR_END_STRING) {
            ensure(Character.isWhitespace(nextChar), ProblemsSExpression.SEXPRESSION_END_OF_INPUT_EXPECTED);
            nextChar = nextChar();
        }
        return result;
    }

    private char nextChar() {
        if (string.length() <= tokenIndex) {
            return CHAR_END_STRING;
        }
        char result = string.charAt(tokenIndex);
        tokenIndex++;
        return result;
    }

    private String nextToken() {
        ensure(nextChar != CHAR_END_STRING, ProblemsSExpression.SEXPRESSION_UNEXPECTED_END_OF_INPUT);
        while (Character.isWhitespace(nextChar)) {
            nextChar = nextChar();
        }
        if (nextChar == CHAR_LIST_OPEN) {
            nextChar = nextChar();
            return SExpression.LIST_OPEN;
        } else if (nextChar == CHAR_LIST_CLOSE) {
            nextChar = nextChar();
            return SExpression.LIST_CLOSE;
        } else {
            int numChars = 0;
            while (nextChar != CHAR_LIST_OPEN && nextChar != CHAR_LIST_CLOSE
                    && !Character.isWhitespace(nextChar)
                    && nextChar != CHAR_END_STRING) {
                if (currentToken.length <= numChars) {
                    currentToken = Arrays.copyOf(currentToken, currentToken.length * 2);
                }
                currentToken[numChars] = nextChar;
                nextChar = nextChar();
                numChars++;
            }
            ensure(numChars > 0, ProblemsSExpression.SEXPRESSION_UNEXPECTED_END_OF_INPUT);
            String result = new String(currentToken, 0, numChars);
            return result;
        }
    }

    private SExpression parseInternal(String token) {
        assert token != null;
        if (token == SExpression.LIST_CLOSE) {
            fail(ProblemsSExpression.SEXPRESSION_UNEXPECTED_CLOSING_BRACE);
        } else if (token != SExpression.LIST_OPEN) {
            return new SExpression(null, token);
        } else {
            token = nextToken();
            int start = childrenStack.size();
            while (token != SExpression.LIST_CLOSE) {
                childrenStack.add(parseInternal(token));
                token = nextToken();
            }
            SExpression[] children = new SExpression[childrenStack.size() - start];
            int childrenSize = childrenStack.size();
            for (int i = childrenSize - 1; i >= start; i--) {
                children[i - start] = childrenStack.get(i); 
                childrenStack.remove(i);
            }
            return new SExpression(children, null);
        }
        return null;
    }
}
