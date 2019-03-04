package system.external.photoshop.MAC;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import system.external.photoshop.PhotoshopEngine;

public class MACPhotoshopEngine extends PhotoshopEngine {
    @Override
    public boolean executeJavascript(@NotNull String code, @NotNull Double[] params) {
        MACPhotoshopBindings.INSTANCE.executeJavascript(code, ArrayUtils.toPrimitive(params), params.length);

        return true;
    }
}
