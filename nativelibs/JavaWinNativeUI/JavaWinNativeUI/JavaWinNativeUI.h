#pragma once

#ifdef JAVAWINNATIVEUI_EXPORTS
#define JAVAWINNATIVEUI_API __declspec(dllexport)
#else
#define JAVAWINNATIVEUI_API __declspec(dllimport)
#endif

extern "C" JAVAWINNATIVEUI_API void displayInfo(wchar_t * title, wchar_t * description, int isCritical, void(*callback)(int));

extern "C" JAVAWINNATIVEUI_API void displayDialog(wchar_t * title, wchar_t * description, int isCritical, 
	wchar_t * buttons[], int buttonCount, int includeCancel, int useCommandLinks, void (*callback)(int));