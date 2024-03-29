package system;

import app.MainApp;
import model.parser.command.CommandParser;
import model.parser.component.ComponentParser;
import model.parser.component.RuntimeComponentParser;
import model.parser.page.DefaultPageParser;
import model.parser.page.PageParser;
import model.parser.section.DefaultSectionParser;
import model.parser.section.SectionParser;
import net.DEManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import system.applications.MAC.MACApplicationManager;
import system.applications.MS.MSApplicationManager;
import system.bookmarks.BookmarkManager;
import system.commands.CommandEngine;
import system.commands.CommandManager;
import system.commands.CommandTemplateLoader;
import system.commands.exporter.CommandExporter;
import system.commands.importer.CommandImporter;
import system.commands.validator.CommandValidator;
import system.context.GeneralContext;
import system.drag_and_drop.DNDCommandProcessor;
import system.exceptions.UnsupportedOperatingSystemException;
import system.external.photoshop.MAC.MACPhotoshopEngine;
import system.external.photoshop.MS.MSPhotoshopEngine;
import system.external.photoshop.PhotoshopEngine;
import system.external.photoshop.PhotoshopManager;
import system.image.ImageResolver;
import system.internal_ipc.IPCServer;
import system.keyboard.KeyboardManager;
import system.keyboard.MACKeyboardManager;
import system.keyboard.MSKeyboardManager;
import system.applications.ApplicationManager;
import system.commands.parsers.RuntimeCommandParser;
import system.search.SearchEngine;
import system.section.SectionManager;
import system.section.exporter.SectionExporter;
import system.section.importer.SectionImporter;
import system.server.*;
import system.startup.MACStartupManager;
import system.startup.MSStartupManager;
import system.startup.StartupManager;
import system.storage.StorageManager;
import system.system.MACSystemManager;
import system.system.MSSystemManager;
import system.system.SystemManager;
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
    public SectionManager sectionManager() throws UnsupportedOperatingSystemException {
        return new SectionManager(storageManager(), sectionParser(), commandManager(), applicationManager());
    }

    @Bean
    public SectionExporter sectionExporter() {
        return new SectionExporter(commandExporter(), commandManager());
    }

    @Bean
    public SectionImporter sectionImporter() throws UnsupportedOperatingSystemException {
        return new SectionImporter(sectionManager(), sectionParser(), commandImporter(), applicationPathResolver());
    }

    @Bean
    public BookmarkManager bookmarkManager() {
        return new BookmarkManager();
    }

    @Bean
    public IPCServer ipcServer() {
        return new IPCServer();
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
    public CommandValidator commandValidator() {
        return new CommandValidator(generalContext());
    }

    @Bean
    public CommandImporter commandImporter() {
        return new CommandImporter(commandValidator(), commandParser(),
                commandManager());
    }

    @Bean
    public CommandExporter commandExporter() {
        return new CommandExporter();
    }

    @Bean
    public CommandTemplateLoader commandTemplateLoader() {
        return new CommandTemplateLoader(generalContext());
    }

    @Bean
    public ImageResolver imageResolver() {
        return new ImageResolver(generalContext());
    }

    @Bean
    public ApplicationPathResolver applicationPathResolver() throws UnsupportedOperatingSystemException {
        return new ApplicationPathResolver(applicationManager());
    }

    @Bean
    @Lazy
    public PhotoshopEngine photoshopEngine() throws UnsupportedOperatingSystemException {
        if (OSValidator.isWindows()) {  // WINDOWS
            return new MSPhotoshopEngine();
        }else if (OSValidator.isMac()) {  // MAC OSX
            return new MACPhotoshopEngine();
        }
        throw new UnsupportedOperatingSystemException("This Operating system is not supported by Dokey");
    }

    @Bean
    @Lazy
    public PhotoshopManager photoshopManager() throws UnsupportedOperatingSystemException {
        return new PhotoshopManager(photoshopEngine());
    }

    public GeneralContext generalContext() {
        return new GeneralContext() {
            @NotNull
            @Override
            public ApplicationPathResolver getApplicationPathResolver() {
                try {
                    return applicationPathResolver();
                } catch (UnsupportedOperatingSystemException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @NotNull
            @Override
            public SystemManager getSystemManager() {
                try {
                    return systemManager();
                } catch (UnsupportedOperatingSystemException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @NotNull
            @Override
            public ApplicationSwitchDaemon getApplicationSwitchDaemon() {
                try {
                    return applicationSwitchDaemon();
                } catch (UnsupportedOperatingSystemException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @NotNull
            @Override
            public SectionManager getSectionManager() {
                try {
                    return sectionManager();
                } catch (UnsupportedOperatingSystemException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @NotNull
            @Override
            public BookmarkManager getBookmarkManager() {
                return bookmarkManager();
            }

            @NotNull
            @Override
            public ResourceBundle getResourceBundle() {
                return resourceBundle();
            }

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
            public PhotoshopManager getPhotoshopManager() {
                try {
                    return photoshopManager();
                } catch (UnsupportedOperatingSystemException e) {
                    e.printStackTrace();
                }
                return null;
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

    @Bean
    @Lazy
    public MobileServer mobileServer(ServerSocket serverSocket, byte[] key){
        return new MobileServer(serverSocket, key);
    }

    @Bean
    @Scope("prototype")
    public MobileWorker mobileWorker(Socket socket, byte[] key){
        return new MobileWorker(socket, key);
    }

    @Bean
    @Scope("prototype")
    public MobileService mobileService(Socket socket, byte[] key, DEManager.OnConnectionListener onConnectionListener)
            throws UnsupportedOperatingSystemException, AWTException {
        return new MobileService(socket, key, commandManager(), generalContext(), commandEngine(),
                imageResolver(), applicationSwitchDaemon(), sectionManager(), onConnectionListener);
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator(storageManager());
    }

    @Bean
    public HandshakeDataBuilder handshakeDataBuilder() {
        return new HandshakeDataBuilder(keyGenerator());
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
