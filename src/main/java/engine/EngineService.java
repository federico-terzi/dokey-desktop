package engine;

import app.MainApp;
import json.JSONObject;
import net.DEDaemon;
import net.LinkManager;
import net.model.IconTheme;
import net.model.KeyboardKeys;
import net.model.RemoteApplication;
import net.packets.AppListPacket;
import net.packets.CommandPacket;
import net.packets.SectionPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import section.model.Section;
import section.model.SystemCommands;
import section.model.SystemItem;
import system.*;
import system.model.Application;
import system.model.ApplicationManager;
import system.sicons.ShortcutIcon;
import system.sicons.ShortcutIconManager;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Represents the background worker that executes all the actions in the server.
 */
public class EngineService implements LinkManager.OnKeyboardShortcutReceivedListener, LinkManager.OnAppListRequestListener, ApplicationSwitchDaemon.OnApplicationSwitchListener, LinkManager.OnImageRequestListener, LinkManager.OnAppOpenRequestReceivedListener, LinkManager.OnSectionRequestListener, LinkManager.OnFolderOpenRequestReceivedListener, LinkManager.OnWebLinkRequestReceivedListener, LinkManager.OnCommandRequestReceivedListener, LinkManager.OnAppInfoRequestReceivedListener {
    public static final int DELAY_FROM_FOCUS_TO_KEYSTROKE = 300;  // In milliseconds

    private LinkManager linkManager;
    private ApplicationManager appManager;
    private KeyboardManager keyboardManager;
    private SectionManager sectionManager;
    private ApplicationSwitchDaemon applicationSwitchDaemon;
    private SystemManager systemManager;
    private ActiveApplicationsDaemon activeApplicationsDaemon;
    private ShortcutIconManager shortcutIconManager;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public EngineService(Socket socket, ApplicationManager appManager, ApplicationSwitchDaemon applicationSwitchDaemon,
                         SystemManager systemManager, ActiveApplicationsDaemon activeApplicationsDaemon) throws AWTException {
        // Create a link manager
        this.linkManager = new LinkManager(socket);
        this.appManager = appManager;
        this.applicationSwitchDaemon = applicationSwitchDaemon;
        this.systemManager = systemManager;
        this.activeApplicationsDaemon = activeApplicationsDaemon;
        this.keyboardManager = new KeyboardManager();
        this.sectionManager = new SectionManager();
        this.shortcutIconManager = new ShortcutIconManager();

        initialization();
    }

    /**
     * Do all the initial configuration needed.
     */
    private void initialization() {
        // Set the listeners
        linkManager.setKeyboardShortcutListener(this);
        linkManager.setAppListRequestListener(this);
        linkManager.setImageRequestListener(this);
        linkManager.setAppOpenRequestListener(this);
        linkManager.setSectionRequestListener(this);
        linkManager.setFolderOpenRequestListener(this);
        linkManager.setWebLinkRequestListener(this);
        linkManager.setCommandRequestListener(this);
        linkManager.setAppInfoRequestListener(this);
        applicationSwitchDaemon.addApplicationSwitchListener(this);

        // Register broadcast listeners
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.EDITOR_MODIFIED_SECTION_EVENT, editorSectionModifiedListener);
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

        // Unregister the broadcast listeners
        BroadcastManager.getInstance().unregisterBroadcastListener(BroadcastManager.EDITOR_MODIFIED_SECTION_EVENT, editorSectionModifiedListener);
    }

    /**
     * Set the connection closed listener
     *
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
     *
     * @param application the application identifier. If null it means GLOBAL SHORTCUT
     * @param keys        a list of keys to be pressed
     */
    @Override
    public boolean onKeyboardShortcutReceived(String application, @NotNull List<? extends KeyboardKeys> keys) {
        boolean result = true;  // Initially true for global shortcuts

        // If an application is specified, first focus the application
        if (application != null) {
            // Try to open the application, and get the result
            result = appManager.openApplication(application);
        }

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
     *
     * @param application the path to the application
     * @return true if opened correctly, false otherwise.
     */
    @Override
    public boolean onAppOpenRequestReceived(String application) {
        // Try to open the application
        return appManager.openApplication(application);
    }

    /**
     * Called when receiving a list app request from a client
     *
     * @return a list of RemoteApplication.
     */
    @NotNull
    @Override
    public List<RemoteApplication> onAppListRequestReceived(int requestType) {
        List<RemoteApplication> output = new ArrayList<>();
        List<Application> apps = null;

        // Get the app list based on the request
        if (requestType == AppListPacket.ALL_APPS) {
            apps = appManager.getApplicationList();
        }else if (requestType == AppListPacket.ACTIVE_APPS) {
            apps = activeApplicationsDaemon.getActiveApplications();
        }

        // Convert it to remote applications
        for (Application app : apps) {
            RemoteApplication remoteApp = new RemoteApplication();
            remoteApp.setName(app.getName());
            remoteApp.setPath(app.getExecutablePath());
            output.add(remoteApp);
        }

        return output;
    }

    /**
     * Called when the user switch to another application
     *
     * @param application the current focused application
     */
    @Override
    public void onApplicationSwitch(@NotNull Application application) {
        RemoteApplication remoteApplication = new RemoteApplication();
        remoteApplication.setName(application.getName());
        remoteApplication.setPath(application.getExecutablePath());

        // Load the section associated with the app to obtain the last edit
        Section section = sectionManager.getShortcutSection(remoteApplication.getPath());
        long lastEdit = -1;
        if (section != null) {
            lastEdit = section.getLastEdit();
        }

        linkManager.sendAppSwitchEvent(remoteApplication, lastEdit, new LinkManager.OnAppSwitchAckListener() {
            @Override
            public void onAppSwitchAck() {
                LOG.fine("APP SWITCH ACK "+remoteApplication.getPath());
            }
        });
    }

    /**
     * Called when a user requests an application icon
     *
     * @param path executable path of the requested application
     * @return the icon File if found, null otherwise.
     */
    @Nullable
    @Override
    public File onAppIconRequestReceived(String path) {
        return appManager.getApplicationIcon(path);
    }

    /**
     * Called when a user requests a shortcut icon
     *
     * @param id the shortcut icon id.
     * @return the icon File if found, null otherwise.
     */
    @Nullable
    @Override
    public File onShortcutIconRequestReceived(String id, IconTheme theme) {
        ShortcutIcon shortcutIcon = shortcutIconManager.getIcon(id, theme);

        if (shortcutIcon != null) {
            return shortcutIcon.getFile();
        }

        return null;
    }

    /**
     * Called when a user requests a web link icon.
     * @param url the url of the image
     * @return the icon File if found, null otherwise.
     */
    @Nullable
    @Override
    public File onWebLinkIconRequestReceived(String url) {
        return WebLinkResolver.getImage(url);
    }

    @NotNull
    @Override
    public Section onSectionRequestReceived(String sectionID, long lastEdit) throws SectionPacket.AlreadyUpToDateException, SectionPacket.NotFoundException {
        Section section = null;

        // Generate the section based on the required sectionID
        if (sectionID.equals("launchpad")) {  // LAUNCHPAD
            section = sectionManager.getLaunchpadSection();
        }else if (sectionID.equals("system")) {  // SYSTEM
            section = sectionManager.getSystemSection();
        }else if (sectionID.equals("foremost")){  // SECTION OF THE FOREMOST APP (used at startup)
            // Get the active application
            Application foremostApp = appManager.getActiveApplication();

            // If valid, load the corresponding section
            if (foremostApp != null) {
                section = sectionManager.getShortcutSection(foremostApp.getExecutablePath());
            }
        } else {  // APP SHORTCUT SECTION
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

    /**
     * Called when the user requests to open a folder.
     *
     * @param folderPath path to the folder.
     * @return true if succeeded, false otherwise.
     */
    @Override
    public boolean onFolderOpenRequestReceived(String folderPath) {
        return appManager.openFolder(folderPath);
    }

    /**
     * Called when the user requests to open a web page.
     *
     * @param url the web url to open
     * @return true if succeeded, false otherwise.
     */
    @Override
    public boolean onWebLinkRequestReceived(String url) {
        return appManager.openWebLink(url);
    }

    /**
     * Called when the user modifies a section in the editor.
     */
    private BroadcastManager.BroadcastListener editorSectionModifiedListener = new BroadcastManager.BroadcastListener() {
        @Override
        public void onBroadcastReceived(Serializable param) {
            Section section = Section.fromJson(new JSONObject((String) param));
            linkManager.notifyModifiedSection(section.getStringID(), section, new LinkManager.OnModifiedSectionAckListener() {
                @Override
                public void onModifiedSectionAck() {
                    LOG.fine("EDITOR SECTION MODIFIED ACK");
                }
            });
        }
    };

    /**
     * Called when a user request a specific command.
     * @param command a string containing the command.
     * @return the response or null
     */
    @Nullable
    @Override
    public String onCommandRequestReceived(String command) {
        if (command.equals(SystemCommands.VOLUME_UP.getCommand())) {
            systemManager.volumeUp();
        }else if (command.equals(SystemCommands.VOLUME_DOWN.getCommand())) {
            systemManager.volumeDown();
        }else if (command.equals(SystemCommands.VOLUME_MUTE.getCommand())) {
            systemManager.volumeMute();
        }else if (command.equals(SystemCommands.PLAY_OR_PAUSE.getCommand())) {
            systemManager.playOrPause();
        }else if (command.equals(SystemCommands.NEXT_TRACK.getCommand())) {
            systemManager.nextTrack();
        }else if (command.equals(SystemCommands.PREV_TRACK.getCommand())) {
            systemManager.previousTrack();
        }else if (command.equals(SystemCommands.SHUTDOWN.getCommand())) {
            systemManager.shutdown();
        }else if (command.equals(SystemCommands.SUSPEND.getCommand())) {
            systemManager.suspend();
        }else if (command.equals(SystemCommands.LOGOUT.getCommand())) {
            systemManager.logout();
        }else if (command.equals(SystemCommands.RESTART.getCommand())) {
            systemManager.restart();
        }else if (command.equals("open_editor")) {  // Request to open the editor
            // Send a broadcast event
            BroadcastManager.getInstance().sendBroadcast(BroadcastManager.OPEN_EDITOR_REQUEST_EVENT, null);
            return CommandPacket.RESPONSE_OK;
        }

        return CommandPacket.RESPONSE_ERROR;
    }

    /**
     * Called when a user request information about an app
     * @param appPath the application path
     * @return the Remote application requested or null
     */
    @Nullable
    @Override
    public RemoteApplication onAppInfoRequestReceived(String appPath) {
        Application application = appManager.getApplication(appPath);

        if (application == null)
            return null;

        RemoteApplication remoteApp = new RemoteApplication();
        remoteApp.setName(application.getName());
        remoteApp.setPath(appPath);

        return remoteApp;
    }
}
