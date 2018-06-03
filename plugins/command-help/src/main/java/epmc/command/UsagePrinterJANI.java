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
import java.util.Set;
import java.util.regex.Pattern;

import epmc.util.Util;
import epmc.jani.interaction.commandline.CommandLineCategory;
import epmc.jani.interaction.commandline.CommandLineCommand;
import epmc.jani.interaction.commandline.CommandLineOption;
import epmc.jani.interaction.commandline.CommandLineOptions;

/**
 * Class to print usage information about options set.
 * The usage information consists of a description of the parameters of the
 * options and commands of the options set, and how to use them.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UsagePrinterJANI {
    /** String containing "[program-options]". */
    private final static String PROGRAM_OPTIONS_STRING = "[program-options]";
    /** String containing "command". */
    private final static String COMMAND_STRING = "command";
    /** String containing dot. */
    private final static String DOT = ".";
    /** String containing new line*/
    private final static String NEWLINE = "\n";
    /** Key in the resource file to read "Usage". */
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
    public static String getUsage(CommandLineOptions options) {
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
    private static void appendCommands(StringBuilder builder, CommandLineOptions options) {
        assert builder != null;
        assert options != null;
        Locale locale = Locale.getDefault();
        MessageFormat formatter = new MessageFormat(EMPTY);
        formatter.setLocale(locale);
        builder.append(options.getToolName() + COLON + SPACE);
        builder.append(options.getToolDescription() + NEWLINE + NEWLINE);
        String revision = Util.getManifestEntry(Util.SCM_REVISION);
        if (revision != null) {
            revision = revision.trim();
        }
        if (revision != null && !revision.equals(EMPTY)) {
            formatter.applyPattern(options.getRunningToolRevisionPatter());
            builder.append(formatter.format(new Object[]{options.getToolName(), revision}) + NEWLINE);
        }
        builder.append(options.getUsagePattern());
        String usageString = COLON + SPACE + SMALLER_THAN + options.getToolCmdPattern() + LARGER_THAN + SPACE + SMALLER_THAN + COMMAND_STRING + LARGER_THAN + SPACE + PROGRAM_OPTIONS_STRING + NEWLINE + NEWLINE;
        builder.append(usageString);
        builder.append(options.getAvailableCommandsPattern() + COLON + NEWLINE);
        List<String> cmdStrings = new ArrayList<>();
        int maxCmdStringSize = 0;
        for (String commandString : options.getCommands().keySet()) {
            String cmdStr = SPACE + SPACE + commandString + SPACE;
            maxCmdStringSize = Math.max(maxCmdStringSize, cmdStr.length());
            cmdStrings.add(cmdStr);
        }
        maxCmdStringSize += 2;
        Iterator<CommandLineCommand> cmdIter = options.getCommands().values().iterator();
        for (String formattedCommandStr : cmdStrings) {
            CommandLineCommand command = cmdIter.next();
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
    private static void appendOptions(StringBuilder builder, CommandLineOptions options) {
        assert builder != null;
        assert options != null;

        Map<CommandLineCategory,Set<CommandLineOption>> optionsByCategory = buildOptionsByCategory(options);
        Map<CommandLineCategory,Set<CommandLineCategory>> hierarchy = buildHierarchy(options);

        builder.append(options.getAvailableProgramOptionsPattern() + COLON + NEWLINE);
        Collection<CommandLineOption> nonCategorisedOptions = optionsByCategory.get(null);

        for (CommandLineOption option : nonCategorisedOptions) {
            appendOption(builder, option, options, 0);
        }
        for (CommandLineCategory category : optionsByCategory.keySet()) {
            if (category == null || category.getParent() != null) {
                continue;
            }
            appendCategorisedOptions(builder, category, hierarchy,
                    optionsByCategory, options, 0);
        }
    }

    private static void appendCategorisedOptions(StringBuilder builder, CommandLineCategory category,
            Map<CommandLineCategory, Set<CommandLineCategory>> hierarchy,
            Map<CommandLineCategory, Set<CommandLineOption>> optionsByCategory, CommandLineOptions options, int level) {
        assert hierarchy != null;
        Set<CommandLineOption> categorisedOptions = optionsByCategory.get(category);
        builder.append(spacify(category.getShortDescription() + COLON, 2 + 2 * level));
        for (CommandLineOption option : categorisedOptions) {
            appendOption(builder, option, options, level + 1);
        }
        for (CommandLineCategory child : hierarchy.get(category)) {
            appendCategorisedOptions(builder, child, hierarchy,
                    optionsByCategory, options, level + 1);
        }
    }

    private static void appendOption(StringBuilder builder, CommandLineOption option, CommandLineOptions options, int level) {
        assert builder != null;
        assert option != null;
        String topLine = buildOptionTopLine(option);
        builder.append(spacify(topLine, 2 + level * 2));
        String description = alignWords(option.getShortDescription(), MAX_LINE_LENGTH - (6 + level * 2));
        description = spacify(description, 6 + level * 2);
        builder.append(description);
        String typeLines = buildOptionTypeLines(options, option);
        typeLines = alignPiped(typeLines, MAX_LINE_LENGTH - (6 + level * 2));
        typeLines = spacify(typeLines, 6 + level * 2);
        builder.append(typeLines);
        String defaultLines = buildOptionDefaultLines(options, option);
        if (defaultLines != null) {
            defaultLines = alignCharacters(defaultLines, MAX_LINE_LENGTH - (6 + level * 2));
            defaultLines = spacify(defaultLines, 6 + level * 2);
            builder.append(defaultLines);
        }
    }

    private static Map<CommandLineCategory,Set<CommandLineCategory>> buildHierarchy(CommandLineOptions options) {
        assert options != null;
        Map<CommandLineCategory,Set<CommandLineCategory>> result = new LinkedHashMap<>();
        for (CommandLineCategory category : options.getAllCategories().values()) {
            result.put(category, new LinkedHashSet<>());
        }
        for (CommandLineCategory category : options.getAllCategories().values()) {
            CommandLineCategory parent = category.getParent();
            if (parent == null) {
                continue;
            }
            result.get(parent).add(category);
        }
        return result;
    }

    private static Map<CommandLineCategory, Set<CommandLineOption>> buildOptionsByCategory(
            CommandLineOptions options) {
        assert options != null;
        Map<CommandLineCategory, Set<CommandLineOption>> result = new LinkedHashMap<>();
        for (CommandLineOption option : options.getAllOptions().values()) {
            CommandLineCategory category = option.getCategory();
            Set<CommandLineOption> catSet = result.get(category);
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
    private static String buildOptionTypeLines(CommandLineOptions options, CommandLineOption option) {
        assert options != null;
        assert option != null;
        String typeInfo = options.getTypePattern() + COLON + SPACE + option.getTypeInfo() + NEWLINE;
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
    private static String buildOptionDefaultLines(CommandLineOptions options,
            CommandLineOption option) {
        assert options != null;
        assert option != null;
        String defaultValue = option.getDefault();
        String result = null;
        if (defaultValue != null && !defaultValue.equals(EMPTY)) {
            result = options.getDefaultPattern() + COLON + SPACE + defaultValue + SPACE + NEWLINE;
        }
        return result;
    }

    private static String buildOptionTopLine(CommandLineOption option) {
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
    private UsagePrinterJANI() {
    }
}
