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

package epmc.command;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import epmc.options.Category;
import epmc.options.Command;
import epmc.options.Option;
import epmc.options.OptionType;
import epmc.options.Options;
import epmc.util.Util;

/**
 * Class to print usage information about options set.
 * The usage information consists of a description of the parameters of the
 * options and commands of the options set, and how to use them.
 * 
 * @author Ernst Moritz Hahn
 */
final class UsagePrinter {
    /** String containing "[program-options]". */
    private final static String PROGRAM_OPTIONS_STRING = "[program-options]";
    /** String containing "command". */
    private final static String COMMAND_STRING = "command";
    /** String containing dot. */
    private final static String DOT = ".";
    /** String containing new line*/
    private final static String NEWLINE = "\n";
    /** Key in the resource file to read "Available program options". */
    private final static String AVAILABLE_PROGRAM_OPTIONS = "availableProgramOptions";
    /** Key in the resource file to read "Default". */
    private final static String DEFAULT = "default";
    /** Key in the resource file to read "Type". */
    private final static String TYPE = "type";
    /** Key in the resource file to read "Available commands". */
    private final static String AVAILABLE_COMMANDS = "availableCommands";
    /** Key in the resource file to read "Usage". */
    private final static String USAGE = "usage";
    /** Key in resource file for "Running revision". */
    private final static String RUNNING_TOOL_REVISION = "running-tool-revision";
    /** String containing colon. */
    private final static String COLON = ":";
    /** String containing \0 */
    private final static String NULL_STRING = "\0";
    /** Empty string. */
    private final static String EMPTY = "";
    /** String containing single space character. */
    private final static String SPACE = " ";
    /** String containing a single pipe character. */
    private final static String PIPE = "|";
    /** String containing smaller than character. */
    private final static String SMALLER_THAN = "<";
    /** String containing larger than character. */
    private final static String LARGER_THAN = ">";
    /** Key in the resource file to read the name of the tool. */
    private final static String TOOL_CMD = "toolCmd";
    /** Prefix used for the options. */
    private final static String OPTION_PREFIX = "--";
    /** Maximal line length of the program options description */
    private final static int MAX_LINE_LENGTH = 72;
    /**
     * First part of regular expression to split line into parts of given
     * length. To complete the sequence, this string shall be followed by a
     * number and then {@link #ALIGN_CHARS_REGEXP_SECOND}.
     * */
    private static final String ALIGN_CHARS_REGEXP_FIRST = "(?<=\\G.{";
    /**
     * Second part of regular expression to split line into parts of given
     * length, see {@link #ALIGN_CHARS_REGEXP_FIRST}.
     */
    private static final String ALIGN_CHARS_REGEXP_SECOND = "})";

    /**
     * Get usage information of the given options set.
     * The options parameter may not be {@code null}.
     * 
     * @param options options to get usage information of
     * @return usage information of the given options set
     */
    static String getUsage() {
        Options options = Options.get();
        assert options != null;
        StringBuilder builder = new StringBuilder();
        appendCommands(builder, options);
        appendOptions(builder, options);        
        return builder.toString();
    }

    /**
     * Append information about the options commands to given string builder.
     * None of the parameters may be {@code null}.
     * 
     * @param builder string builder to append information to
     * @param options options the command information shall be appended
     */
    private static void appendCommands(StringBuilder builder, Options options) {
        assert builder != null;
        assert options != null;
        Locale locale = Locale.getDefault();
        MessageFormat formatter = new MessageFormat(EMPTY);
        ResourceBundle poMsg = ResourceBundle.getBundle(options.getResourceFileName(), locale);
        formatter.setLocale(locale);
        builder.append(options.getToolName() + COLON + SPACE);
        builder.append(options.getToolDescription() + NEWLINE + NEWLINE);
        String revision = Util.getManifestEntry(Util.SCM_REVISION);
        if (revision != null) {
            revision = revision.trim();
        }
        if (revision != null && !revision.equals(EMPTY)) {
            formatter.applyPattern(poMsg.getString(RUNNING_TOOL_REVISION));
            builder.append(formatter.format(new Object[]{options.getToolName(), revision}) + NEWLINE);
        }
        builder.append(poMsg.getString(USAGE));
        String usageString = COLON + SPACE + SMALLER_THAN + poMsg.getString(TOOL_CMD) + LARGER_THAN + SPACE + SMALLER_THAN + COMMAND_STRING + LARGER_THAN + SPACE + PROGRAM_OPTIONS_STRING + NEWLINE + NEWLINE;
        builder.append(usageString);
        builder.append(poMsg.getString(AVAILABLE_COMMANDS) + COLON + NEWLINE);
        List<String> cmdStrings = new ArrayList<>();
        int maxCmdStringSize = 0;
        for (String commandString : options.getCommands().keySet()) {
            String cmdStr = SPACE + SPACE + commandString + SPACE;
            maxCmdStringSize = Math.max(maxCmdStringSize, cmdStr.length());
            cmdStrings.add(cmdStr);
        }
        maxCmdStringSize += 2;
        Iterator<Command> cmdIter = options.getCommands().values().iterator();
        for (String formattedCommandStr : cmdStrings) {
            Command command = cmdIter.next();
            if (!command.isCommandLine()) {
                continue;
            }
            String dots = dots(maxCmdStringSize - formattedCommandStr.length());
            formattedCommandStr += dots + SPACE;
            formattedCommandStr += command.getShortDescription();
            formattedCommandStr += NEWLINE;
            builder.append(formattedCommandStr);
        }
        builder.append(NEWLINE);
    }

    /**
     * Append information about the options of the given options set to builder.
     * None of the parameters may be {@code null}.
     * 
     * @param builder string builder to append information to
     * @param options options the command information shall be appended
     */
    private static void appendOptions(StringBuilder builder, Options options) {
        assert builder != null;
        assert options != null;

        Map<Category,Set<Option>> optionsByCategory = buildOptionsByCategory(options);
        Map<Category,Set<Category>> hierarchy = buildHierarchy(options);

        Locale locale = Locale.getDefault();
        ResourceBundle poMsg = ResourceBundle.getBundle(options.getResourceFileName(), locale);
        builder.append(poMsg.getString(AVAILABLE_PROGRAM_OPTIONS) + COLON + NEWLINE);
        Collection<Option> nonCategorisedOptions = optionsByCategory.get(null);

        for (Option option : nonCategorisedOptions) {
            if (!option.isCommandLine()) {
                continue;
            }
            appendOption(builder, option, 0);
        }
        for (Category category : optionsByCategory.keySet()) {
            if (category == null || category.getParent() != null) {
                continue;
            }
            appendCategorisedOptions(builder, category, hierarchy,
                    optionsByCategory, 0);
        }
    }

    private static void appendCategorisedOptions(StringBuilder builder, Category category,
            Map<Category, Set<Category>> hierarchy,
            Map<Category, Set<Option>> optionsByCategory, int level) {
        Set<Option> options = optionsByCategory.get(category);
        builder.append(spacify(category.getShortDescription() + COLON, 2 + 2 * level));
        for (Option option : options) {
            if (!option.isCommandLine()) {
                continue;
            }
            appendOption(builder, option, level + 1);
        }
        for (Category child : hierarchy.get(category)) {
            appendCategorisedOptions(builder, child, hierarchy,
                    optionsByCategory, level + 1);
        }
    }

    private static void appendOption(StringBuilder builder, Option option, int level) {
        assert builder != null;
        assert option != null;
        Locale locale = Locale.getDefault();
        ResourceBundle poMsg = ResourceBundle.getBundle(Options.get().getResourceFileName(), locale);
        String topLine = buildOptionTopLine(poMsg, option);
        builder.append(spacify(topLine, 2 + level * 2));
        String description = alignWords(option.getShortDescription(), MAX_LINE_LENGTH - (6 + level * 2));
        description = spacify(description, 6 + level * 2);
        builder.append(description);
        String typeLines = buildOptionTypeLines(poMsg, option);
        typeLines = alignPiped(typeLines, MAX_LINE_LENGTH - (6 + level * 2));
        typeLines = spacify(typeLines, 6 + level * 2);
        builder.append(typeLines);
        String defaultLines = buildOptionDefaultLines(poMsg, option);
        if (defaultLines != null) {
            defaultLines = alignCharacters(defaultLines, MAX_LINE_LENGTH - (6 + level * 2));
            defaultLines = spacify(defaultLines, 6 + level * 2);
            builder.append(defaultLines);
        }
    }

    private static Map<Category,Set<Category>> buildHierarchy(Options options) {
        assert options != null;
        Map<Category,Set<Category>> result = new LinkedHashMap<>();
        for (Category category : options.getAllCategories().values()) {
            result.put(category, new LinkedHashSet<>());
        }
        for (Category category : options.getAllCategories().values()) {
            Category parent = category.getParent();
            if (parent == null) {
                continue;
            }
            result.get(parent).add(category);
        }
        return result;
    }

    private static Map<Category, Set<Option>> buildOptionsByCategory(
            Options options) {
        assert options != null;
        Map<Category, Set<Option>> result = new LinkedHashMap<>();
        for (Option option : options.getAllOptions().values()) {
            Category category = option.getCategory();
            Set<Option> catSet = result.get(category);
            if (catSet == null) {
                catSet = new LinkedHashSet<>();
                result.put(category, catSet);
            }
            catSet.add(option);
        }
        return result;
    }

    /**
     * Create a string describing the type of a given option.
     * The internationalization information will read from resource bundle with
     * the given base name. The returned string is of the format
     * &quot;&lt;word-type-in-language&gt;: &lt;type-info&gt;&lt;newline&gt;.
     * None of the parameters may be {@code null}.
     * 
     * @param resourceBundle base name of resource bundle
     * @param option option to get type info description of
     * @return string describing the type of a given option
     */
    private static String buildOptionTypeLines(ResourceBundle resourceBundle,
            Option option) {
        assert resourceBundle != null;
        assert option != null;
        String typeInfo = resourceBundle.getString(TYPE) + COLON + SPACE + option.getTypeInfo() + NEWLINE;
        return typeInfo;
    }

    /**
     * Build string describing default value of given option.
     * The internationalization information will read from resource bundle with
     * the given base name. The returned string is of the format
     * &quot;&lt;word-default-in-language&gt;: &lt;default&gt;&lt;newline&gt;.
     * If the option does not have a default value, the method will return
     * {@code null}.
     * None of the parameters may be {@code null}.
     * 
     * @param resourceBundle base name of resource bundle
     * @param option  option to get default value description of
     * @return describing the default value of a given option or {@code null}
     */
    private static String buildOptionDefaultLines(ResourceBundle resourceBundle,
            Option option) {
        assert resourceBundle != null;
        assert option != null;
        OptionType type = option.getType();
        assert type != null : option;
        Object defaultValue = option.getDefault();
        String defaultValueString = null;
        if (defaultValue != null) {
            defaultValueString = type.unparse(defaultValue).trim();
        }
        String result = null;
        if (defaultValueString != null && !defaultValueString.equals(EMPTY)) {
            result = resourceBundle.getString(DEFAULT) + COLON + SPACE + defaultValueString + SPACE + NEWLINE;
        }
        return result;
    }

    private static String buildOptionTopLine(ResourceBundle poMsg, Option option) {
        String poStr = OPTION_PREFIX + option.getIdentifier() + SPACE;
        return poStr;
    }

    /**
     * Obtain a sequence of a give number of dots (".").
     * The number of dots must be nonnegative.
     * 
     * @param numDots length of sequence
     * @return sequence of a give number of dots (".")
     */
    private static String dots(int numDots) {
        assert numDots >= 0;
        return new String(new char[numDots]).replace(NULL_STRING, DOT);
    }

    /**
     * Obtain a sequence of a give number of spaces (" ").
     * The number of spaces must be nonnegative.
     * 
     * @param numSpaces length of sequence
     * @return sequence of a give number of spaces (" ")
     */
    private static String spaces(int numSpaces) {
        assert numSpaces >= 0;
        return new String(new char[numSpaces]).replace(NULL_STRING, SPACE);        
    }

    /**
     * Align lines by prefixing them by the given number of spaces.
     * The input string parameter may not be {@code null}, and the number of
     * spaces must be nonnegative.
     *
     * @param lines lines to align
     * @param numSpaces number of spaces to prefix lines with
     * @return aligned string
     */
    private static String spacify(String lines, int numSpaces) {
        assert lines != null;
        assert numSpaces >= 0;
        String[] linesArray = lines.split(NEWLINE);
        StringBuilder result = new StringBuilder();
        for (int lineNr = 0; lineNr < linesArray.length; lineNr++) {
            result.append(spaces(numSpaces) + linesArray[lineNr] + NEWLINE);
        }
        return result.toString();
    }

    /**
     * Split string by words into lines not exceeding length limit.
     * The line length may be exceeded if there is a single word larger than
     * the given line length. The method only splits along word limits; it is
     * not able to perform hyphenation etc.
     * The string to split must not be {@code null}, and the maximal line length
     * must be positive.
     * 
     * @param string string to split
     * @param maxLineLength maximal line length
     * @return split string
     */
    private static String alignWords(String string, int maxLineLength) {
        return alignSplit(string, maxLineLength, SPACE);
    }

    private static String alignPiped(String string, int maxLineLength) {
        return alignSplit(string, maxLineLength, PIPE);
    }

    private static String alignSplit(String string, int maxLineLength, String split) {
        assert string != null;
        assert maxLineLength >= 1;
        String[] words = string.split(Pattern.quote(split));
        StringBuilder result = new StringBuilder();
        int lineLength = 0;
        for (String word : words) {
            lineLength += word.length() + 1;
            if (lineLength > maxLineLength) {
                result.append(NEWLINE);
                lineLength = word.length() + 1;
            }
            result.append(word + split);
        }
        result.delete(result.length() - 1, result.length());
        return result.toString();
    }

    /**
     * Split string into lines of given length along characters.
     * The string to split may not be {@code null}, and the maximal line length
     * must be positive.
     * 
     * @param string string to split
     * @param maxLineLength maximal line length
     * @return split string
     */
    private static String alignCharacters(String string, int maxLineLength) {
        assert string != null;
        assert maxLineLength >= 1;
        String[] lines = string.split(ALIGN_CHARS_REGEXP_FIRST + maxLineLength + ALIGN_CHARS_REGEXP_SECOND);
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append(line + NEWLINE);
        }
        return result.toString();
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private UsagePrinter() {
    }
}
