package system;

import net.model.KeyboardKeys;
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
     * Used to simulate keyboard presses to complete the keystroke
     * @param keys list of KeyboardKeys to press
     */
    public synchronized void sendKeystroke(List<? extends KeyboardKeys> keys) {
        // Press all the keys
        for (KeyboardKeys key : keys) {
            robot.delay(3);
            robot.keyPress(key.getKeyCode(OSValidator.getOS()));
        }

        // Release all the keys
        for (KeyboardKeys key : keys) {
            robot.delay(3);
            robot.keyRelease(key.getKeyCode(OSValidator.getOS()));
        }
    }
}
