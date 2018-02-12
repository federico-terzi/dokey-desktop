package utils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APIOptions;

import java.util.List;

/**
 * This class is used as a workaround on windows to
 * type arrow keys.
 */
public class MSKeyboardTyper {
    public static void keyPress(int keyCode) {
        // Prepare input reference
        WinUser.INPUT input = new WinUser.INPUT(  );

        input.type = new WinDef.DWORD( WinUser.INPUT.INPUT_KEYBOARD );
        input.input.setType("ki"); // Because setting INPUT_INPUT_KEYBOARD is not enough: https://groups.google.com/d/msg/jna-users/NDBGwC1VZbU/cjYCQ1CjBwAJ
        input.input.ki.wScan = new WinDef.WORD( 0 );
        input.input.ki.time = new WinDef.DWORD( 0 );
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR( 0 );

        input.input.ki.wVk = new WinDef.WORD(keyCode); // 0x41
        input.input.ki.dwFlags = new WinDef.DWORD( 0 );  // keydown

        User32.INSTANCE.SendInput( new WinDef.DWORD( 1 ), ( WinUser.INPUT[] ) input.toArray( 1 ), input.size() );

    }

    public static void keyRelease(int keyCode) {
        // Prepare input reference
        WinUser.INPUT input = new WinUser.INPUT(  );

        input.type = new WinDef.DWORD( WinUser.INPUT.INPUT_KEYBOARD );
        input.input.setType("ki"); // Because setting INPUT_INPUT_KEYBOARD is not enough: https://groups.google.com/d/msg/jna-users/NDBGwC1VZbU/cjYCQ1CjBwAJ
        input.input.ki.wScan = new WinDef.WORD( 0 );
        input.input.ki.time = new WinDef.DWORD( 0 );
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR( 0 );

        input.input.ki.wVk = new WinDef.WORD( keyCode); // 0x41
        input.input.ki.dwFlags = new WinDef.DWORD( 2 );  // keyup

        User32.INSTANCE.SendInput( new WinDef.DWORD( 1 ), ( WinUser.INPUT[] ) input.toArray( 1 ), input.size() );
    }
}
