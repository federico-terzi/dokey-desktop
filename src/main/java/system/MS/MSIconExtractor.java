package system.MS;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import jdk.nashorn.tools.Shell;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.sun.jna.platform.WindowUtils.getIconSize;

public class MSIconExtractor {

    private static Pointer RT_GROUP_ICON = Pointer.createConstant(14);
    private static Pointer RT_ICON = Pointer.createConstant(3);

    public interface PsApi extends Library {
        PsApi INSTANCE = (PsApi) Native.loadLibrary("psapi", PsApi.class, W32APIOptions.DEFAULT_OPTIONS);

        int GetMappedFileName(WinNT.HANDLE process, WinNT.HANDLE module,
                              byte[] name, int i);

    }

    public interface ShellLib extends Shell32 {
        ShellLib INSTANCE = (ShellLib) Native.loadLibrary("Shell32", ShellLib.class, W32APIOptions.DEFAULT_OPTIONS);

        WinNT.HRESULT SHDefExtractIcon(String lpszFile, int nIconIndex, int flags, WinDef.HICON[] phiconLarge, WinDef.HICON[] phiconSmall, int iconSize);
    }

    public static void saveExecutableIcon(String exePath) {
//        WinNT.HMODULE hModule = Kernel32.INSTANCE.LoadLibraryEx(exePath, null, Kernel32.LOAD_LIBRARY_AS_DATAFILE);
//
//        // Get the filename
//        byte[] filenameBytes = new byte[1024];
//        PsApi.INSTANCE.GetMappedFileName(Kernel32.INSTANCE.GetCurrentProcess(),
//                hModule,
//                filenameBytes,
//                1024);
//        String filename = Native.toString(filenameBytes);
//
//        Kernel32.INSTANCE.EnumResourceNames(hModule, RT_ICON,
//                (module, type, name, lParam) -> {
//                    WinNT.HANDLE hResInfo = Kernel32.INSTANCE.FindResource(hModule, name, type);
//
//                    WinNT.HANDLE hResData = Kernel32.INSTANCE.LoadResource(hModule, (WinDef.HRSRC) hResInfo);
//
//                    Pointer pResData = Kernel32.INSTANCE.LockResource(hResData);
//
//                    int size = Kernel32.INSTANCE.SizeofResource(hModule, hResInfo);
//
//                    byte[] buff = pResData.getByteArray(0, size);
//
//                    return true;
//                }, null);
        WinDef.HICON[] icons = new WinDef.HICON[100];
        WinNT.HRESULT hresult = ShellLib.INSTANCE.SHDefExtractIcon(exePath,
                0,
                0,
                icons,
                null,
                128);
        for (int j = 0; j<icons.length; j++) {
            WinDef.HICON hIcon = icons[j];
            final Dimension iconSize = getIconSize(hIcon);
            if (iconSize.width == 0 || iconSize.height == 0)
                return;

            final int width = iconSize.width;
            final int height = iconSize.height;
            final short depth = 24;

            final byte[] lpBitsColor = new byte[width * height * depth / 8];
            final Pointer lpBitsColorPtr = new Memory(lpBitsColor.length);
            final byte[] lpBitsMask = new byte[width * height * depth / 8];
            final Pointer lpBitsMaskPtr = new Memory(lpBitsMask.length);
            final WinGDI.BITMAPINFO bitmapInfo = new WinGDI.BITMAPINFO();
            final WinGDI.BITMAPINFOHEADER hdr = new WinGDI.BITMAPINFOHEADER();

            bitmapInfo.bmiHeader = hdr;
            hdr.biWidth = width;
            hdr.biHeight = height;
            hdr.biPlanes = 1;
            hdr.biBitCount = depth;
            hdr.biCompression = 0;
            hdr.write();
            bitmapInfo.write();

            final WinDef.HDC hDC = User32.INSTANCE.GetDC(null);
            final WinGDI.ICONINFO iconInfo = new WinGDI.ICONINFO();
            User32.INSTANCE.GetIconInfo(hIcon, iconInfo);
            iconInfo.read();

            GDI32.INSTANCE.GetDIBits(hDC, iconInfo.hbmColor, 0, height,
                    lpBitsColorPtr, bitmapInfo, 0);
            lpBitsColorPtr.read(0, lpBitsColor, 0, lpBitsColor.length);
            GDI32.INSTANCE.GetDIBits(hDC, iconInfo.hbmMask, 0, height,
                    lpBitsMaskPtr, bitmapInfo, 0);
            lpBitsMaskPtr.read(0, lpBitsMask, 0, lpBitsMask.length);
            final BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);

            int r, g, b, a, argb;
            int x = 0, y = height - 1;
            for (int i = 0; i < lpBitsColor.length; i = i + 3) {
                b = lpBitsColor[i] & 0xFF;
                g = lpBitsColor[i + 1] & 0xFF;
                r = lpBitsColor[i + 2] & 0xFF;
                a = 0xFF - lpBitsMask[i] & 0xFF;
                argb = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, argb);
                x = (x + 1) % width;
                if (x == 0)
                    y--;
            }

            User32.INSTANCE.ReleaseDC(null, hDC);

            try {
                File output = File.createTempFile("test", "test.png");
                ImageIO.write(image, "png", output);
                System.out.println(output.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return;
    }
}
