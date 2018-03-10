package app;

import app.editor.stages.EditorStage;
import app.search.stages.SearchStage;
import app.stages.SettingsStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import system.ShortcutIconManager;
import system.SystemConfig;
import system.WebLinkResolver;
import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.section.SectionManager;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Spring configuration class
 */
@Configuration
@Import(SystemConfig.class)
public class AppConfig {
    @Autowired private ApplicationManager applicationManager;
    @Autowired private SectionManager sectionManager;
    @Autowired private ShortcutIconManager shortcutIconManager;
    @Autowired private WebLinkResolver webLinkResolver;
    @Autowired private SearchEngine searchEngine;
    @Autowired private ResourceBundle resourceBundle;


    @Bean
    public TrayIconManager trayIconManager()
    {
        return new TrayIconManager(resourceBundle);
    }

    @Bean
    @Scope("prototype")
    public EditorStage editorStage(String targetApp, EditorStage.OnEditorEventListener onEditorEventListener) throws IOException {
        return new EditorStage(applicationManager, sectionManager, shortcutIconManager, webLinkResolver,
                onEditorEventListener, resourceBundle, targetApp);
    }

    @Bean
    @Scope("prototype")
    public SettingsStage settingsStage(SettingsStage.OnSettingsCloseListener onSettingsCloseListener) throws IOException {
        return new SettingsStage(applicationManager, resourceBundle, onSettingsCloseListener);
    }

    @Bean
    @Scope("prototype")
    public SearchStage searchStage() throws IOException {
        return new SearchStage(resourceBundle, searchEngine);
    }
}
