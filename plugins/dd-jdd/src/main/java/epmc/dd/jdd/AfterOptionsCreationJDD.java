package epmc.dd.jdd;

import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeInteger;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationJDD implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-jdd";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        Category category = options.addCategory()
        		.setBundleName(OptionsDDJDD.DD_JDD_CATEGORY)
        		.setIdentifier(OptionsDDJDD.DD_JDD_CATEGORY)
        		.setParent(OptionsDD.DD_CATEGORY)
        		.build();
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        ddLibraryClasses.put(LibraryDDJDD.IDENTIFIER, LibraryDDJDD.class);
        OptionTypeInteger typeInteger = OptionTypeInteger.getInstance();
        options.addOption().setBundleName(OptionsDDJDD.OPTIONS_DD_JDD)
        	.setIdentifier(OptionsDDJDD.DD_JDD_INIT_CACHE_SIZE)
        	.setType(typeInteger).setDefault("262144")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDJDD.OPTIONS_DD_JDD)
        	.setIdentifier(OptionsDDJDD.DD_JDD_INIT_NODES)
        	.setType(typeInteger).setDefault("1000000")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
    }

}
