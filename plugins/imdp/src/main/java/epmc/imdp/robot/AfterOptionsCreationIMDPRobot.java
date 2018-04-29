package epmc.imdp.robot;

import epmc.imdp.options.OptionsIMDP;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeInteger;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationIMDPRobot implements AfterOptionsCreation {
    public final static String IDENTIFIER = "after-options-creation-imdp-robot";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        OptionTypeMap<Class<?>> modelInputType = options.getType(OptionsModelChecker.MODEL_INPUT_TYPE);
        modelInputType.put(ModelIMDPRobot.IDENTIFIER, ModelIMDPRobot.class);
        
        
        options.addOption().setBundleName(OptionsRobot.OPTIONS_IMDP_ROBOT)
        .setIdentifier(OptionsRobot.IMDP_ROBOT_INITIAL_STATE)
        .setType(OptionTypeInteger.getInstance())
        .setDefault(0)
        .setCommandLine().setGui().setWeb()
        .build();

    }
}
