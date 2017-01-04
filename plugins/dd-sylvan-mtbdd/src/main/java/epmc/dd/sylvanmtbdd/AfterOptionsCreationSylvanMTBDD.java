package epmc.dd.sylvanmtbdd;

import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeInteger;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationSylvanMTBDD implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-sylvanmtbdd";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        Category category = options.addCategory()
        		.setBundleName(OptionsDDSylvanMTBDD.OPTIONS_DD_SYLVAN_MTBDD)
        		.setIdentifier(OptionsDDSylvanMTBDD.DD_SYLVAN_MTBDD_CATEGORY)
        		.setParent(OptionsDD.DD_CATEGORY)
        		.build();
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_MT_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        ddLibraryClasses.put(LibraryDDSylvanMTBDD.IDENTIFIER, LibraryDDSylvanMTBDD.class);
        OptionTypeInteger typeInteger = OptionTypeInteger.getInstance();
        options.addOption().setBundleName(OptionsDDSylvanMTBDD.OPTIONS_DD_SYLVAN_MTBDD)
        	.setIdentifier(OptionsDDSylvanMTBDD.DD_SYLVAN_MTBDD_WORKERS)
        	.setType(typeInteger).setDefault("1")
        	.setCommandLine().setGui()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDSylvanMTBDD.OPTIONS_DD_SYLVAN_MTBDD)
        	.setIdentifier(OptionsDDSylvanMTBDD.DD_SYLVAN_MTBDD_INIT_CACHE_SIZE)
        	.setType(typeInteger).setDefault("262144")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDSylvanMTBDD.OPTIONS_DD_SYLVAN_MTBDD)
        	.setIdentifier(OptionsDDSylvanMTBDD.DD_SYLVAN_MTBDD_INIT_NODES)
        	.setType(typeInteger).setDefault("1000000")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDSylvanMTBDD.OPTIONS_DD_SYLVAN_MTBDD)
        	.setIdentifier(OptionsDDSylvanMTBDD.DD_SYLVAN_MTBDD_CACHE_GRANULARITY)
        	.setType(typeInteger).setDefault("4")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
    }
}
