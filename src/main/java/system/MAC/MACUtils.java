package system.MAC;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * This class provides the bindings to the native operating system.
 */
public class MACUtils {
    /**
     * A JNA wrapper around the objective-c runtime.  This contains all of the functions
     * needed to interact with the runtime (e.g. send messages, etc..).
     *
     * <h3>Sample Usage</h3>
     * <script src="https://gist.github.com/3974488.js?file=SampleLowLevelAPI.java"></script>
     *
     * @author shannah
     * @see <a href="https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ObjCRuntimeRef/Reference/reference.html">Objective-C Runtime Reference</a>
     */
    public interface Runtime extends Library {
        Runtime INSTANCE = (Runtime) Native.loadLibrary("objc.A", Runtime.class);

        Pointer objc_lookUpClass(String name);
        String class_getName(Pointer id);
        Pointer class_getProperty(Pointer cls, String name);
        Pointer class_getSuperclass(Pointer cls);
        int class_getVersion(Pointer theClass);
        String class_getWeakIvarLayout(Pointer cls);
        boolean class_isMetaClass(Pointer cls);
        int class_getInstanceSize(Pointer cls);
        Pointer class_getInstanceVariable(Pointer cls, String name);
        Pointer class_getInstanceMethod(Pointer cls, Pointer aSelector);
        Pointer class_getClassMethod(Pointer cls, Pointer aSelector);

        String class_getIvarLayout(Pointer cls);
        Pointer class_getMethodImplementation(Pointer cls, Pointer name);
        Pointer class_getMethodImplementation_stret(Pointer cls, Pointer name);
        Pointer class_replaceMethod(Pointer cls, Pointer name, Pointer imp, String types);
        Pointer class_respondsToSelector(Pointer cls, Pointer sel);
        void class_setIvarLayout(Pointer cls, String layout);
        Pointer class_setSuperclass(Pointer cls, Pointer newSuper);
        void class_setVersion(Pointer theClass, int version);
        void class_setWeakIvarLayout(Pointer cls, String layout);
        String ivar_getName(Pointer ivar);
        long ivar_getOffset(Pointer ivar);
        String ivar_getTypeEncoding(Pointer ivar);
        String method_copyArgumentType(Pointer method, int index);
        String method_copyReturnType(Pointer method);
        void method_exchangeImplementations(Pointer m1, Pointer m2);
        void method_getArgumentType(Pointer method, int index, Pointer dst, long dst_len);
        Pointer method_getImplementation(Pointer method);
        Pointer method_getName(Pointer method);
        int method_getNumberOfArguments(Pointer method);
        void method_getReturnType(Pointer method, Pointer dst, long dst_len);
        String method_getTypeEncoding(Pointer method);
        Pointer method_setImplementation(Pointer method, Pointer imp);
        Pointer objc_allocateClassPair(Pointer superclass, String name, long extraBytes);
        Pointer[] objc_copyProtocolList(Pointer outCount);
        Pointer objc_getAssociatedObject(Pointer object, String key);
        Pointer objc_getClass(String name);
        int objc_getClassList(Pointer buffer, int bufferlen);
        Pointer objc_getFutureClass(String name);
        Pointer objc_getMetaClass(String name);
        Pointer objc_getProtocol(String name);
        Pointer objc_getRequiredClass(String name);
        long objc_msgSend(Pointer theReceiver, Pointer theSelector,Object... arguments);

        long objc_msgSendSuper(Pointer superClassStruct, Pointer op, Object... arguments);
        long objc_msgSendSuper_stret(Pointer superClassStruct, Pointer op, Object... arguments);
        double objc_msgSend_fpret(Pointer self, Pointer op, Object... arguments);
        void objc_msgSend_stret(Pointer stretAddr, Pointer theReceiver, Pointer theSelector, Object... arguments);
        void objc_registerClassPair(Pointer cls);
        void objc_removeAssociatedObjects(Pointer object);
        void objc_setAssociatedObject(Pointer object, Pointer key, Pointer value, Pointer policy);
        void objc_setFutureClass(Pointer cls, String name);
        Pointer object_copy(Pointer obj, long size);
        Pointer object_dispose(Pointer obj);
        Pointer object_getClass(Pointer object);
        String object_getClassName(Pointer obj);
        Pointer object_getIndexedIvars(Pointer obj);
        Pointer object_getInstanceVariable(Pointer obj, String name, Pointer outValue);
        Pointer object_getIvar(Pointer object, Pointer ivar);
        Pointer object_setClass(Pointer object, Pointer cls);
        Pointer object_setInstanceVariable(Pointer obj, String name, Pointer value);
        void object_setIvar(Pointer object, Pointer ivar, Pointer value);
        String property_getAttributes(Pointer property);
        boolean protocol_conformsToProtocol(Pointer proto, Pointer other);
        Structure protocol_copyMethodDescriptionList(Pointer protocol, boolean isRequiredMethod, boolean isInstanceMethod, Pointer outCount);
        Pointer protocol_copyPropertyList(Pointer proto, Pointer outCount);
        Pointer protocol_copyProtocolList(Pointer proto, Pointer outCount);
        Pointer protocol_getMethodDescription(Pointer proto, Pointer aSel, boolean isRequiredMethod, boolean isInstanceMethod);
        String protocol_getName(Pointer proto);
        Pointer protocol_getProperty(Pointer proto, String name, boolean isRequiredProperty, boolean isInstanceProperty);
        boolean protocol_isEqual(Pointer protocol, Pointer other);
        String sel_getName(Pointer aSelector);

        Pointer sel_getUid(String name);
        boolean sel_isEqual(Pointer lhs, Pointer rhs);
        Pointer sel_registerName(String name);
    }

    public static Pointer lookUpClass(String name) {
        return Runtime.INSTANCE.objc_lookUpClass(name);
    }

    public static Pointer message(Pointer object, String selector, Object... arguments) {
        Pointer selectorPtr = Runtime.INSTANCE.sel_getUid(selector);
        long resultAddress = Runtime.INSTANCE.objc_msgSend(object, selectorPtr, arguments);
        return new Pointer(resultAddress);
    }

    public static long messageLong(Pointer object, String selector, Object... arguments) {
        Pointer selectorPtr = Runtime.INSTANCE.sel_getUid(selector);
        return Runtime.INSTANCE.objc_msgSend(object, selectorPtr, arguments);
    }
}
