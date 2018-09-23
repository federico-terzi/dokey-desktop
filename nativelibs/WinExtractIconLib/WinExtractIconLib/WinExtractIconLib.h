#pragma once

#ifdef WINEXTRACTICONLIB_EXPORTS
#define WINEXTRACTICONLIB_API __declspec(dllexport)
#else
#define WINEXTRACTICONLIB_API __declspec(dllimport)
#endif

extern "C" WINEXTRACTICONLIB_API int extractIconInternal(const wchar_t * executablePath, const wchar_t * destinationPath, int jumbo);