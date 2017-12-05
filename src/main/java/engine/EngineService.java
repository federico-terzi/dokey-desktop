package engine;

import net.DEDaemon;
import net.LinkManager;
import net.model.KeyboardKeys;
import net.model.RemoteApplication;
import net.packets.SectionPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import section.model.Section;
import system.ApplicationSwitchDaemon;
import system.KeyboardManager;
import system.SectionManager;
import system.model.Application;
import system.model.ApplicationManager;

import java.awt.*;
import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents the background worker that executes all the actions in the server.
 */
public class EngineService implements LinkManager.OnKeyboardShortcutReceivedListener, LinkManager.OnAppListRequestListener, ApplicationSwitchDaemon.OnApplicationSwitchListener, LinkManager.OnAppIconRequestListener, LinkManager.OnAppOpenRequestReceivedListener, LinkManager.OnSectionRequestListener {
    public static final int DELAY_FROM_FOCUS_TO_KEYSTROKE = 300;  // In milliseconds

    private LinkManager linkManager;
    private ApplicationManager appManager;
    private KeyboardManager keyboardManager;
    private SectionManager sectionManager;
    private ApplicationSwitchDaemon applicationSwitchDaemon;

    public EngineService(LinkManager linkManager, ApplicationManager appManager, ApplicationSwitchDaemon applicationSwitchDaemon) throws AWTException {
        this.linkManager = linkManager;
        this.appManager = appManager;
        this.applicationSwitchDaemon = applicationSwitchDaemon;
        this.keyboardManager = new KeyboardManager();
        this.sectionManager = new SectionManager();

        initialization();
    }

    public EngineService(Socket socket, ApplicationManager appManager, ApplicationSwitchDaemon applicationSwitchDaemon) throws AWTException {
        // Create a link manager
        this.linkManager = new LinkManager(socket);
        this.appManager = appManager;
        this.applicationSwitchDaemon = applicationSwitchDaemon;
        this.keyboardManager = new KeyboardManager();
        this.sectionManager = new SectionManager();

        initialization();
    }

    /**
     * Do all the initial configuration needed.
     */
    private void initialization() {
        // Set the listeners
        linkManager.setKeyboardShortcutListener(this);
        linkManager.setAppListRequestListener(this);
        linkManager.setAppIconRequestListener(this);
        linkManager.setAppOpenRequestListener(this);
        linkManager.setSectionRequestListener(this);
        applicationSwitchDaemon.addApplicationSwitchListener(this);
    }

    /**
     * Start all the services
     */
    public void start() {
        // Start the linkManager daemon
        linkManager.startDaemon();
    }

    /**
     * Close gracefully and unregister from listeners
     */
    public void close() {
        // Stop the linkManager
        linkManager.stopDaemon();

        // Remove the listeners
        applicationSwitchDaemon.removeApplicationSwitchListener(this);
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
        // Try to open the application, and get the result
        boolean result = appManager.openApplication(application);

        // TODO: before sending the keystrokes, make sure the requested app is in focus.

        // The app was already focused, send the keystrokes directly
        if (result) {
            keyboardManager.sendKeystroke(keys);
            return true;
        }

        // Error
        return false;
    }

    /**
     * Called when receiving a request to focus/open an application.
     * @param application the path to the application
     * @return true if opened correctly, false otherwise.
     */
    @Override
    public boolean onAppOpenRequestReceived(String application) {
        System.out.println("AppOpenRequest");
        System.out.println(appManager.getActiveApplication());
        System.out.println(appManager.getActivePID());
        // Try to open the application
        boolean res = appManager.openApplication(application);
        System.out.println("Requested open");
        System.out.println(appManager.getActiveApplication());
        System.out.println(appManager.getActivePID());

        return res;
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

    /**
     * Called when the user switch to another application
     * @param application the current focused application
     */
    @Override
    public void onApplicationSwitch(@NotNull Application application) {
        RemoteApplication remoteApplication = new RemoteApplication();
        remoteApplication.setName(application.getName());
        remoteApplication.setPath(application.getExecutablePath());

        // TODO: section logic

        linkManager.sendAppSwitchEvent(remoteApplication, -1, new LinkManager.OnAppSwitchAckListener() {
            @Override
            public void onAppSwitchAck() {
                System.out.println("App Switch Event Received");
            }
        });
    }

    /**
     * Called when a user requests an application icon
     * @param path
     * @return
     */
    @Nullable
    @Override
    public File onAppIconRequestReceived(String path) {
        return appManager.getApplicationIcon(path);
    }

    @NotNull
    @Override
    public Section onSectionRequestReceived(String sectionID, long lastEdit) throws SectionPacket.AlreadyUpToDateException, SectionPacket.NotFoundException {
        Section section = null;

        // Generate the section based on the required sectionID
        if (sectionID.equals("launchpad")) {  // LAUNCHPAD
            section = sectionManager.getLaunchpadSection();
        }else{  // APP SHORTCUT SECTION
            section = sectionManager.getShortcutSection(sectionID);
        }

        // If the section couldn't be found
        if (section == null) {
            throw new SectionPacket.NotFoundException("Required section was not found");
        }

        // If the requested section is already up to date
        if (section.getLastEdit() <= lastEdit) {
            throw new SectionPacket.AlreadyUpToDateException("Requested section is already up to date");
        }

        return section;
    }
}
