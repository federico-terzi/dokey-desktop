import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.MediaKey;
import com.tulskiy.keymaster.common.Provider;

import javax.swing.*;

public class TestMain {
    public static void main(String[] args) {
        Provider provider = Provider.getCurrentProvider(false);

        provider.register(KeyStroke.getKeyStroke("alt SPACE"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                System.out.println(hotKey);
            }
        });

        while(true) {

        }

        //System.exit(0);
    }
}