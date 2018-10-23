#pragma once

#ifdef WINAPPLICATIONLIB_EXPORTS
#define WINAPPLICATIONLIB_API __declspec(dllexport)
#else
#define WINAPPLICATIONLIB_API __declspec(dllimport)
#endif

#define ActiveAppCallback void(*callback)(HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * uwpId)

extern "C" WINAPPLICATIONLIB_API void listActiveApplications(ActiveAppCallback);
