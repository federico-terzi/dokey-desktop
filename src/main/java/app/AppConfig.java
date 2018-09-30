package app;

import app.control_panel.ControlPanelStage;
import app.control_panel.appearance.position.PositionResolver;
import app.control_panel.appearance.position.MACPositionResolver;
import app.control_panel.appearance.position.MSPositionResolver;
import app.search.stages.SearchStage;
import app.tray_icon.MSTrayIconManager;
import app.tray_icon.MacTrayIconManager;
import app.tray_icon.TrayIconManager;
import model.parser.component.ComponentParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import system.SettingsManager;
import system.SystemConfig;
import system.commands.CommandManager;
import system.commands.exporter.CommandExporter;
import system.commands.importer.CommandImporter;
import system.drag_and_drop.DNDCommandProcessor;
import system.exceptions.UnsupportedOperatingSystemException;
import system.image.ImageResolver;
import system.applications.ApplicationManager;
import system.search.SearchEngine;
import system.section.SectionManager;
import system.section.exporter.SectionExporter;
import system.section.importer.SectionImporter;
import system.server.HandshakeDataBuilder;
import system.startup.StartupManager;
import system.storage.StorageManager;
import utils.OSValidator;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Spring configuration class
 */
@Configuration
@Import(SystemConfig.class)
public class AppConfig {
    @Autowired private ApplicationManager applicationManager;
//    @Autowired private SectionManager sectionManager;
//    @Autowired private ShortcutIconManager shortcutIconManager;
//    @Autowired private WebLinkResolver webLinkResolver;
    @Autowired private SearchEngine searchEngine;
    @Autowired private ResourceBundle resourceBundle;
//    @Autowired private QuickCommandManager quickCommandManager;
//    @Autowired private DependencyResolver dependencyResolver;
    @Autowired private SettingsManager settingsManager;
    @Autowired private StartupManager startupManager;
    @Autowired private StorageManager storageManager;
    @Autowired private ImageResolver imageResolver;
    @Autowired private SectionManager sectionManager;
    @Autowired private SectionExporter sectionExporter;
    @Autowired private SectionImporter sectionImporter;
    @Autowired private CommandManager commandManager;
    @Autowired private CommandExporter commandExporter;
    @Autowired private CommandImporter commandImporter;
    @Autowired private ComponentParser componentParser;
    @Autowired private DNDCommandProcessor dndCommandProcessor;
    @Autowired private HandshakeDataBuilder handshakeDataBuilder;

    @Bean
    public TrayIconManager trayIconManager() throws UnsupportedOperatingSystemException {
        if (OSValidator.isMac()) {
            return new MacTrayIconManager(resourceBundle);
        }else if (OSValidator.isWindows()) {
            return new MSTrayIconManager(resourceBundle);
        }
        throw new UnsupportedOperatingSystemException("This Operating system is not supported by Dokey");
    }

    @Bean
    public PositionResolver appearanceManager() throws UnsupportedOperatingSystemException {
        if (OSValidator.isMac()) {
            return new MACPositionResolver(trayIconManager());
        }else if (OSValidator.isWindows()) {
            return new MSPositionResolver(trayIconManager());
        }
        throw new UnsupportedOperatingSystemException("This Operating system is not supported by Dokey");
    }

    @Bean
    @Lazy
    public ControlPanelStage controlPanelStage() throws IOException, UnsupportedOperatingSystemException {
        return new ControlPanelStage(sectionManager, imageResolver, resourceBundle, componentParser, commandManager,
                applicationManager,handshakeDataBuilder, dndCommandProcessor, settingsManager, startupManager,
                storageManager, commandExporter, commandImporter, sectionExporter, sectionImporter);
    }

//    @Bean
//    @Scope("prototype")
//    public CommandEditorStage commandEditorStage(CommandEditorStage.OnCommandEditorCloseListener onCommandEditorCloseListener) throws IOException {
//        return new CommandEditorStage(quickCommandManager, resourceBundle, applicationManager, dependencyResolver, onCommandEditorCloseListener);
//    }

    @Bean
    @Scope("prototype")
    public SearchStage searchStage() throws IOException {
        return new SearchStage(resourceBundle, searchEngine, imageResolver);
    }

}
