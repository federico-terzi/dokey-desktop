package engine;

import net.DEDaemon;
import net.LinkManager;
import net.model.KeyboardKeys;
import net.model.RemoteApplication;
import org.jetbrains.annotations.NotNull;
import system.ApplicationManagerFactory;
import system.KeyboardManager;
import system.model.Application;
import system.model.ApplicationManager;

import java.awt.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents the background worker that executes all the actions in the server.
 */
public class EngineService implements LinkManager.OnKeyboardShortcutReceivedListener, LinkManager.OnAppListRequestListener {
    private LinkManager linkManager;
    private ApplicationManager appManager;
    private KeyboardManager keyboardManager;

    public EngineService(LinkManager linkManager, ApplicationManager appManager) throws AWTException {
        this.linkManager = linkManager;
        this.appManager = appManager;
        this.keyboardManager = new KeyboardManager();

        initialization();
    }

    public EngineService(Socket socket, ApplicationManager appManager) throws AWTException {
        // Create a link manager
        this.linkManager = new LinkManager(socket);
        this.appManager = appManager;
        this.keyboardManager = new KeyboardManager();

        initialization();
    }

    /**
     * Do all the initial configuration needed.
     */
    private void initialization() {
        // Set the listeners
        linkManager.setKeyboardShortcutListener(this);
        linkManager.setAppListRequestListener(this);
    }

    /**
     * Start all the services
     */
    public void start() {
        // Start the linkManager daemon
        linkManager.startDaemon();
    }

    /**
     * Set the connection closed listener
     * @param listener
     */
    public void setOnConnectionClosedListener(DEDaemon.OnConnectionClosedListener listener) {
        linkManager.setOnConnectionClosedListener(listener);
    }

    /**
     * EVENTS
     */

    /**
     * Called when receiving a key shortcut request from a client.
     * @param application the application identifier
     * @param keys a list of keys to be pressed
     */
    @Override
    public boolean onKeyboardShortcutReceived(@NotNull String application, @NotNull List<? extends KeyboardKeys> keys) {
        // Focus the application
        boolean res = appManager.openApplication(application);

        // If the app has been focused correctly, send the keystrokes
        if (res) {
            keyboardManager.sendKeystroke(keys);
            return true;
        }

        return false;
    }

    /**
     * Called when receiving a list app request from a client
     * @return a list of RemoteApplication installed in the system.
     */
    @NotNull
    @Override
    public List<RemoteApplication> onAppListRequestReceived() {
        List<RemoteApplication> output = new ArrayList<>();
        List<Application> apps = appManager.getApplicationList();

        for(Application app : apps) {
            RemoteApplication remoteApp = new RemoteApplication();
            remoteApp.setName(app.getName());
            remoteApp.setPath(app.getExecutablePath());
            output.add(remoteApp);
        }

        return output;
    }
}
