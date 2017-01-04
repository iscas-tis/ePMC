package epmc.webserver.frontend.webtomodelchecker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.ContextExpression;
import epmc.expression.ExpressionParser;
import epmc.expression.UtilExpression;
import epmc.modelchecker.Model;
import epmc.options.OptionsEPMC;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.options.UtilOptionsEPMC;
import epmc.util.Util;
import epmc.value.ContextValue;
import epmc.value.UtilValue;

/**
 *
 * @author ori
 */
public final class WebToModelCheckerConnector {
	
	/**
	 * Provides the list of available options that the user can modify to customize the model checking of a model
	 * @param language for producing localized options (mainly, the description)
	 * @return a list of options in no specific order
	 */
	public static List<EPMCOption> getOptions(String language) {
		List<Map<String, String>> wboptions = UtilOptionsEPMC.getWebOptions(language);
		List<EPMCOption> options = new ArrayList<EPMCOption>(wboptions.size());
		for (Map<String, String> wbopt : wboptions) {
			String name = wbopt.get("name");
			String type = wbopt.get("type");
			String defaultvalue = wbopt.get("default");
			String comment = wbopt.get("comment");
			options.add(new EPMCOption(name, type, defaultvalue, comment));
		}
		return Collections.unmodifiableList(options);
	}
	
	/**
	 * Produces a localized string corresponding to the given key filled with the given arguments
	 * @param locale the locale to use for the translation
	 * @param key the key of the message to localize
	 * @param arguments the arguments of the message to localize
	 * @return the localized string
	 */
	private static String localize(Locale locale, String key, Object[] arguments) {
		ResourceBundle messages = ResourceBundle.getBundle("Error", locale);
		MessageFormat formatter = new MessageFormat(messages.getString(key), locale);
		String message = formatter.format(arguments);
		String format;
		if (key.startsWith("prismParser") || key.startsWith("expressionParser")) {
			if (key.equals("prismParserGeneralError") || key.equals("prismParserInternalError")) {
				format = "{0}";
			} else {
				if (arguments.length == 3) {
					format = messages.getString("parseExceptionFormat");
				} else {
					format = messages.getString("parseExceptionFormatNoIdentifier");
				}
			}
		} else {
			format = "{0}";
		}
		formatter = new MessageFormat(format, locale);
		Object[] extArg = new Object[1 + arguments.length];
		extArg[0] = message;
		System.arraycopy(arguments, 0, extArg, 1, arguments.length);
		return formatter.format(extArg);
	}
	
	/**
	 * Returns the localized human-readable string corresponding to the given key and filled with the arguments
	 * @param language the language to use for the translation
	 * @param key the key of the message to localize
	 * @param arguments the arguments of the message to localize
	 * @return the localized human-readable string
	 */
	public static String getErrorTranslation(String language, String key, String arguments) {
		if (arguments == null) {
			arguments = "";
		}
		Properties args = new Properties();
		try {
			args.load(new StringReader(arguments));
		} catch (IOException ioe) {}
		Object[] extracted = new Object[args.size()];
		int i = 0;
		String arg;
		while ((arg = args.getProperty(String.valueOf(i))) != null) {
			extracted[i] = arg;
			i++;
		}
		return getErrorTranslation(language, key, extracted);
	}
	
	/**
	 * Returns the localized human-readable string corresponding to the given key and filled with the arguments
	 * @param language the language to use for the translation
	 * @param key the key of the message to localize
	 * @param arguments the arguments of the message to localize
	 * @return the localized human-readable string
	 */
	private static String getErrorTranslation(String language, String key, Object[] arguments) {
		Locale locale;
        if (language != null) {
            locale = Locale.forLanguageTag(language);
        } else {
            locale = Locale.ENGLISH;
        }		
		return localize(locale, key, arguments);
	}
	
	/**
	 * Returns a localized ParseResult containing the information relative to the given EPMCException
	 * @param locale the locale to use for the translation
	 * @param ime the exception containing all information
	 * @return the corresponding ParseResult
	 */
	private static ParseResult iscasMcExceptionToParseResult(Locale locale, EPMCException ime) {
//		Object[] old = ime.getArguments();
//		String[] arg = new String[old.length];
//		for (int i = 0; i < old.length; i++) {
//			arg[i] = old[i].toString();
//		}
//		if (old.length == 3) {
//			return new ParseResult(localize(locale, ime.getProblemString(), ime.getArguments()), ime.getProblemString(), arg[0], Integer.valueOf(arg[1]), Integer.valueOf(arg[2]));
//		} else {
//			return new ParseResult(localize(locale, ime.getProblemString(), ime.getArguments()), ime.getProblemString(), arg[0], 0, 0);
//		}
		Positional pos = ime.getPositional();
		return new ParseResult(localize(locale, ime.getProblemIdentifier(), ime.getArguments()), ime.getProblemIdentifier(), null, pos.getLine(), pos.getColumn());
	}
		
	/**
	 * Performs a syntactic check with respect to the given model type of the given model and return the parsing result localized according to the given language
	 * @param language the language to use for the translation
	 * @param modelType the model type
	 * @param model the model to syntactically check
	 * @return the result of the syntactic check
	 */
	public static ParseResult checkParsingModel(String language, String modelType, String model) {
		Locale locale;
        if (language != null) {
            locale = Locale.forLanguageTag(language);
        } else {
            locale = Locale.ENGLISH;
        }
        ParseResult pr;
        Options options = null; // ??
        Map<String,Class<Model>> modelInputMap = options.get(OptionsEPMC.MODEL_CLASS);
        Class<Model> modelClass = modelInputMap.get(options.get(OptionsEPMC.MODEL_INPUT_TYPE));
        InputStream is = new ByteArrayInputStream(model.getBytes());
        try {
            Model modelM = Util.newPluginClassInstance(modelClass);
            modelM.read(is);
            pr = new ParseResult();
        } catch (EPMCException e) {
            pr = iscasMcExceptionToParseResult(locale, e);
        } catch (Throwable e) {
            pr = new ParseResult(localize(locale, "prism-parser-general-error", new Object[] {}), "prism-parser-general-error");
        }
		return pr;
	}
	
	/**
	 * Performs a syntactic check with respect to PRISM of the given model and return the parsing result localized according to the given language
	 * @param modelType the model type
	 * @param model the model to syntactically check
	 * @return the result of the syntactic check
	 */
	public static ParseResult checkParsingModel(String modelType, String model) {
		return checkParsingModel(null, modelType, model);
	}
	
	/**
	 * Performs a syntactic check with respect to the given model type of the given formula and return the parsing result localized according to the given language
	 * @param language the language to use for the translation
	 * @param modelType the model type
	 * @param formulae the list of formulae to syntactically check
	 * @return the result of the syntactic check
	 */
	public static ParseResult checkParsingFormulae(String language, String modelType, List<String> formulae) {
		Locale locale;
        if (language != null) {
            locale = Locale.forLanguageTag(language);
        } else {
			locale = Locale.ENGLISH;
		}
		for (String formula : formulae) {
			try {
			    ContextValue valueContext = UtilValue.newContextValue(UtilOptionsEPMC.newOptions());
			    ContextExpression context = UtilExpression.newContextExpression(valueContext);
			     Options options = context.getOptions();
			     Map<String,Class<Model>> modelInputMap = options.get(OptionsEPMC.MODEL_CLASS);
			     Class<Model> modelClass = modelInputMap.get(options.get(OptionsEPMC.MODEL_INPUT_TYPE));
			     Model modelM = Util.newPluginClassInstance(modelClass);
			     modelM.parse(formula);
			     context.close();
			} catch (EPMCException ime) {
				return iscasMcExceptionToParseResult(locale, ime);
			} catch (Throwable t) {
				//generated by the parser in case of lexical error
				return new ParseResult(localize(locale, "formula-parser-unsupported-format", new Object[]{}), "formula-parser-unsupported-format");
			}
		}
		return new ParseResult();
	}

	/**
	 * Performs a syntactic check with respect to PRISM of the given formula and return the parsing result localized according to the given language
	 * @param language the language to use for the translation
	 * @param formulae the list of formulae to syntactically check
	 * @return the result of the syntactic check
	 */
	public static ParseResult checkParsingFormulae(String language, List<String> formulae) {
		return checkParsingFormulae(language, "prism", formulae);
	}
	
	/**
	 * Extracts and return the labels defined in the given model
	 * @param modelType the model type
	 * @param model the model from which to extract the labels
	 * @return the list of labels
	 */
	/*
	public static List<String> getLabels(String modelType, String model) {
	    try {
	        switch (modelType) {
	        case "prism" : 
	            return new PrismParser(new StringReader(model)).getLabels();
	        case "prism-qmc" : 
	            return new QMCParser(new StringReader(model)).getLabels();
	        default:
	            return new ArrayList<String>();
	        }
	    } catch (Throwable t) {
	        return new ArrayList<>();
	    }
	}
	*/
	
	/**
	 * Extracts and return the labels defined in the given model
	 * @param model the model from which to extract the labels
	 * @return the list of labels
	 */
	/*
	public static List<String> getLabels(String model) {
		return getLabels("prism", model);
	}
	*/
}
