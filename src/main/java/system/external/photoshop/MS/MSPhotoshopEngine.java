package system.external.photoshop.MS;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.Ole32;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import system.external.photoshop.PhotoshopEngine;

import java.util.logging.Logger;

public class MSPhotoshopEngine extends PhotoshopEngine {
    private boolean initialized = false;

    private PhotoshopCOMReference reference = null;

    private Logger log = Logger.getGlobal();

    private void initialize() {
        if (!initialized) {
            Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
            bindPhotoshopReference();
            initialized = true;
        }
    }

    private void bindPhotoshopReference() {
        reference = new PhotoshopCOMReference();
    }

    @Override
    public boolean executeJavascript(@NotNull String code, @NotNull Double[] params) {
        initialize();

        boolean firstTry = true;

        try {
            return executeJavascriptInternal(code, params);
        }catch (COMException exception) {
            // Probably photoshop was closed and the reference to the instance is not valid anymore.
            // We should try to connect again to photoshop and re-send the command. If even in this
            // case it doesn't work, we should stop.
            bindPhotoshopReference();
        }

        try {
            return executeJavascriptInternal(code, params);
        }catch (COMException exception) {
            log.warning("Could not execute Photoshop command: "+exception.getMessage());
            return false;
        }
    }

    private boolean executeJavascriptInternal(String code, Double[] params) {
        reference.doJavascript(code, ArrayUtils.toPrimitive(params));
        return true;
    }
}
