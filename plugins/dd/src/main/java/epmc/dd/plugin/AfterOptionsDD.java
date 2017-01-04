package epmc.dd.plugin;

import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public final class AfterOptionsDD implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-dd";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
        assert options != null;
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = new OrderedMap<>();
        OptionTypeMap<?> ddBinaryEngineType = new OptionTypeMap<>(ddLibraryClasses);
        Category category = options.addCategory().setBundleName(OptionsDD.OPTIONS_DD)
                .setIdentifier(OptionsDD.DD_CATEGORY)
            .   build();
        options.set(OptionsDD.DD_LIBRARY_CLASS, ddLibraryClasses);
        options.addOption().setBundleName(OptionsDD.OPTIONS_DD)
            .setIdentifier(OptionsDD.DD_BINARY_ENGINE)
            .setType(ddBinaryEngineType)
            .setCommandLine().setGui().setWeb()
            .setCategory(category).build();
        Map<String,Class<? extends LibraryDD>> ddMtLibraryClasses = new OrderedMap<>();
        OptionTypeMap<?> ddMultiEngineType = new OptionTypeMap<>(ddMtLibraryClasses);
        options.set(OptionsDD.DD_MT_LIBRARY_CLASS, ddMtLibraryClasses);
        options.addOption().setBundleName(OptionsDD.OPTIONS_DD)
            .setIdentifier(OptionsDD.DD_MULTI_ENGINE)
            .setType(ddMultiEngineType)
            .setCommandLine().setGui().setWeb()
            .setCategory(category).build();
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsDD.OPTIONS_DD)
            .setIdentifier(OptionsDD.DD_DEBUG)
            .setType(typeBoolean)
            .setCommandLine().setGui()
            .setCategory(category).build();
        options.addOption().setBundleName(OptionsDD.OPTIONS_DD)
            .setIdentifier(OptionsDD.DD_AND_EXIST)
            .setType(typeBoolean)
            .setCommandLine().setGui().setWeb()
            .setCategory(category).build();
        options.addOption().setBundleName(OptionsDD.OPTIONS_DD)
            .setIdentifier(OptionsDD.DD_LEAK_CHECK)
            .setType(typeBoolean)
            .setCommandLine().setGui()
            .setCategory(category).build();
	}

}
