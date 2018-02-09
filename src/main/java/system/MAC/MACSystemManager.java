package system.MAC;

import system.SystemManager;

import java.io.IOException;

public class MACSystemManager implements SystemManager {
    private static final int VOLUME_STEP = 20;

    @Override
    public boolean shutdown() {
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
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"osascript", "-e", "set volume output volume (output volume of (get volume settings) - "+VOLUME_STEP+") --100%"});
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
    public boolean volumeUp() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"osascript", "-e", "set volume output volume (output volume of (get volume settings) + "+VOLUME_STEP+") --100%"});
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
    public boolean volumeMute() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"osascript", "-e", "set volume with output muted"});
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
    public boolean playOrPause() {

        return false;
    }

    @Override
    public boolean nextTrack() {
        return false;
    }

    @Override
    public boolean previousTrack() {
        return false;
    }
}
