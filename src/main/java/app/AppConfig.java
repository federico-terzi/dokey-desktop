package app;

import app.editor.stages.EditorStage;
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

    @Bean
    public TrayIconManager trayIconManager()
    {
        return new TrayIconManager(resourceBundle());
    }

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
    @Scope("prototype")
    public EditorStage editorStage(EditorStage.OnEditorEventListener onEditorEventListener) throws IOException {
        return new EditorStage(applicationManager, sectionManager, shortcutIconManager, webLinkResolver,
                onEditorEventListener, resourceBundle());
    }

    @Bean
    @Scope("prototype")
    public SettingsStage settingsStage(SettingsStage.OnSettingsCloseListener onSettingsCloseListener) throws IOException {
        return new SettingsStage(applicationManager, resourceBundle(), onSettingsCloseListener);
    }
}
