// WinExtractIconLib.cpp: definisce le funzioni esportate per l'applicazione DLL.
//

#include "stdafx.h"
#include "WinExtractIconLib.h"

#ifndef WINVER
#define WINVER 0x0603
#endif

#ifndef _WIN32_WINNT
#define _WIN32_WINNT 0x0603
#endif

#define UNICODE


#include <string>
#include <iostream>  
#include <winerror.h>
#include <windows.h>
#include <objidl.h>
#include <shellapi.h>
#include <gdiplus.h>
#include <CommCtrl.h>
#include <commoncontrols.h>


#pragma comment(lib, "gdiplus.lib")

using namespace Gdiplus;

int ExtractIcon(const wchar_t *path, const wchar_t *output, bool jumbo) {
	// Initialize GDI+.
	GdiplusStartupInput gdiplusStartupInput;
	ULONG_PTR gdiplusToken;
	GdiplusStartup(&gdiplusToken, &gdiplusStartupInput, NULL);

	// Initialize the encoder
	CLSID encoderClsid;
	CLSIDFromString(L"{557cf406-1a04-11d3-9a73-0000f81ef32e}", &encoderClsid);

	// Get the file info
	SHFILEINFO fi = { 0 };
	auto hr = SHGetFileInfo(path, 0, &fi, sizeof(fi), SHGFI_SYSICONINDEX);
	if (hr == 0) {
		return -1;
	}

	// If jumbo parameter is true, request the jumbo image.
	int quality = jumbo ? SHIL_JUMBO : SHIL_EXTRALARGE;

	IImageList *piml;
	if (SHGetImageList(quality, IID_PPV_ARGS(&piml)) != 0) {
		return -2;
	}

	HICON hIcon;
	piml->GetIcon(fi.iIcon, 0x00000020, &hIcon);
	piml->Release();

	// icon
	ICONINFO iconInfo = { 0 };
	GetIconInfo(hIcon, &iconInfo);

	HDC dc = GetDC(NULL);
	BITMAP bm = { 0 };
	GetObject(iconInfo.hbmColor, sizeof(BITMAP), &bm);

	BITMAPINFO bmi = { 0 };
	bmi.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
	bmi.bmiHeader.biWidth = bm.bmWidth;
	bmi.bmiHeader.biHeight = -bm.bmHeight;
	bmi.bmiHeader.biPlanes = 1;
	bmi.bmiHeader.biBitCount = 32;
	bmi.bmiHeader.biCompression = BI_RGB;

	// extract
	int nBits = bm.bmWidth * bm.bmHeight;
	int32_t* colorBits = new int32_t[nBits];
	GetDIBits(dc, iconInfo.hbmColor, 0, bm.bmHeight, colorBits, &bmi, DIB_RGB_COLORS);

	ReleaseDC(NULL, dc);
	::DeleteObject(iconInfo.hbmColor);
	::DeleteObject(iconInfo.hbmMask);

	Gdiplus::Bitmap *bmp = new Gdiplus::Bitmap(bm.bmWidth, bm.bmHeight, bm.bmWidth * 4, PixelFormat32bppARGB, (BYTE*)colorBits);
	DestroyIcon(hIcon);

	if (bmp->Save(output, &encoderClsid, NULL) != Ok) {
		delete bmp;
		delete[] colorBits;
		return -4;
	}

	GdiplusShutdown(gdiplusToken);

	return 0;
}

int extractIconInternal(const wchar_t * executablePath, const wchar_t * destinationPath, int jumbo) {
	return ExtractIcon(executablePath, destinationPath, jumbo);
}