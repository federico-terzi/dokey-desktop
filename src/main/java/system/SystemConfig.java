package system;

import app.MainApp;
import app.search.stages.SearchStage;
import engine.EngineServer;
import engine.EngineService;
import engine.EngineWorker;
import net.DEManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import section.model.ShortcutItem;
import system.MAC.MACApplicationManager;
import system.MAC.MACSystemManager;
import system.MS.MSApplicationManager;
import system.MS.MSSystemManager;
import system.bookmarks.BookmarkManager;
import system.exceptions.UnsupportedOperatingSystemException;
import system.model.ApplicationManager;
import system.quick_commands.QuickCommandManager;
import system.search.SearchEngine;
import system.search.agents.*;
import system.section.SectionInfoResolver;
import system.section.SectionManager;
import utils.IconManager;
import utils.OSValidator;

import java.awt.*;
import java.net.ServerSocket;
import java.net.Socket;
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

    /**
     * @return the correct ApplicationManager instance based on the operating system.
     * @throws UnsupportedOperatingSystemException
     */
    @Bean
    public ApplicationManager applicationManager() throws UnsupportedOperatingSystemException {
        if (OSValidator.isWindows()) {  // WINDOWS
            return new MSApplicationManager(StartupManager.getInstance());
        }else if (OSValidator.isMac()) {  // MAC OSX
            return new MACApplicationManager(StartupManager.getInstance());
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
        return new SectionManager();
    }

    @Bean
    public BookmarkManager bookmarkManager() {
        return new BookmarkManager();
    }

    @Bean
    public SectionInfoResolver sectionInfoResolver() throws UnsupportedOperatingSystemException {
        return new SectionInfoResolver(applicationManager(), resourceBundle());
    }

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
    @Lazy
    public EngineServer engineServer(ServerSocket serverSocket){
        return new EngineServer(serverSocket);
    }

    @Bean
    @Scope("prototype")
    public EngineWorker engineWorker(Socket socket){
        return new EngineWorker(socket);
    }

    @Bean
    @Scope("prototype")
    public EngineService engineService(Socket socket, DEManager.OnConnectionListener onConnectionListener)
            throws UnsupportedOperatingSystemException, AWTException {
        return new EngineService(socket, applicationManager(), applicationSwitchDaemon(),
                systemManager(), activeApplicationsDaemon(), webLinkResolver(), onConnectionListener);
    }

    @Bean
    public IconManager iconManager() {
        return new IconManager();
    }

    @Bean
    public ShortcutIconManager shortcutIconManager() {
        return new ShortcutIconManager();
    }

    @Bean
    public WebLinkResolver webLinkResolver() {
        return new WebLinkResolver(iconManager());
    }

    @Bean
    public DebugManager debugManager() throws UnsupportedOperatingSystemException { return new DebugManager(applicationManager());}

    @Bean
    public QuickCommandManager quickCommandManager() {
        return new QuickCommandManager();
    }

    // SEARCH
    @Bean
    public SearchEngine searchEngine() throws UnsupportedOperatingSystemException {
        return new SearchEngine(applicationManager());
    }

    @Bean
    public ApplicationAgent applicationAgent() throws UnsupportedOperatingSystemException {
        return new ApplicationAgent(searchEngine(), resourceBundle(), applicationManager());
    }

    @Bean
    public BookmarkAgent bookmarkAgent() throws UnsupportedOperatingSystemException {
        return new BookmarkAgent(searchEngine(), resourceBundle(), bookmarkManager(), applicationManager());
    }

    @Bean
    public GoogleSearchAgent googleSearchAgent() throws UnsupportedOperatingSystemException {
        return new GoogleSearchAgent(searchEngine(), resourceBundle(), applicationManager());
    }

    @Bean
    public TerminalAgent terminalAgent() throws UnsupportedOperatingSystemException {
        return new TerminalAgent(searchEngine(), resourceBundle(), applicationManager());
    }

    @Bean
    public CalculatorAgent calculatorAgent() throws UnsupportedOperatingSystemException {
        return new CalculatorAgent(searchEngine(), resourceBundle());
    }

    @Bean
    public DebugAgent debugAgent() throws UnsupportedOperatingSystemException {
        return new DebugAgent(searchEngine(), resourceBundle(), debugManager());
    }

    @Bean
    public ShortcutAgent shortcutAgent() throws UnsupportedOperatingSystemException {
        return new ShortcutAgent(searchEngine(), resourceBundle(), applicationManager(), sectionManager(), sectionInfoResolver());
    }
}
