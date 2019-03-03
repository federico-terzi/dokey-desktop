package system.external.photoshop.MAC;

import org.jetbrains.annotations.NotNull;
import system.external.photoshop.PhotoshopEngine;

public class MACPhotoshopEngine extends PhotoshopEngine {
    @Override
    public boolean executeJavascript(@NotNull String code, @NotNull Double[] params) {
        return false;
    }
}
