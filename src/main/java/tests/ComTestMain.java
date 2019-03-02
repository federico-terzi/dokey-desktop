package tests;

import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMLateBindingObject;
import com.sun.jna.platform.win32.COM.IDispatch;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WinDef;

public class ComTestMain {
    class Photoshop extends COMLateBindingObject {
        public Photoshop() throws COMException {
            super("Photoshop.Application", true);
        }

        public String getPath() {
            return this.getStringProperty("Path");
        }

        public Document getActiveDocument() {
            return new Document(this.getAutomationProperty(
                    "ActiveDocument"));
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

        public void executeAction(int eventID, ActionDescriptor desc, int mode) {
            this.invoke("ExecuteAction", new Variant.VARIANT(eventID),
                    new Variant.VARIANT(desc.getIDispatch()), new Variant.VARIANT(mode));
        }

        public String doJavascript(String code, double[] arguments) {
            OaIdl.SAFEARRAY args = OaIdl.SAFEARRAY.createSafeArray(1);
            args.putElement(new Variant.VARIANT(arguments[0]), 0);
            Variant.VARIANT res = this.invoke("DoJavaScript", new Variant.VARIANT(code),
                    new Variant.VARIANT(args));
            return res.stringValue();
        }
    }

    public class Document extends COMLateBindingObject {
        public Document(IDispatch iDispatch) throws COMException {
            super(iDispatch);
        }

        public Layer getActiveLayer() {
            return new Layer(this.getAutomationProperty("ActiveLayer"));
        }
    }

    public class Layer extends COMLateBindingObject {
        public Layer(IDispatch iDispatch) throws COMException {
            super(iDispatch);
        }

        public void setOpacity(int opacity) {
            this.setProperty("Opacity", opacity);
        }
    }

    public class ActionReference extends COMLateBindingObject {
        private Photoshop application;
        public ActionReference(Photoshop application) throws COMException {
            super("Photoshop.ActionReference", true);
            this.application = application;
        }

        public void putEnumerated(int desiredClass, int enumType, int value) {
            this.invokeNoReply("PutEnumerated", new Variant.VARIANT(desiredClass),
                    new Variant.VARIANT(enumType), new Variant.VARIANT(value));
        }

    }

    public class ActionDescriptor extends COMLateBindingObject {
        private Photoshop application;
        public ActionDescriptor(Photoshop application) throws COMException {
            super("Photoshop.ActionDescriptor", true);
            this.application = application;
        }

        public void putReference(int key, IDispatch value) {
            this.invokeNoReply("PutReference", new Variant.VARIANT(key), new Variant.VARIANT(value));
        }

        public void putDouble(int key, double value) {
            this.invokeNoReply("PutDouble", new Variant.VARIANT(key), new Variant.VARIANT(value));
        }

        public void putObject(int key, int classType, IDispatch value) {
            this.invokeNoReply("PutObject", new Variant.VARIANT(key), new Variant.VARIANT(classType),
                    new Variant.VARIANT(value));
        }
    }

    public static void main(String[] args) {
        ComTestMain main = new ComTestMain();
        main.demo();
    }

    public static void setOpacity(Photoshop p, double val) {
        p.doJavascript("var idsetd = charIDToTypeID( \"setd\" );\r\n    var desc5 = new ActionDescriptor();\r\n    var idnull = charIDToTypeID( \"null\" );\r\n        var ref2 = new ActionReference();\r\n        var idLyr = charIDToTypeID( \"Lyr \" );\r\n        var idOrdn = charIDToTypeID( \"Ordn\" );\r\n        var idTrgt = charIDToTypeID( \"Trgt\" );\r\n        ref2.putEnumerated( idLyr, idOrdn, idTrgt );\r\n    desc5.putReference( idnull, ref2 );\r\n    var idT = charIDToTypeID( \"T   \" );\r\n        var desc6 = new ActionDescriptor();\r\n        var idOpct = charIDToTypeID( \"Opct\" );\r\n        var idPrc = charIDToTypeID( \"#Prc\" );\r\n        desc6.putUnitDouble( idOpct, idPrc, arguments[0] );\r\n    var idLyr = charIDToTypeID( \"Lyr \" );\r\n    desc5.putObject( idT, idLyr, desc6 );\r\nexecuteAction( idsetd, desc5, DialogModes.NO );\r\n", new double[] {val});
    }

    public void demo() {
        Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
        Photoshop photoshop = new Photoshop();
        for (int i = 0; i<100; i++) {
            setOpacity(photoshop, i);
        }


//        System.out.println(photoshop.getPath());
//        Document document = photoshop.getActiveDocument();
//        Layer layer = document.getActiveLayer();
//
////        for (int i = 0; i <= 100; i+= 10) {
////            layer.setOpacity(i);
////            try {
////                Thread.sleep(100);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
////        }
//        int brush = photoshop.charIDToTypeID("Brsh");
//        int ordn = photoshop.charIDToTypeID("Ordn");
//        int target = photoshop.charIDToTypeID("Trgt");
//        ActionReference ref = new ActionReference(photoshop);
//        ref.putEnumerated(brush, ordn, target);
//        ActionDescriptor desc = new ActionDescriptor(photoshop);
//        desc.putReference(photoshop.getNullTypeID(), ref.getIDispatch());
//        int diameter = photoshop.stringIDToTypeID("diameter");
//        ActionDescriptor desc1 = new ActionDescriptor(photoshop);
//        desc1.putDouble(diameter, 18);
//        int toType = photoshop.stringIDToTypeID("to");
//        desc.putObject(toType, brush, desc1.getIDispatch());
//        int setDType = photoshop.charIDToTypeID("setd");
//        photoshop.executeAction(setDType, desc, 3);

    }
}
