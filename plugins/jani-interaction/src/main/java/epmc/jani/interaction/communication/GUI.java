package epmc.jani.interaction.communication;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.options.Options;
import epmc.util.Util;
//import epmc.web.util.GUILabeller;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public final class GUI extends Application {
	private final static String ADDRESS_LOCALHOST = "http://localhost:";
	private final static String MENU_COMMAND = "menu-command";
	private final static String MENU_SERVER = "menu-server";
	private final static String MENU_ITEM_STARTCONNECT = "menu-item-startconnect";
	private final static String MENU_ITEM_STOPDISCONNECT = "menu-item-stopdisconnect";
	private final static String MENU_FILE = "menu-file";
	private final static String MENU_FILE_EXIT = "menu-file-exit";
	private final static String MENU_MODEL = "menu-model";
	private final static String MENU_MODEL_NEW = "menu-model-new";
	private final static String MENU_MODEL_OPEN = "menu-model-open";
	private final static String MENU_MODEL_SAVE = "menu-model-save";
	private final static String MENU_MODEL_SAVE_AS = "menu-model-save-as";
	private final static String MENU_PROPERTIES = "menu-properties";
	private final static String MENU_PROPERTIES_NEW = "menu-properties-new";
	private final static String MENU_PROPERTIES_OPEN = "menu-properties-open";
	private final static String MENU_PROPERTIES_SAVE = "menu-properties-save";
	private final static String MENU_PROPERTIES_SAVE_AS = "menu-properties-save-as";
	private static final String MENU_LOG = "menu-log";
	private static final String MENU_LOG_SAVE = "menu-log-save";
	private static final String MENU_LOG_CLEAR = "menu-log-clear";
	private static final String MENU_OPTIONS = "menu-options";
	private static final String MENU_OPTIONS_OPTIONS = "menu-options-options";
	private final static String REVISION = " revision ";
	private final static String MODEL_CLEAR = "clear()";
	private static final String MENU_COMMAND_STOP = "menu-command-stop";
	private static Options options;
	private WebEngine modelEngine;
	private MenuItem menuServerStartConnect;
	private MenuItem menuServerStopDisconnect;
	private Menu menuCommand;
//	private GUILabeller labeller;

	public static void startGUI(Options options) {
		assert options != null;
		GUI.options = options;
		launch(GUI.class);
	}
	
	@Override
    public void start(Stage stage) {
		assert stage != null;
		String resourceName = options.getResourceFileName();
		Locale locale = options.getLocale();
        ResourceBundle poMsg = ResourceBundle.getBundle(resourceName, locale);
        String toolName = poMsg.getString(Options.TOOL_NAME);
        String revision = Util.getManifestEntry(Util.SCM_REVISION);
        String title = toolName;
        if (revision != null) {
        	title += REVISION + revision;
        }
        stage.setTitle(title);
//        this.labeller = new GUILabeller(options);
        Scene scene = new Scene(new VBox(), 400, 350);
        scene.setFill(Color.OLDLACE);
        WebView webView = new WebView();
        int port = options.getInteger(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_SERVER_PORT);
        webView.getEngine().load(ADDRESS_LOCALHOST + port);
        
 //       MenuBar menuBar = createMenuBar();
  //      menuBar.useSystemMenuBarProperty().set(true);
//        labeller.relabel();
        ((VBox) scene.getRoot()).getChildren().addAll(webView);
//        ((VBox) scene.getRoot()).getChildren().addAll(menuBar, webView);
        List<Object> disableIfRunning = new ArrayList<>();
        disableIfRunning.add(menuServerStartConnect);
        List<Object> disableIfNotRunning = new ArrayList<>();
        disableIfNotRunning.add(menuCommand);
        disableIfNotRunning.add(menuServerStopDisconnect);
        stage.setScene(scene);
        stage.show();
    }

	@Override
	public void stop() throws Exception {
	}

	/*
	private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu menuFile = createMenuFile();
        menuBar.getMenus().add(menuFile);
        Menu menuModel = createMenuModel();
        menuBar.getMenus().add(menuModel);
        Menu menuProperties = createMenuProperties();
        menuBar.getMenus().add(menuProperties);
        this.menuCommand = createMenuCommand();
    	menuBar.getMenus().add(menuCommand);
    	Menu menuServer = createMenuServer();
        menuBar.getMenus().add(menuServer);
        Menu menuLog = createMenuLog();
        menuBar.getMenus().add(menuLog);
        Menu menuOptions = createMenuOptions();
        menuBar.getMenus().add(menuOptions);

        return menuBar;
	}

	private Menu createMenuOptions() {
    	Menu menuOptions = new Menu();
        labeller.put(MENU_OPTIONS, menuOptions);
        MenuItem menuOptionsOptions = new MenuItem();
        menuOptions.getItems().add(menuOptionsOptions);
        labeller.put(MENU_OPTIONS_OPTIONS, menuOptionsOptions);
        return menuOptions;
	}

	private Menu createMenuProperties() {
    	Menu menuProperties = new Menu();
        labeller.put(MENU_PROPERTIES, menuProperties);
        MenuItem menuPropertiesNew = new MenuItem();
        menuProperties.getItems().add(menuPropertiesNew);
        labeller.put(MENU_PROPERTIES_NEW, menuPropertiesNew);
        MenuItem menuPropertiesOpen = new MenuItem();
        menuProperties.getItems().add(menuPropertiesOpen);
        labeller.put(MENU_PROPERTIES_OPEN, menuPropertiesOpen);
        MenuItem menuPropertiesSave = new MenuItem();
        menuProperties.getItems().add(menuPropertiesSave);
        labeller.put(MENU_PROPERTIES_SAVE, menuPropertiesSave);
        MenuItem menuPropertiesSaveAs = new MenuItem();
        menuProperties.getItems().add(menuPropertiesSaveAs);
        labeller.put(MENU_PROPERTIES_SAVE_AS, menuPropertiesSaveAs);

        return menuProperties;
	}

	private Menu createMenuFile() {
    	Menu menuFile = new Menu();
        labeller.put(MENU_FILE, menuFile);
        MenuItem menuFileExit = new MenuItem();
        menuFile.getItems().add(menuFileExit);
        menuFileExit.setOnAction(event -> {
        	System.exit(0);
        });
        labeller.put(MENU_FILE_EXIT, menuFileExit);
        return menuFile;
	}

	private Menu createMenuLog() {
    	Menu menuLog = new Menu();
        labeller.put(MENU_LOG, menuLog);
        MenuItem menuLogSave = new MenuItem();
        labeller.put(MENU_LOG_SAVE, menuLogSave);
        menuLog.getItems().add(menuLogSave);
        MenuItem menuLogClear = new MenuItem();
        labeller.put(MENU_LOG_CLEAR, menuLogClear);
        menuLog.getItems().add(menuLogClear);
		return menuLog;
	}

	private Menu createMenuServer() {
    	Menu menuServer = new Menu();
        labeller.put(MENU_SERVER, menuServer);
        this.menuServerStartConnect = new MenuItem();
        menuServer.getItems().add(menuServerStartConnect);
        labeller.put(MENU_ITEM_STARTCONNECT, menuServerStartConnect);
        this.menuServerStopDisconnect = new MenuItem();
        menuServer.getItems().add(menuServerStopDisconnect);
        labeller.put(MENU_ITEM_STOPDISCONNECT, menuServerStopDisconnect);
    	this.menuServerStartConnect.setDisable(false);
    	this.menuServerStopDisconnect.setDisable(true);
    	return menuServer;
	}

	private Menu createMenuCommand() {
    	Menu menuCommand = new Menu();
    	menuCommand.setDisable(true);
    	labeller.put(MENU_COMMAND, menuCommand);
    	for (Command command : options.getCommands().values()) {
    		if (!command.isGUI()) {
    			continue;
    		}
        	MenuItem menuCommandCommand = new MenuItem();
        	menuCommand.getItems().add(menuCommandCommand);
        	menuCommandCommand.setText(command.getIdentifier());
    	}
        SeparatorMenuItem separator = new SeparatorMenuItem();
        menuCommand.getItems().add(separator);
    	MenuItem menuCommandStop = new MenuItem();
        labeller.put(MENU_COMMAND_STOP, menuCommandStop);
    	menuCommand.getItems().add(menuCommandStop);
    	return menuCommand;
	}

	private Menu createMenuModel() {
        Menu menuFile = new Menu();
        labeller.put(MENU_MODEL, menuFile);
        
        MenuItem menuFileNew = new MenuItem();
        labeller.put(MENU_MODEL_NEW, menuFileNew);
        menuFile.getItems().add(menuFileNew);
        menuFileNew.setOnAction(event -> {
        	modelEngine.executeScript(MODEL_CLEAR);        	
        });
	
        
        MenuItem menuFileOpen = new MenuItem();
        labeller.put(MENU_MODEL_OPEN, menuFileOpen);
        menuFile.getItems().add(menuFileOpen);
        
        MenuItem menuFileSave = new MenuItem();
        labeller.put(MENU_MODEL_SAVE, menuFileSave);
        menuFile.getItems().add(menuFileSave);

        MenuItem menuFileSaveAs = new MenuItem();
        labeller.put(MENU_MODEL_SAVE_AS, menuFileSaveAs);
        menuFile.getItems().add(menuFileSaveAs);

        return menuFile;
	}
*/

}
