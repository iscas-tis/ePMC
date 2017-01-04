package epmc.dd.buddy;

import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeInteger;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationBuDDy implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-buddy";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        Category category = options.addCategory()
        		.setBundleName(OptionsDDBuDDy.OPTIONS_DD_BUDDY)
        		.setIdentifier(OptionsDDBuDDy.DD_BUDDY_CATEGORY)
        		.setParent(OptionsDD.DD_CATEGORY)
        		.build();
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        ddLibraryClasses.put(LibraryDDBuDDy.IDENTIFIER, LibraryDDBuDDy.class);
        OptionTypeInteger typeInteger = OptionTypeInteger.getInstance();
        options.addOption().setBundleName(OptionsDDBuDDy.OPTIONS_DD_BUDDY)
        	.setIdentifier(OptionsDDBuDDy.DD_BUDDY_INIT_CACHE_SIZE)
        	.setType(typeInteger).setDefault("262144")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDBuDDy.OPTIONS_DD_BUDDY)
        	.setIdentifier(OptionsDDBuDDy.DD_BUDDY_INIT_NODES)
        	.setType(typeInteger).setDefault("1000000")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDBuDDy.OPTIONS_DD_BUDDY)
        	.setIdentifier(OptionsDDBuDDy.DD_BUDDY_CACHE_RATIO)
        	.setType(typeInteger).setDefault("0")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDBuDDy.OPTIONS_DD_BUDDY)
        	.setIdentifier(OptionsDDBuDDy.DD_BUDDY_MAX_INCREASE)
        	.setType(typeInteger).setDefault("50000")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDBuDDy.OPTIONS_DD_BUDDY)
        	.setIdentifier(OptionsDDBuDDy.DD_BUDDY_MAX_NODE_NUM)
        	.setType(typeInteger).setDefault("0")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDBuDDy.OPTIONS_DD_BUDDY)
        	.setIdentifier(OptionsDDBuDDy.DD_BUDDY_MIN_FREE_NODES)
        	.setType(typeInteger).setDefault("20")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsDDBuDDy.OPTIONS_DD_BUDDY)
        	.setIdentifier(OptionsDDBuDDy.DD_BUDDY_INIT_VARNUM)
        	.setType(typeInteger).setDefault("0")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
    }

}
