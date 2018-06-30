package tests;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMLateBindingObject;
import com.sun.jna.platform.win32.COM.IDispatch;
import com.sun.jna.platform.win32.Ole32;

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

    public static void main(String[] args) {
        ComTestMain main = new ComTestMain();
        main.demo();
    }

    public void demo() {
        Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
        Photoshop photoshop = new Photoshop();
        System.out.println(photoshop.getPath());
        Document document = photoshop.getActiveDocument();
        Layer layer = document.getActiveLayer();

        for (int i = 0; i <= 100; i+= 10) {
            layer.setOpacity(i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
