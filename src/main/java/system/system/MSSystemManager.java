package system.system;

import com.sun.jna.Pointer;
import system.keyboard.bindings.WinKeyboardLib;
import system.system.SystemManager;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.io.IOException;

/**
 * Windows system manager.
 */
public class MSSystemManager extends SystemManager {
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public boolean restart() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"Shutdown.exe", "-r", "-t", "00"});
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
    public boolean shutdownPC() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"Shutdown.exe", "-s", "-t", "00"});
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
            Process proc = runtime.exec(new String[]{"Rundll32.exe", "User32.dll,LockWorkStation"});
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
            Process proc = runtime.exec(new String[]{"rundll32.exe", "powrprof.dll,SetSuspendState", "0,1,0"});
            proc.waitFor();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean attemptToSendKey(int virtualKey) {
        /*
        When sending an input from the dokey search bar, we must ensure that a window is currently in the foreground.
        If that's not true ( like in the instant of the dokey search bar closing ) the call to sendInput will fail.
        This workaround "waits" until a new window is in the foreground.
         */
        int attempts = 0;
        while (User32.INSTANCE.GetForegroundWindow() == null && attempts < MAX_ATTEMPTS) {
            attempts++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        WinKeyboardLib.INSTANCE.sendKey(virtualKey);

        return attempts < MAX_ATTEMPTS;
    }

    @Override
    public boolean volumeDown() {
        return attemptToSendKey(0xAE);
    }

    @Override
    public boolean volumeUp() {
        return attemptToSendKey(0xAF);
    }

    @Override
    public boolean volumeMute() {
        return attemptToSendKey(0xAD);
    }

    @Override
    public boolean playOrPause() {
        return attemptToSendKey(0xB3);
    }

    @Override
    public boolean nextTrack() {
        return attemptToSendKey(0xB0);
    }

    @Override
    public boolean previousTrack() {
        return attemptToSendKey(0xB1);
    }
}
