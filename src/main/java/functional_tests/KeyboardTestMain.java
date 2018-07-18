//import net.model.KeyboardKeys;
//import system.keyboard.KeyboardManager;
//
//import java.awt.*;
//import java.util.Arrays;
//
//public class KeyboardTestMain {
//    public static void main(String[] args) {
//        try {
//            KeyboardManager km = new KeyboardManager();
//            km.sendKeystroke(Arrays.asList(KeyboardKeys.VK_CONTROL, KeyboardKeys.VK_SHIFT,  KeyboardKeys.VK_K));
//            Thread.sleep(1000);
//            km.sendKeystroke(Arrays.asList(KeyboardKeys.VK_ESCAPE));
//        } catch (AWTException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        System.exit(0);
//    }
//}