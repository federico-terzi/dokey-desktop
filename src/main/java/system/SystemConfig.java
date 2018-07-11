package system;

import app.MainApp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import system.MAC.MACApplicationManager;
import system.startup.MACStartupManager;
import system.MAC.MACSystemManager;
import system.MS.MSApplicationManager;
import system.startup.MSStartupManager;
import system.MS.MSSystemManager;
import system.bookmarks.BookmarkManager;
import system.exceptions.UnsupportedOperatingSystemException;
import system.model.ApplicationManager;
import system.startup.StartupManager;
import system.storage.StorageManager;
import utils.IconManager;
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

//    @Bean
//    public SectionManager sectionManager() {
//        return new SectionManager();
//    }

    @Bean
    public BookmarkManager bookmarkManager() {
        return new BookmarkManager();
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

    @Bean
    public IconManager iconManager() {
        return new IconManager();
    }

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

//    // SEARCH
//    @Bean
//    public SearchEngine searchEngine() throws UnsupportedOperatingSystemException {
//        return new SearchEngine(applicationManager());
//    }
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
