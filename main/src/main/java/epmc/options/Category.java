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

package epmc.options;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import com.google.common.base.CaseFormat;

// TODO complete documentation
// TODO describe how resource bundles are used

/**
 * Category to group options.
 * Categories provide a means to group options in a hierarchical, that is,
 * tree-like, way.
 * Each option can optionally have a category, and each category optionally can
 * have a parent category.
 * Options with no category and categories without parent categories reside on
 * the top level, that is on the root of a hierarchy tree.
 * Grouped below them are the options which belong to such a top-level category
 * and the categories which have a top-level category as parent category,
 * and so on.
 * This structure allows displaying the options to be printed in a tree-like
 * way in a GUI or, using indentation, when calling the tool from the command
 * line.
 * It also allows hiding all options and subcategories of a certain categories,
 * to allow the user to concentrate at the options it currently considers
 * relevant.
 * These structuring features are useful, because the number of available
 * options can be in the order of several hundredth, depending on how many
 * plugins are used.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Category implements Serializable, Cloneable {
    /**
     * Builder for {@link Category}.
     * 
     * @author Ernst Moritz Hahn
     */
    public final static class Builder {
        /**
         * Whether the builder has already been used to construct a category.
         * To avoid accidentally calling the {@link #build()} method multiple
         * times, we user {@code assert}ions to prevent doing so and use this
         * value to remember whether the option was already build.
         */
        private boolean built;
        /** The options the constructed category will belong to. */
        private Options options;
        /** The bundle name which will be used for the constructed category. */
        private String bundleName;
        /** The identifier which will be used for the constructed category. */
        private String identifier;
        /** Parent which to use for constructed category, or {@code null}. */
        private String parent;

        /**
         * Construct new builder.
         * This method is package-private, because we want builders to be
         * constructed using {@link Options#addCategory()}, such that they are
         * initialised for being used with the given options set.
         */
        Builder() {
        }

        /**
         * Set options the category will belong to.
         * This method may only be called if {@link #build()} has not been
         * called yet.
         * 
         * @param options options the category will belong t
         * @return the builder itself, for method chaining
         */
        Builder setOptions(Options options) {
            assert !built;
            this.options = options;
            return this;
        }

        private Options getOptions() {
            assert built;
            return options;
        }

        /**
         * Set resource bundle which will be used for the category.
         * This method may only be called if {@link #build()} has not been
         * called yet.
         * 
         * @param bundleName resource bundle which will be used for the category
         * @return the builder itself, for method chaining
         */
        public Builder setBundleName(String bundleName) {
            assert !built;
            this.bundleName = bundleName;
            return this;
        }

        public Builder setBundleName(Enum<?> bundleName) {
            assert !built;
            this.bundleName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, bundleName.name());
            return this;
        }

        private String getBundleName() {
            assert built;
            return bundleName;
        }

        /**
         * Set identifier the category will have.
         * This method may only be called if {@link #build()} has not been
         * called yet.
         * 
         * @param identifier identifier the category will have
         * @return the builder itself, for method chaining
         */
        public Builder setIdentifier(String identifier) {
            assert !built;
            this.identifier = identifier;
            return this;
        }

        /**
         * Set identifier the category will have.
         * This method may only be called if {@link #build()} has not been
         * called yet.
         * The identifier will be the {@link Enum#name()} of the enum, with
         * uppercase letters being replaced by lowercase letters, and
         * underscores being replaced by hyphens.
         * 
         * @param identifier identifier the category will have
         * @return the builder itself, for method chaining
         */
        public Builder setIdentifier(Enum<?> identifier) {
            assert !built;
            this.identifier = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, identifier.name());
            return this;
        }

        private String getIdentifier() {
            assert built;
            return identifier;
        }

        public Builder setParent(String parent) {
            assert !built;
            this.parent = parent;
            return this;
        }

        public Builder setParent(Enum<?> parent) {
            assert !built;
            this.parent = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, parent.name());
            return this;
        }

        public Builder setParent(Category parent) {
            assert !built;
            this.parent = parent.getIdentifier();
            return this;
        }

        private String getParent() {
            return parent;
        }

        public Category build() {
            assert !built;
            assert options != null;
            built = true;
            Category result = new Category(this);
            options.addCategory(result);
            return result;
        }
    }

    /** 1L, as I don't know any better. */
    private static final long serialVersionUID = 1L;
    /** Prefix for short description in resource file. */
    private final static String SHORT_PREFIX = "short-";

    /** The options which this category belongs to. */
    private final Options options;
    /** The name of the resource bundle user-readable name etc. are read from. */
    private final String bundleName;
    /** Identifier of the category. */
    private final String identifier;
    /** Parent category of this category, or {@code null}. */
    private final Category parent;

    private Category(Builder builder) {
        assert builder != null;
        assert builder.getOptions() != null;
        assert builder.getBundleName() != null;
        assert builder.getIdentifier() != null;

        options = builder.getOptions();
        bundleName = builder.getBundleName();
        identifier = builder.getIdentifier();
        parent = builder.getParent() != null
                ? options.getCategory(builder.getParent())
                        : null;
    }

    public String getBundleName() {
        return bundleName;
    }

    /**
     * Get identifier of this category.
     * 
     * @return identifier of this category
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get parent category of this category, or {@code null}.
     * If {@code null} is obtained, this category is at top level.
     * 
     * @return parent category of this category, or {@code null}
     */
    public Category getParent() {
        return parent;
    }

    /**
     * Obtain short user-readable description of the category.
     * The description shall be short enough to be shown in a single line of
     * around 72 characters.
     * 
     * @return short user-readable description of the category
     */
    public String getShortDescription() {
        ResourceBundle poMsg = getBundle();
        return poMsg.getString(SHORT_PREFIX + identifier);
    }

    /**
     * Read resource bundle from base name given with {@link Options} {@link Locale}.
     * @return
     */
    private ResourceBundle getBundle() {
        Locale locale = options.getLocale();
        ResourceBundle poMsg = ResourceBundle.getBundle(bundleName, locale, Thread.currentThread().getContextClassLoader());
        return poMsg;
    }
}
