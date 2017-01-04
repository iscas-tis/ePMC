package epmc.dd.meddly;

import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeInteger;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationMeddly implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-meddly";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        Category category = options.addCategory()
        		.setBundleName(OptionsDDMeddly.OPTIONS_DD_MEDDLY)
        		.setIdentifier(OptionsDDMeddly.DD_MEDDLY_CATEGORY)
        		.setParent(OptionsDD.DD_CATEGORY)
        		.build();
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        ddLibraryClasses.put(LibraryDDMeddly.IDENTIFIER, LibraryDDMeddly.class);
        OptionTypeInteger typeInteger = OptionTypeInteger.getInstance();
        options.addOption().setBundleName(OptionsDDMeddly.OPTIONS_DD_MEDDLY)
        	.setIdentifier(OptionsDDMeddly.DD_MEDDLY_MAX_NUM_VARIABLES)
        	.setType(typeInteger).setDefault("65535")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
    }
}
