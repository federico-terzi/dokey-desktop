// WinApplicationLib.cpp: definisce le funzioni esportate per l'applicazione DLL.
//

#include "stdafx.h"
#define UNICODE
#include "Windows.h"
#include <dwmapi.h>
#include <array>
#include <string>
#include <memory>
#include <unordered_set>
#include <appmodel.h>
#include <functional>
#include "WinApplicationLib.h"

#pragma comment(lib, "Dwmapi.lib")

/*
	Extract the ApplicationUserModelId from the given process handle.
	This can be used to identify a UWP application.
	Return the AUMID if found, nullptr otherwise.
*/
std::unique_ptr<std::wstring> extractApplicationUserModelId(HANDLE process) {
	UINT32 length = 0;
	LONG rc = GetApplicationUserModelId(process, &length, NULL);
	if (rc != ERROR_INSUFFICIENT_BUFFER)
	{
		return std::unique_ptr<std::wstring>(nullptr);
	}

	std::vector<WCHAR> appId(length);
	rc = GetApplicationUserModelId(process, &length, appId.data());
	if (rc != ERROR_SUCCESS) {
		return std::unique_ptr<std::wstring>(nullptr);
	}

	// Convert the full name buffer to a string
	std::wstring appIdString(appId.data());
	auto appIdStr = std::make_unique<std::wstring>(appIdString);

	return appIdStr;
}

/*
	LIST ACTIVE APPLICATIONS
*/

typedef std::function<bool(HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * uwpId)> FunctionalActiveAppsCallback;

struct enum_windows_info {
	std::unordered_set<std::wstring> *pathSetPtr;
	FunctionalActiveAppsCallback callback;
};

struct enum_children_windows_info {
	enum_windows_info *parentInfo;
	int parentPid;
	int stopEnumeration = 0;
};

void listActiveApplicationsInternal(FunctionalActiveAppsCallback const& callback) {
	// This set will hold the previous executable paths analyzed.
	std::unordered_set<std::wstring> pathSet;

	enum_windows_info info;
	info.pathSetPtr = &pathSet;
	info.callback = callback;
	
	// Start the analysis by iterating through all windows.
	EnumWindows([](HWND hwnd, LPARAM lparam) -> BOOL {
		enum_windows_info& info(*reinterpret_cast<enum_windows_info*>(lparam));

		// Filter out all the invisible windows
		if (!IsWindowVisible(hwnd)) {
			return true;
		}

		// Get the attributes of the window
		LONG style = GetWindowLong(hwnd, GWL_EXSTYLE);

		// Filter out all the tool windows ( that will not be displayed in the task bar )
		if ((style & WS_EX_TOOLWINDOW) != 0) {
			return true;
		}

		// This flag will become 1 if the current app is a UWP application.
		int isUWPApp = 0;

		// Check if the current window belongs to a UWP app
		if ((style & WS_EX_NOREDIRECTIONBITMAP) != 0) {
			isUWPApp = 1;

			// Check if the window is visible. This is necessary because with the new
			// UWP apps, the isWindowVisible is not sufficient to determine if it is visible.
			// The cloaked attribute must be checked
			DWORD cloaked;
			DwmGetWindowAttribute(hwnd, DWMWA_CLOAKED, &cloaked, sizeof(DWORD));

			// Filter out invisible windows.
			if (cloaked != 0) {
				return true;
			}
		}

		// Extract the window PID
		DWORD windowPid;
		GetWindowThreadProcessId(hwnd, &windowPid);

		// Extract the process executable file path
		HANDLE process = OpenProcess(PROCESS_QUERY_LIMITED_INFORMATION, FALSE, windowPid);
		std::array<WCHAR, MAX_PATH> filename;
		DWORD filenameSize = MAX_PATH;
		QueryFullProcessImageName(process, 0, filename.data(), &filenameSize);

		// Convert the char array to a string
		std::wstring filenameString(filename.data());

		if (!isUWPApp) {  // Standard info extraction, for Win32 apps			
			// Check if the executable has been analyzed previously
			if (info.pathSetPtr->find(filenameString) == info.pathSetPtr->end()) {  // Not found
				// Invoke the callback
				bool result = info.callback(hwnd, filename.data(), isUWPApp, filename.data());
				// If the callback returned false, stop the enumeration.
				if (!result) {
					return false;
				}

				// Insert the path in the set to avoid duplicates
				info.pathSetPtr->insert(filenameString);
			}
		}
		else {  // New extraction needed for Windows 10 UWP Apps
			// The window belongs to a UWP app, make sure that it is the ApplicationFrameHost
			if (filenameString.find(L"ApplicationFrameHost.exe") == std::string::npos) {
				return true;  // Not the ApplicationFrameHost, skip this one
			}

			// With UWP apps, to find the executable path of the UWP the process is a bit complicated.
			// After we found the application frame host handle, we must enumerate all the children
			// windows until we found a child with a different pid than the parent. That is the 
			// correct window, and we can then extract the executable path.

			enum_children_windows_info childInfo = {
				&info,
				windowPid
			};

			EnumChildWindows(hwnd, [](HWND childHwnd, LPARAM childLparam) -> BOOL {
				enum_children_windows_info& childInfo(*reinterpret_cast<enum_children_windows_info*>(childLparam));
			
				// Get the current child window pid
				DWORD childPid;
				GetWindowThreadProcessId(childHwnd, &childPid);

				// Check if the window PID is different than the parent one.
				if (childPid != childInfo.parentPid) {
					// Extract the children executable path
					HANDLE process = OpenProcess(PROCESS_QUERY_LIMITED_INFORMATION, FALSE, childPid);
					std::array<WCHAR, MAX_PATH> filename;
					DWORD filenameSize = MAX_PATH;
					QueryFullProcessImageName(process, 0, filename.data(), &filenameSize);

					// Convert the char array to a string
					std::wstring filenameString(filename.data());

					// Check if the executable has been analyzed previously
					if (childInfo.parentInfo->pathSetPtr->find(filenameString) == childInfo.parentInfo->pathSetPtr->end()) {  // Not found
						// Extract the UWP application identifier
						auto appIdPtr = extractApplicationUserModelId(process);

						// Make sure the application id could be extracted
						if (appIdPtr) {
							// Convert the appId to a dokey valid identifier by prefixing "store:"
							std::wstring id = L"store:" + *appIdPtr;

							// Invoke the callback
							bool result = childInfo.parentInfo->callback(childHwnd, filename.data(), 1, id.c_str());
							if (!result) {
								childInfo.stopEnumeration = 1;
								return false;
							}
						}
								
						// Insert the path in the set to avoid duplicates
						childInfo.parentInfo->pathSetPtr->insert(filenameString);
					}

					return false;
				}

				return true;
			}, reinterpret_cast<LPARAM>(&childInfo));

			// Check if one of the child window decided to stop the enumeration
			if (childInfo.stopEnumeration) {
				return false;
			}
		}

		return true;
	}, (LPARAM) &info);
}

void listActiveApplications(ActiveAppCallback callback) {
	listActiveApplicationsInternal([&callback](HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * appId) -> bool {
		return callback(hwnd, executablePath, isUWPApp, appId);
	});
}

/*
	APPLICATION FOCUS METHODS
*/

void sendAltTabSwitch() {
	INPUT ip;
	ip.type = INPUT_KEYBOARD;
	ip.ki.wScan = 0; // hardware scan code for key
	ip.ki.time = 0;
	ip.ki.dwExtraInfo = 0;

	ip.ki.wVk = VK_MENU; // virtual-key code 
	ip.ki.dwFlags = 0; // 0 for key press
	SendInput(1, &ip, sizeof(INPUT));
	Sleep(20);

	ip.ki.wVk = VK_TAB; // virtual-key code 
	ip.ki.dwFlags = 0; // 0 for key press
	SendInput(1, &ip, sizeof(INPUT));
	Sleep(20);

	ip.ki.wVk = VK_TAB; // virtual-key code 
	ip.ki.dwFlags = KEYEVENTF_KEYUP;
	SendInput(1, &ip, sizeof(INPUT));
	Sleep(20);

	ip.ki.wVk = VK_MENU; // virtual-key code 
	ip.ki.dwFlags = KEYEVENTF_KEYUP;
	SendInput(1, &ip, sizeof(INPUT));
	Sleep(20);
}

int focusApplication(const wchar_t * applicationId) {
	bool applicationFound{ false };
	bool applicationAlreadyFocused{ false };
	HWND targetWindow;

	// Save the initial focused window
	HWND initialWindow = GetForegroundWindow();

	listActiveApplicationsInternal([&applicationFound, &targetWindow, &applicationId, &initialWindow, &applicationAlreadyFocused]
	(HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * appId) {		
		// Check if the current application is the desired one.
		// If so, save the window handle reference.
		if (wcscmp(appId, applicationId) == 0) {
			applicationFound = true;
			targetWindow = hwnd;

			// Check if the application is already the one currently in focus
			if (hwnd == initialWindow) {
				applicationAlreadyFocused = true;
			}

			return false;  // Stop the enumeration
		}

		return true;
	});

	// If the application was already open, don't do anything
	if (applicationAlreadyFocused) {
		return 2;
	}

	// If the application was not currently open, return an error.
	if (!applicationFound) {
		return -1;
	}

	bool hasBeenOpened{ false };
	bool hasAltTabWorkaroundBeenTried{ false };

	while (!hasBeenOpened) {
		// Try to focus the requested window
		SetForegroundWindow(targetWindow);
		// Restore if minimized
		WINDOWPLACEMENT placement;
		GetWindowPlacement(targetWindow, &placement);
		if (placement.showCmd == SW_SHOWMINIMIZED) {
			ShowWindow(targetWindow, SW_RESTORE);
		}

		Sleep(20);

		// Check if the window has been focused
		HWND focusedWindow = GetForegroundWindow();
		
		if (focusedWindow == targetWindow) {   // WORKED CORRECLY.
			hasBeenOpened = true;
			break;
		}
		else {  // NOT OPENED CORRECTLY, Attempt with the ALT+TAB workaround.
			if (!hasAltTabWorkaroundBeenTried) {
				sendAltTabSwitch();
				hasAltTabWorkaroundBeenTried = true;

				Sleep(APPLICATION_FOCUS_CHECK_INTERVAL);
			}
		}
	}

	return hasBeenOpened ? 1 : -2;
}