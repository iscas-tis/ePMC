package epmc.dd.sylvan;

import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeInteger;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationSylvan implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-sylvan";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        Category category = options.addCategory()
        		.setBundleName(OptionsDDSylvan.OPTIONS_DD_SYLVAN)
        		.setIdentifier(OptionsDDSylvan.DD_SYLVAN_CATEGORY)
        		.setParent(OptionsDD.DD_CATEGORY)
        		.build();
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        ddLibraryClasses.put(LibraryDDSylvan.IDENTIFIER, LibraryDDSylvan.class);
        OptionTypeInteger typeInteger = OptionTypeInteger.getInstance();
        options.addOption().setBundleName(OptionsDDSylvan.OPTIONS_DD_SYLVAN)
        	.setIdentifier(OptionsDDSylvan.DD_SYLVAN_WORKERS)
        	.setType(typeInteger).setDefault("1")
        	.setCommandLine().setGui()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDSylvan.OPTIONS_DD_SYLVAN)
        	.setIdentifier(OptionsDDSylvan.DD_SYLVAN_INIT_CACHE_SIZE)
        	.setType(typeInteger).setDefault("262144")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDSylvan.OPTIONS_DD_SYLVAN)
        	.setIdentifier(OptionsDDSylvan.DD_SYLVAN_INIT_NODES)
        	.setType(typeInteger).setDefault("1000000")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDSylvan.OPTIONS_DD_SYLVAN)
        	.setIdentifier(OptionsDDSylvan.DD_SYLVAN_CACHE_GRANULARITY)
        	.setType(typeInteger).setDefault("4")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
    }
}
