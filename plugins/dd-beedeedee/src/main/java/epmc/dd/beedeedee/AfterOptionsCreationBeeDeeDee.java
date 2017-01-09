package epmc.dd.beedeedee;

import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeInteger;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationBeeDeeDee implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-beedeedee";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        Category category = options.addCategory()
        		.setBundleName(OptionsDDBeeDeeDee.OPTIONS_DD_BEEDEEDEE)
        		.setIdentifier(OptionsDDBeeDeeDee.DD_BEEDEEDEE_CATEGORY)
        		.setParent(OptionsDD.DD_CATEGORY)
        		.build();
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        ddLibraryClasses.put(LibraryDDBeeDeeDee.IDENTIFIER, LibraryDDBeeDeeDee.class);
        OptionTypeInteger typeInteger = OptionTypeInteger.getInstance();
        options.addOption().setBundleName(OptionsDDBeeDeeDee.OPTIONS_DD_BEEDEEDEE)
        	.setIdentifier(OptionsDDBeeDeeDee.DD_BEEDEEDEE_INIT_CACHE_SIZE)
        	.setType(typeInteger).setDefault("262144")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDBeeDeeDee.OPTIONS_DD_BEEDEEDEE)
        	.setIdentifier(OptionsDDBeeDeeDee.DD_BEEDEEDEE_INIT_NODES)
        	.setType(typeInteger).setDefault("1000000")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
    }

}
