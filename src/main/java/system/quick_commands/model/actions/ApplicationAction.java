package system.quick_commands.model.actions;

import system.model.Application;
import system.quick_commands.model.DependencyResolver;

import java.util.ResourceBundle;

/**
 * This class represents the action of opening/focusing an application.
 */
public class ApplicationAction extends QuickAction{
    private String executablePath;

    public ApplicationAction() {
        super();
        setType(Type.APP);
    }

    @Override
    public boolean executeAction() {
        return resolver.getApplicationManager().openApplication(executablePath);
    }

    @Override
    public String getDisplayText(ResourceBundle resourceBundle) {
        Application application = resolver.getApplicationManager().getApplication(executablePath);

        if (application != null)
            return "Open "+application.getName();  // TODO: i18n

        return "";
    }
}
