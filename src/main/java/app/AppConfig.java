package app;

import app.control_panel.ControlPanelStage;
import app.control_panel.layout_editor.appearance.AppearanceManager;
import app.control_panel.layout_editor.appearance.MACAppearanceManager;
import app.control_panel.layout_editor.appearance.MSAppearanceManager;
import app.search.stages.SearchStage;
import app.stages.SettingsStage;
import app.tray_icon.MSTrayIconManager;
import app.tray_icon.MacTrayIconManager;
import app.tray_icon.TrayIconManager;
import model.parser.component.ComponentParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import system.SettingsManager;
import system.SystemConfig;
import system.commands.CommandManager;
import system.drag_and_drop.DNDCommandProcessor;
import system.exceptions.UnsupportedOperatingSystemException;
import system.image.ImageResolver;
import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.section.SectionManager;
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
    @Autowired private CommandManager commandManager;
    @Autowired private ComponentParser componentParser;
    @Autowired private DNDCommandProcessor dndCommandProcessor;

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
    public AppearanceManager appearanceManager() throws UnsupportedOperatingSystemException {
        if (OSValidator.isMac()) {
            return new MACAppearanceManager(trayIconManager());
        }else if (OSValidator.isWindows()) {
            return new MSAppearanceManager(trayIconManager());
        }
        throw new UnsupportedOperatingSystemException("This Operating system is not supported by Dokey");
    }

    @Bean
    @Lazy
    public ControlPanelStage controlPanelStage() throws IOException, UnsupportedOperatingSystemException {
        return new ControlPanelStage(sectionManager, imageResolver, resourceBundle, componentParser, commandManager,
                applicationManager, dndCommandProcessor, appearanceManager());
    }

    @Bean
    @Scope("prototype")
    public SettingsStage settingsStage(SettingsStage.OnSettingsCloseListener onSettingsCloseListener) throws IOException {
        return new SettingsStage(applicationManager, resourceBundle, settingsManager, startupManager, storageManager, onSettingsCloseListener);
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
