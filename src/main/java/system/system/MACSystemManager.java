package system.system;

import system.ResourceUtils;
import system.keyboard.bindings.MacKeyboardLib;
import system.system.SystemManager;

import java.io.IOException;

public class MACSystemManager extends SystemManager {
    private static final int VOLUME_STEP = 20;

    @Override
    public boolean shutdownPC() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"osascript", "-e", "tell app \"System Events\" to shut down"});
            proc.waitFor();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean restart() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"osascript", "-e", "tell app \"System Events\" to restart"});
            proc.waitFor();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean logout() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"osascript", "-e", "tell app \"System Events\" to log out"});
            proc.waitFor();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean suspend() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"osascript", "-e", "tell app \"System Events\" to sleep"});
            proc.waitFor();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean volumeDown() {
        MacKeyboardLib.INSTANCE.simulateMediaKey(MacKeyboardLib.NX_KEYTYPE_SOUND_DOWN);
        return true;
    }

    @Override
    public boolean volumeUp() {
        MacKeyboardLib.INSTANCE.simulateMediaKey(MacKeyboardLib.NX_KEYTYPE_SOUND_UP);
        return true;
    }

    @Override
    public boolean volumeMute() {
        MacKeyboardLib.INSTANCE.simulateMediaKey(MacKeyboardLib.NX_KEYTYPE_MUTE);
        return true;
    }

    @Override
    public boolean playOrPause() {
        MacKeyboardLib.INSTANCE.simulateMediaKey(MacKeyboardLib.NX_KEYTYPE_PLAY);
        return true;
    }

    @Override
    public boolean nextTrack() {
        MacKeyboardLib.INSTANCE.simulateMediaKey(MacKeyboardLib.NX_KEYTYPE_NEXT);
        return true;
    }

    @Override
    public boolean previousTrack() {
        MacKeyboardLib.INSTANCE.simulateMediaKey(MacKeyboardLib.NX_KEYTYPE_PREVIOUS);
        return true;
    }
}
