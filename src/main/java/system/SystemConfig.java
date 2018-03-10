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
import system.MAC.MACApplicationManager;
import system.MAC.MACSystemManager;
import system.MS.MSApplicationManager;
import system.MS.MSSystemManager;
import system.exceptions.UnsupportedOperatingSystemException;
import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.search.agents.ApplicationAgent;
import system.search.agents.CalculatorAgent;
import system.search.agents.GoogleSearchAgent;
import system.search.agents.TerminalAgent;
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
            return new MSApplicationManager(iconManager(), StartupManager.getInstance());
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
}
