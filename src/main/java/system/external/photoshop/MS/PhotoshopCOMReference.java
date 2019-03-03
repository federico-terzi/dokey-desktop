package system.external.photoshop.MS;

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMLateBindingObject;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.Variant;

class PhotoshopCOMReference extends COMLateBindingObject {
    public PhotoshopCOMReference() throws COMException {
        super("Photoshop.Application", true);
    }

    public int charIDToTypeID(String charID) {
        Variant.VARIANT arg = new Variant.VARIANT(charID);
        Variant.VARIANT res = this.invoke("CharIDToTypeID", arg);
        return res.intValue();
    }

    public int stringIDToTypeID(String stringID) {
        Variant.VARIANT arg = new Variant.VARIANT(stringID);
        Variant.VARIANT res = this.invoke("StringIDToTypeID", arg);
        return res.intValue();
    }

    public int getNullTypeID() {
        return charIDToTypeID("null");
    }

    public String doJavascript(String code, double[] arguments) {
        OaIdl.SAFEARRAY args = OaIdl.SAFEARRAY.createSafeArray(arguments.length);
        for (int i = 0; i<arguments.length; i++) {
            args.putElement(new Variant.VARIANT(arguments[i]), i);
        }
        Variant.VARIANT res = this.invoke("DoJavaScript", new Variant.VARIANT(code),
                new Variant.VARIANT(args));
        return res.stringValue();
    }
}
