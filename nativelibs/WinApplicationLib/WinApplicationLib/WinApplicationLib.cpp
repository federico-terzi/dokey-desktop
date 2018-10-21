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
#include "WinApplicationLib.h"

#pragma comment(lib, "Dwmapi.lib")

struct enum_windows_info {
	std::unordered_set<std::wstring> *pathSetPtr;
	void(*callback)(const wchar_t * executablePath, int isUWPApp);
};

struct enum_children_windows_info {
	enum_windows_info *parentInfo;
	int parentPid;
};

void listActiveApplications(void(*callback)(const wchar_t * executablePath, int isUWPApp)) {
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
				info.callback(filename.data(), isUWPApp);

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
						// Invoke the callback
						childInfo.parentInfo->callback(filename.data(), 1);

						// Insert the path in the set to avoid duplicates
						childInfo.parentInfo->pathSetPtr->insert(filenameString);
					}

					return false;
				}

				return true;
			}, reinterpret_cast<LPARAM>(&childInfo));
		}

		return true;
	}, (LPARAM) &info);
}
