package system;

import net.model.KeyboardKeys;
import utils.MSKeyboardTyper;
import utils.OSValidator;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Used to send keystrokes
 */
public class KeyboardManager {
    private Robot robot;

    public KeyboardManager() throws AWTException {
        robot = new Robot();
        robot.setAutoDelay(40);
        robot.setAutoWaitForIdle(true);
    }

    /**
     * @param keyCode the keyCode
     * @return true if the given key has to be patched on windows.
     */
    private static boolean isPatchedKey(int keyCode) {
        return (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
                keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN);
    }

    /**
     * Used to simulate keyboard presses to complete the keystroke
     * @param keys list of KeyboardKeys to press
     */
    public synchronized void sendKeystroke(List<? extends KeyboardKeys> keys) {
        // Press all the keys
        for (KeyboardKeys key : keys) {
            robot.delay(3);
            int keyCode = key.getKeyCode(OSValidator.getOS());

            // Workaround on windows for arrow keys
            if (OSValidator.isWindows() && isPatchedKey(keyCode)) {
                MSKeyboardTyper.keyPress(keyCode);
            }else{
                robot.keyPress(keyCode);
            }
        }

        // Release all the keys
        for (KeyboardKeys key : keys) {
            robot.delay(3);

            int keyCode = key.getKeyCode(OSValidator.getOS());

            // Workaround on windows for arrow keys
            if (OSValidator.isWindows() && isPatchedKey(keyCode)) {
                MSKeyboardTyper.keyRelease(keyCode);
            }else{
                robot.keyRelease(keyCode);
            }
        }
    }
}
