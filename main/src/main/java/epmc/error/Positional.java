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

package epmc.error;

import java.io.Serializable;

// TODO decide whether lines/columns should start with zero or one; also check
// how this is currently handled in the tool.

// TODO unclear whether it makes sense for a Positional to always refer to
// line/position numbers - e.g. for graphical representations. If not, might
// consider turning this into an interface.

// TODO complete documentation

/**
 * Obtain a position in a file.
 * This class is intended to provide feedback from which part of the input a
 * problems occurs. Objects of classes outside the
 * {@link epmc.main.error error} package may store such positions to
 * later on throw {@link EPMCException}s with the appropriate position
 * information. 
 * 
 * @author Ernst Moritz Hahn
 */
public final class Positional implements Serializable {
    /**
     * Builder for {@link Positional}.
     * 
     * @author Ernst Moritz Hahn
     */
    public static final class Builder {
        private Object part;
        private long line;
        private long column;
        private String content;

        public Builder setPart(Object part) {
            this.part = part;
            return this;
        }

        private Object getPart() {
            return part;
        }

        public Builder setLine(long line) {
            this.line = line;
            return this;
        }

        private long getLine() {
            return line;
        }

        public Builder setColumn(long column) {
            this.column = column;
            return this;
        }

        private long getColumn() {
            return column;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        private String getContent() {
            return content;
        }

        public Positional build() {
            return new Positional(this);
        }
    }

    private final static String NULL = "null";
    /** Serial version UID - 1L, as I don't know any better. */
    private static final long serialVersionUID = 1L;
    // TODO change to String? Otherwise, might not be clear whether referring
    // to model or to some property
    /** Part of input to which this positional refers. */
    private final Object part;
    /** Line number to which this positional refers to. */
    private final long line;
    /** Line number to which this positional refers to. */
    private final long column;
    /** User-readable representation of this positional. */
    private final String content;

    private Positional(Builder builder) {
        assert builder != null;
        assert builder.getLine() >= 0;
        assert builder.getColumn() >= 0;
        this.part = builder.getPart();
        this.line = builder.getLine();
        this.column = builder.getColumn();
        String content = builder.getContent();
        /*
        if (content == null) {
            content = part + COMMA + line + COMMA + column;
        }
         */
        this.content = content;
    }

    @Override
    public String toString() {
        return String.format("positional(%s, %s, %d, %d)",
                content, part != null ? part.toString() : NULL, line, column);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof Positional)) {
            return false;
        }
        Positional other = (Positional) obj;
        if ((this.part == null) != (other.part != null)) {
            return false;
        }
        if ((part != null) && !this.part.equals(other.part)) {
            return false;
        }
        if (this.line != other.line) {
            return false;
        }
        if (this.column != other.column) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = (part != null ? part.hashCode() : 3751) + (hash << 6) + (hash << 16) - hash;
        hash = Long.hashCode(line) + (hash << 6) + (hash << 16) - hash;
        hash = Long.hashCode(column) + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    /**
     * Obtain a description of the position as a user-readable string.
     * 
     * @return user-readable string about position
     */
    public String getContent() {
        return content;
    }

    /**
     * Obtain the part of the model the position refers to.
     * 
     * @return part of the model the position refers to
     */
    public Object getPart() {
        return part;
    }

    /**
     * Obtain line number stored in position object.
     * Might be used e.g. to mark a particular place at a file editor.
     * 
     * @return line number
     */
    public long getLine() {
        return line;
    }

    /**
     * Obtain column number stored in position object.
     * Might be used e.g. to mark a particular place at a file editor.
     * 
     * @return column number
     */
    public long getColumn() {
        return column;
    }
}
