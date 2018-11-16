#pragma once

#ifdef WINAPPLICATIONLIB_EXPORTS
#define WINAPPLICATIONLIB_API __declspec(dllexport)
#else
#define WINAPPLICATIONLIB_API __declspec(dllimport)
#endif


// Possible values of isUWPApp
#define NOT_UWP_APP 0
#define IS_UWP_APP 1
#define IS_PARTIAL_UWP_APP 2  // The given handle was previously associated with a UWP app
							  // but now they are detached so we cannot extract full information.

typedef int(*ActiveAppCallback)(HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * appId);

/*
	Enumerate all the active applications, by invoking the callback for each one found.
	Note: the callback is invoked only once per application.
*/
extern "C" WINAPPLICATIONLIB_API void listActiveApplications(ActiveAppCallback callback, HWND lastActiveHandles[], int lastActiveHandlesSize);

/*
	Find the application currently in focus.
*/
extern "C" WINAPPLICATIONLIB_API void getActiveApplication(ActiveAppCallback callback);

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
extern "C" WINAPPLICATIONLIB_API int focusApplication(const wchar_t * applicationId, HWND applicationHandle, HWND lastActiveHandles[], int lastActiveHandlesSize);

/*
	Extract the application directory for the given dokeyAppId ( store:familyName!AppId )
	The path will be exported to the pathBuffer. The bufferSize parameter specify the number
	of characters that the buffer accepts. If the path exceeds that size, it will be truncated.
	Return 1 if the path was correctly found, -1 if an error occurred.
*/
extern "C" WINAPPLICATIONLIB_API int extractUWPApplicationDirectory(const wchar_t * dokeyAppId, __out wchar_t * pathBuffer, int bufferSize);