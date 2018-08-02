package system;

import app.MainApp;
import model.parser.command.CommandParser;
import model.parser.component.ComponentParser;
import model.parser.component.RuntimeComponentParser;
import model.parser.page.DefaultPageParser;
import model.parser.page.PageParser;
import model.parser.section.DefaultSectionParser;
import model.parser.section.SectionParser;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import system.applications.MAC.MACApplicationManager;
import system.applications.MS.MSApplicationManager;
import system.bookmarks.BookmarkManager;
import system.commands.CommandEngine;
import system.commands.CommandManager;
import system.commands.CommandTemplateLoader;
import system.context.GeneralContext;
import system.drag_and_drop.DNDCommandProcessor;
import system.exceptions.UnsupportedOperatingSystemException;
import system.image.ImageResolver;
import system.keyboard.KeyboardManager;
import system.keyboard.MACKeyboardManager;
import system.keyboard.MSKeyboardManager;
import system.model.ApplicationManager;
import system.parsers.RuntimeCommandParser;
import system.search.SearchEngine;
import system.section.SectionManager;
import system.startup.MACStartupManager;
import system.startup.MSStartupManager;
import system.startup.StartupManager;
import system.storage.StorageManager;
import system.system.MACSystemManager;
import system.system.MSSystemManager;
import system.system.SystemManager;
import utils.OSValidator;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Spring configuration class
 */
@Configuration
public class SystemConfig {
    @Bean
    public ResourceBundle resourceBundle() {
        // Try to load the correct locale, if not found, fallback to English.
        try {
            return ResourceBundle.getBundle("lang.dokey", MainApp.locale);
        }catch (MissingResourceException e) {
            return ResourceBundle.getBundle("lang.dokey", Locale.ENGLISH); // Default fallback
        }
    }

    @Bean
    public StorageManager storageManager() {
        return StorageManager.getDefault();
    }

    /**
     * @return the correct ApplicationManager instance based on the operating system.
     * @throws UnsupportedOperatingSystemException
     */
    @Bean
    public ApplicationManager applicationManager() throws UnsupportedOperatingSystemException {
        if (OSValidator.isWindows()) {  // WINDOWS
            return new MSApplicationManager(storageManager(), startupManager());
        }else if (OSValidator.isMac()) {  // MAC OSX
            return new MACApplicationManager(storageManager(), startupManager());
        }
        throw new UnsupportedOperatingSystemException("This Operating system is not supported by Dokey");
    }

    /**
     * @return the correct StartupManager instance based on the operating system.
     * @throws UnsupportedOperatingSystemException
     */
    @Bean
    public StartupManager startupManager() throws UnsupportedOperatingSystemException {
        if (OSValidator.isWindows()) {  // WINDOWS
            return new MSStartupManager(storageManager());
        }else if (OSValidator.isMac()) {  // MAC OSX
            return new MACStartupManager(storageManager());
        }
        throw new UnsupportedOperatingSystemException("This Operating system is not supported by Dokey");
    }

    /**
     * @return the correct KeyboardManager instance based on the operating system.
     * @throws UnsupportedOperatingSystemException
     */
    @Bean
    public KeyboardManager keyboardManager() throws UnsupportedOperatingSystemException {
        if (OSValidator.isWindows()) {  // WINDOWS
            return new MSKeyboardManager();
        }else if (OSValidator.isMac()) {  // MAC OSX
            return new MACKeyboardManager();
        }
        throw new UnsupportedOperatingSystemException("This Operating system is not supported by Dokey");
    }

    @Bean
    public DaemonMonitor daemonMonitor() {
        return new DaemonMonitor();
    }

    @Bean
    public ApplicationSwitchDaemon applicationSwitchDaemon() throws UnsupportedOperatingSystemException {
        return new ApplicationSwitchDaemon(applicationManager(), daemonMonitor());
    }

    @Bean
    public ActiveApplicationsDaemon activeApplicationsDaemon() throws UnsupportedOperatingSystemException {
        return new ActiveApplicationsDaemon(applicationManager(), daemonMonitor());
    }

    @Bean
    public SectionManager sectionManager() {
        return new SectionManager(storageManager(), sectionParser(), commandManager());
    }

    @Bean
    public BookmarkManager bookmarkManager() {
        return new BookmarkManager();
    }

    @Bean
    public DNDCommandProcessor dndCommandProcessor() {
        return new DNDCommandProcessor(commandManager());
    }

//    @Bean
//    public SectionInfoResolver sectionInfoResolver() throws UnsupportedOperatingSystemException {
//        return new SectionInfoResolver(applicationManager(), resourceBundle());
//    }

    /**
     * @return the correct SystemManager instance based on the operating system.
     * @throws UnsupportedOperatingSystemException
     */
    @Bean
    public SystemManager systemManager() throws UnsupportedOperatingSystemException {
        if (OSValidator.isWindows()) {  // WINDOWS
            return new MSSystemManager();
        }else if (OSValidator.isMac()) {  // MAC OSX
            return new MACSystemManager();
        }
        throw new UnsupportedOperatingSystemException("This Operating system is not supported by Dokey");
    }

    @Bean
    public CommandEngine commandEngine() {
        return new CommandEngine(generalContext());
    }

    @Bean
    public CommandParser commandParser() {
        return new RuntimeCommandParser();
    }

    @Bean
    public ComponentParser componentParser() {
        return new RuntimeComponentParser(commandManager());
    }

    @Bean
    public PageParser pageParser() {
        return new DefaultPageParser(componentParser());
    }

    @Bean
    public SectionParser sectionParser() {
        return new DefaultSectionParser(pageParser());
    }

    @Bean
    public CommandManager commandManager() {
        return new CommandManager(commandParser(), storageManager(), commandTemplateLoader());
    }

    @Bean
    public CommandTemplateLoader commandTemplateLoader() {
        return new CommandTemplateLoader(generalContext());
    }

    @Bean
    public ImageResolver imageResolver() {
        return new ImageResolver(generalContext());
    }

    public GeneralContext generalContext() {
        return new GeneralContext() {
            @NotNull
            @Override
            public KeyboardManager getKeyboardManager() {
                try {
                    return keyboardManager();
                } catch (UnsupportedOperatingSystemException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @NotNull
            @Override
            public StorageManager getStorageManager() {
                return storageManager();
            }

            @NotNull
            @Override
            public CommandParser getCommandParser() {
                return commandParser();
            }

            @NotNull
            @Override
            public ApplicationManager getApplicationManager() {
                try {
                    return applicationManager();
                } catch (UnsupportedOperatingSystemException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @NotNull
            @Override
            public CommandManager getCommandManager() {
                return commandManager();
            }

            @NotNull
            @Override
            public CommandEngine getCommandEngine() {
                return commandEngine();
            }
        };
    }

//    @Bean
//    @Lazy
//    public EngineServer engineServer(ServerSocket serverSocket){
//        return new EngineServer(serverSocket);
//    }
//
//    @Bean
//    @Scope("prototype")
//    public EngineWorker engineWorker(Socket socket){
//        return new EngineWorker(socket);
//    }
//
//    @Bean
//    @Scope("prototype")
//    public EngineService engineService(Socket socket, DEManager.OnConnectionListener onConnectionListener)
//            throws UnsupportedOperatingSystemException, AWTException {
//        return new EngineService(socket, applicationManager(), applicationSwitchDaemon(),
//                systemManager(), activeApplicationsDaemon(), webLinkResolver(), onConnectionListener);
//    }

//    @Bean
//    public ShortcutIconManager shortcutIconManager() {
//        return new ShortcutIconManager();
//    }
//
//    @Bean
//    public WebLinkResolver webLinkResolver() {
//        return new WebLinkResolver(iconManager());
//    }

    @Bean
    public DebugManager debugManager() throws UnsupportedOperatingSystemException { return new DebugManager(applicationManager(), storageManager());}

    @Bean
    public SettingsManager settingsManager() {
        return new SettingsManager(storageManager());
    }

//    @Bean
//    public DependencyResolver dependencyResolver() {
//        return new DependencyResolver() {
//            @Override
//            public ApplicationManager getApplicationManager() {
//                try {
//                    return applicationManager();
//                } catch (UnsupportedOperatingSystemException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            public WebLinkResolver getWebLinkResolver() {
//                return webLinkResolver();
//            }
//        };
//    }

    // SEARCH
    @Bean
    public SearchEngine searchEngine() throws UnsupportedOperatingSystemException {
        return new SearchEngine(applicationManager(), generalContext());
    }
//
//    @Bean
//    public ApplicationAgent applicationAgent() throws UnsupportedOperatingSystemException {
//        return new ApplicationAgent(searchEngine(), resourceBundle(), applicationManager());
//    }
//
//    @Bean
//    public BookmarkAgent bookmarkAgent() throws UnsupportedOperatingSystemException {
//        return new BookmarkAgent(searchEngine(), resourceBundle(), bookmarkManager(), applicationManager());
//    }
//
//    @Bean
//    public GoogleSearchAgent googleSearchAgent() throws UnsupportedOperatingSystemException {
//        return new GoogleSearchAgent(searchEngine(), resourceBundle(), applicationManager());
//    }
//
//    @Bean
//    public TerminalAgent terminalAgent() throws UnsupportedOperatingSystemException {
//        return new TerminalAgent(searchEngine(), resourceBundle(), applicationManager());
//    }
//
//    @Bean
//    public CalculatorAgent calculatorAgent() throws UnsupportedOperatingSystemException {
//        return new CalculatorAgent(searchEngine(), resourceBundle());
//    }
//
//    @Bean
//    public DebugAgent debugAgent() throws UnsupportedOperatingSystemException {
//        return new DebugAgent(searchEngine(), resourceBundle(), debugManager());
//    }
//
//    @Bean
//    public ShortcutAgent shortcutAgent() throws UnsupportedOperatingSystemException {
//        return new ShortcutAgent(searchEngine(), resourceBundle(), applicationManager(), sectionManager(), sectionInfoResolver());
//    }
//
//    @Bean
//    public QuickCommandAgent quickCommandAgent() throws UnsupportedOperatingSystemException {
//        return new QuickCommandAgent(searchEngine(), resourceBundle(), quickCommandManager(), dependencyResolver());
//    }
//
//    @Bean
//    public AddUrlToQuickCommandsAgent addUrlToQuickCommandsAgent() throws UnsupportedOperatingSystemException {
//        return new AddUrlToQuickCommandsAgent(searchEngine(), resourceBundle());
//    }
}
