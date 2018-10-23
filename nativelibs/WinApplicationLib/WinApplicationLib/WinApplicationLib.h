#pragma once

#ifdef WINAPPLICATIONLIB_EXPORTS
#define WINAPPLICATIONLIB_API __declspec(dllexport)
#else
#define WINAPPLICATIONLIB_API __declspec(dllimport)
#endif

typedef int(*ActiveAppCallback)(HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * appId);

/*
	Enumerate all the active applications, by invoking the callback for each one found.
	Note: the callback is invoked only once per application.
*/
extern "C" WINAPPLICATIONLIB_API void listActiveApplications(ActiveAppCallback callback);


/*
	Application Focus
*/

const long APPLICATION_FOCUS_CHECK_INTERVAL = 300;

/*
	Attempt to focus the requested application.
	Return:
	* 1 if succeeded.
	* 2 the application was already focused.
	* -1 if the application was not open.
	* -2 if the application was open, but could not be focused.
*/
extern "C" WINAPPLICATIONLIB_API int focusApplication(const wchar_t * applicationId);