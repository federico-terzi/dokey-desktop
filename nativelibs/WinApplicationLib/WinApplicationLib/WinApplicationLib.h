#pragma once

#ifdef WINAPPLICATIONLIB_EXPORTS
#define WINAPPLICATIONLIB_API __declspec(dllexport)
#else
#define WINAPPLICATIONLIB_API __declspec(dllimport)
#endif

extern "C" WINAPPLICATIONLIB_API void listActiveApplications(void(*callback)(const wchar_t * executablePath, int isUWPApp));
