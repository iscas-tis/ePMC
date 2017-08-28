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

package epmc.messages;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Represents a message to the user.
 * This class represents a class of messages, that is the message not yet
 * instantiated with according parameters.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Message implements Serializable {
    /**
     * Builder for {@link Message} object.
     * 
     * @author Ernst Moritz Hahn
     */
    public final static class Builder {
        /**
         * Whether {@link Message} already built.
         * To prevent accidentially calling {@link #build()} multiple times, we
         * use {@code assert} statements to prevent this.
         * This variable denotes whether {@link #build()} has already been
         * called.
         */
        private boolean built;
        /** Name of resource bundle to use in built object. */
        private String bundle;
        /** Identifier for built object. */
        private String identifier;
        /** Whether the build message is a warning. */
        private boolean warning;

        /**
         * Set resource bundle name to use for message.
         * This method must not be called after calling {@link #build()}.
         * 
         * @param bundle resource bundle name to use for message
         * @return builder object itself, for method chaining
         */
        public Builder setBundle(String bundle) {
            assert !built;
            this.bundle = bundle;
            return this;
        }

        /**
         * Get resource bundle name to use for message.
         * 
         * @return resource bundle name to use for message
         */
        private String getBundle() {
            return bundle;
        }

        /**
         * Set identifier to use for message.
         * This method must not be called after calling {@link #build()}.
         * 
         * @param identifier identifier to use for message
         * @return builder object itself, for method chaining
         */
        public Builder setIdentifier(String identifier) {
            assert !built;
            this.identifier = identifier;
            return this;
        }

        /**
         * Get identifier to use for message.
         * 
         * @return identifier to use for message.
         */
        private String getIdentifier() {
            return identifier;
        }

        /**
         * Set whether message to construct is a warning.
         * This method must not be called after calling {@link #build()}.
         * 
         * @param warning whether message to construct is a warning
         * @return builder object itself, for method chaining
         */
        public Builder setWarning(boolean warning) {
            assert !built;
            this.warning = warning;
            return this;
        }

        /**
         * Get whether message to construct is a warning.
         * 
         * @return whether message to construct is a warning.
         */
        private boolean isWarning() {
            return warning;
        }

        /**
         * Set message to be constructed to be a warning.
         * This method must not be called after calling {@link #build()}.
         * 
         * @return builder object itself, for method chaining
         */
        public Builder setWarning() {
            assert !built;
            this.warning = true;
            return this;
        }

        /**
         * Construct message with according parameters set.
         * This method must only be called once.
         * After calling this method, no further methods to set parameters must
         * be called
         * 
         * @return message constructed
         */
        public Message build() {
            assert !built;
            built = true;
            return new Message(this);
        }
    }

    /** 1L, as I don't know any better. */
    private static final long serialVersionUID = 1L;

    /** Base name of property bundle the message translation is read from. */
    private final String bundle;
    /** Identifier of the message in the resource bundle. */
    private final String name;
    /** Whether this message is a warning. */
    private final boolean warning;

    /**
     * Create a new message.
     * The information to create the message is read from the builder parameter.
     * The builder object must not be {@code null}.
     * Also, the bundle and identifier of the builder must have been set.
     * 
     * @param builder
     */
    private Message(Builder builder) {
        assert builder != null;
        assert builder.getBundle() != null;
        assert builder.getIdentifier() != null;
        ResourceBundle defaultMessages = ResourceBundle.getBundle(
                builder.getBundle(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
        assert defaultMessages != null;
        assert defaultMessages.containsKey(builder.getIdentifier()) : builder.getIdentifier();
        bundle = builder.getBundle();
        name = builder.getIdentifier();
        warning = builder.isWarning();
    }

    /**
     * Get identifier of the message.
     * The identifier is used in the resource bundle to
     * translate the messages. It may also be used to identify the message for
     * further processing, or to be directly printed out for machine-readable
     * output.
     * 
     * @return identifier of the message
     */
    public String getIdentifier() {
        return name;
    }

    /**
     * Get user-readable string in given locale.
     * The locale parameter must not be {@code null}.
     * 
     * @param locale locale to use for message
     * @return user-readable string in given locale
     */
    public String getMessage(Locale locale) {
        assert locale != null;
        ResourceBundle messages = ResourceBundle.getBundle(this.bundle, locale,
                Thread.currentThread().getContextClassLoader());
        return messages.getString(this.name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof Message)) {
            return false;
        }
        Message other = (Message) obj;
        return name.equals(other.getIdentifier());
    }

    /**
     * Returns whether message is a warning.
     * 
     * @return whether message is a warning
     */
    public boolean isWarning() {
        return warning;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
