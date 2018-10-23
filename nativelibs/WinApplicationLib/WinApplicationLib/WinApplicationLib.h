#pragma once

#ifdef WINAPPLICATIONLIB_EXPORTS
#define WINAPPLICATIONLIB_API __declspec(dllexport)
#else
#define WINAPPLICATIONLIB_API __declspec(dllimport)
#endif

#define ActiveAppCallback void(*callback)(HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * uwpId)

/*
	Enumerate all the active applications, by invoking the callback for each one found.
	Note: the callback is invoked only once per application.
*/
extern "C" WINAPPLICATIONLIB_API void listActiveApplications(ActiveAppCallback);

