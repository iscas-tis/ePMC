package epmc.dd.cacbdd;

import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeInteger;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationCacBDD implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-cacbdd";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        Category category = options.addCategory()
        		.setBundleName(OptionsDDCacBDD.OPTIONS_DD_CACBDD)
        		.setIdentifier(OptionsDDCacBDD.DD_CACBDD_CATEGORY)
        		.setParent(OptionsDD.DD_CATEGORY)
        		.build();
        
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        ddLibraryClasses.put(LibraryDDCacBDD.IDENTIFIER, LibraryDDCacBDD.class);
        OptionTypeInteger typeInteger = OptionTypeInteger.getInstance();
        options.addOption().setBundleName(OptionsDDCacBDD.OPTIONS_DD_CACBDD)
        	.setIdentifier(OptionsDDCacBDD.DD_CACBDD_MAX_NUM_VARIABLES)
        	.setType(typeInteger).setDefault("65535")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDCacBDD.OPTIONS_DD_CACBDD)
        	.setIdentifier(OptionsDDCacBDD.DD_CACBDD_MAX_CACHE_SIZE)
        	.setType(typeInteger).setDefault("0")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
    }

}
