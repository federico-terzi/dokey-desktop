package system;

import net.model.KeyboardKeys;

import java.awt.Robot;
import java.awt.AWTException;
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
    public void sendKeystroke(List<? extends KeyboardKeys> keys) {
        // Press all the keys
        for (KeyboardKeys key : keys) {
            robot.delay(40);
            robot.keyPress(key.getKeyCode());
        }

        // Release all the keys
        for (KeyboardKeys key : keys) {
            robot.delay(10);
            robot.keyRelease(key.getKeyCode());
        }
    }
}
