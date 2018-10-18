#pragma once

#ifdef WINKEYBOARDLIB_EXPORTS
#define WINKEYBOARDLIB_API __declspec(dllexport)
#else
#define WINKEYBOARDLIB_API __declspec(dllimport)
#endif

extern "C" WINKEYBOARDLIB_API int sendShortcut(const wchar_t * keys[], int keyCount);
extern "C" WINKEYBOARDLIB_API int decodeVirtualKey(const wchar_t key);
extern "C" WINKEYBOARDLIB_API int forceDisableCapsLock();