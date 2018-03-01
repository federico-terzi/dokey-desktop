package system;

import engine.EngineServer;
import engine.EngineService;
import engine.EngineWorker;
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
import system.section.SectionManager;
import utils.IconManager;
import utils.OSValidator;

import java.awt.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Spring configuration class
 */
@Configuration
public class SystemConfig {
    /**
     * @return the correct ApplicationManager instance based on the operating system.
     * @throws UnsupportedOperatingSystemException
     */
    @Bean
    public ApplicationManager applicationManager() throws UnsupportedOperatingSystemException {
        if (OSValidator.isWindows()) {  // WINDOWS
            return new MSApplicationManager(iconManager());
        }else if (OSValidator.isMac()) {  // MAC OSX
            return new MACApplicationManager();
        }
        throw new UnsupportedOperatingSystemException("This Operating system is not supported by Dokey");
    }

    @Bean
    public ApplicationSwitchDaemon applicationSwitchDaemon() throws UnsupportedOperatingSystemException {
        return new ApplicationSwitchDaemon(applicationManager());
    }

    @Bean
    public ActiveApplicationsDaemon activeApplicationsDaemon() throws UnsupportedOperatingSystemException {
        return new ActiveApplicationsDaemon(applicationManager());
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
    public EngineService engineService(Socket socket) throws UnsupportedOperatingSystemException, AWTException {
        return new EngineService(socket, applicationManager(), applicationSwitchDaemon(),
                systemManager(), activeApplicationsDaemon(), webLinkResolver());
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
}
