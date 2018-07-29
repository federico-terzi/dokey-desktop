package app;

import app.control_panel.ControlPanelStage;
import app.control_panel.layout_editor.LayoutEditorLoader;
import app.control_panel.layout_editor.grid.SectionGrid;
import app.search.stages.SearchStage;
import app.stages.SettingsStage;
import model.parser.component.ComponentParser;
import model.section.Section;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import system.SettingsManager;
import system.SystemConfig;
import system.commands.CommandManager;
import system.image.ImageResolver;
import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.section.SectionManager;
import system.startup.StartupManager;
import system.storage.StorageManager;

import java.io.IOException;
import java.util.Collection;
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

    @Bean
    public TrayIconManager trayIconManager()
    {
        return new TrayIconManager(resourceBundle);
    }

    @Bean
    @Scope("prototype")
    public ControlPanelStage controlPanelStage() throws IOException {
        return new ControlPanelStage(resourceBundle, layoutEditorLoader());
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

    @Bean
    @Scope("prototype")
    public LayoutEditorLoader layoutEditorLoader() throws IOException {
        return new LayoutEditorLoader(sectionManager, imageResolver, resourceBundle, componentParser, commandManager);
    }


}
