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
	DWORD dokeyPID;
	HWND *lastActiveHandles;
	int lastActiveHandlesSize;
	bool avoidDuplicates;
};

struct enum_children_windows_info {
	enum_windows_info *parentInfo;
	int parentPid;
	HWND parentHandle;
	int stopEnumeration = 0;
	int childFound = 0;
	bool avoidDuplicates;
};

void listActiveApplicationsInternal(FunctionalActiveAppsCallback const& callback, HWND lastActiveHandles[], int lastActiveHandlesSize, bool avoidDuplicates) {
	// This set will hold the previous executable paths analyzed.
	std::unordered_set<std::wstring> pathSet;

	// Save the current PID to exclude dokey from the list
	DWORD dokeyPID = GetCurrentProcessId();

	enum_windows_info info;
	info.pathSetPtr = &pathSet;
	info.callback = callback;
	info.dokeyPID = dokeyPID;
	info.lastActiveHandles = lastActiveHandles;
	info.lastActiveHandlesSize = lastActiveHandlesSize;
	info.avoidDuplicates = avoidDuplicates;
	
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
		int isUWPApp = NOT_UWP_APP;

		// Check if the current window belongs to a UWP app
		if ((style & WS_EX_NOREDIRECTIONBITMAP) != 0) {
			isUWPApp = IS_UWP_APP;

			// Check if the window is visible. This is necessary because with the new
			// UWP apps, the isWindowVisible is not sufficient to determine if it is visible.
			// The cloaked attribute must be checked
			DWORD cloaked;
			DwmGetWindowAttribute(hwnd, DWMWA_CLOAKED, &cloaked, sizeof(DWORD));

			// Filter out invisible windows. Cloaked value meanings:
			//    0 = Program is running on current virtual desktop
			//    1 = Program is not running
			//    2 = Program is running on a different virtual desktop
			if (cloaked != 0 && cloaked != 2) {
				return true;
			}
		}

		// Extract the window PID
		DWORD windowPid;
		GetWindowThreadProcessId(hwnd, &windowPid);

		// Filter out dokey from the list
		if (windowPid == info.dokeyPID) {
			return true;
		}

		// Extract the process executable file path
		HANDLE process = OpenProcess(PROCESS_QUERY_LIMITED_INFORMATION, FALSE, windowPid);
		std::array<WCHAR, MAX_PATH> filename;
		DWORD filenameSize = MAX_PATH;
		QueryFullProcessImageName(process, 0, filename.data(), &filenameSize);
		CloseHandle(process);

		// Convert the char array to a string
		std::wstring filenameString(filename.data());

		// Some windows store apps don't have the WS_EX_NOREDIRECTIONBITMAP flag, so another check
		// is needed to detect them correctly
		if (filenameString.find(L"WindowsApps") != std::string::npos) {
			isUWPApp = IS_UWP_APP;
		}

		if (!isUWPApp) {  // Standard info extraction, for Win32 apps
			// Filter out Explorer
			if (filenameString.find(L"explorer.exe") != std::string::npos) {
				return true;
			}

			// Check if the executable has been analyzed previously
			if (!info.avoidDuplicates || info.pathSetPtr->find(filenameString) == info.pathSetPtr->end()) {  // Not found
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

			// With UWP apps, to find the executable path of the UWP the process is a bit complicated.
			// After we found the application frame host handle, we must enumerate all the children
			// windows until we found a child with a different pid than the parent. That is the 
			// correct window, and we can then extract the executable path.

			enum_children_windows_info childInfo = {
				&info,
				windowPid,
				hwnd,
				0,
				info.avoidDuplicates
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
					if (!childInfo.avoidDuplicates || childInfo.parentInfo->pathSetPtr->find(filenameString) == childInfo.parentInfo->pathSetPtr->end()) {  // Not found
						// Extract the UWP application identifier
						auto appIdPtr = extractApplicationUserModelId(process);

						// Make sure the application id could be extracted
						if (appIdPtr) {
							// Convert the appId to a dokey valid identifier by prefixing "store:"
							std::wstring id = L"store:" + *appIdPtr;

							// Mark the child as found
							childInfo.childFound = TRUE;

							// Invoke the callback
							bool result = childInfo.parentInfo->callback(childInfo.parentHandle, filename.data(), IS_UWP_APP, id.c_str());
							if (!result) {
								childInfo.stopEnumeration = 1;
							}
						}
								
						// Insert the path in the set to avoid duplicates
						childInfo.parentInfo->pathSetPtr->insert(filenameString);
					}

					// Release the process handle
					CloseHandle(process);

					return false;
				}

				return true;
			}, reinterpret_cast<LPARAM>(&childInfo));

			// Check if the child window ( the real UWP store app window ) was found
			// If not, we could try to recover the situation by looking at the lastActiveHandles
			// to determine if this parent window was previously associated with a UWP app
			// This is necessary because when a UWP app is minimized ( or changes virtual desktop )
			// the application frame host parent detaches from it, and we cannot find it anymore.
			// So we send a "partial" result, that dokey will try to use by combining it with the
			// previous results.
			if (!childInfo.childFound) {
				// Check if this ApplicationFrameHost was attached to a child window previously
				for (int i = 0; i < info.lastActiveHandlesSize; i++) {
					if (info.lastActiveHandles[i] == hwnd) {
						// Invoke the callback, with partial results
						info.callback(hwnd, NULL, IS_PARTIAL_UWP_APP, NULL);

						break;
					}
				}
			}

			// Check if one of the child window decided to stop the enumeration
			if (childInfo.stopEnumeration) {
				return false;
			}
		}

		return true;
	}, (LPARAM) &info);
}

void listActiveApplications(ActiveAppCallback callback, HWND lastActiveHandles[], int lastActiveHandlesSize) {
	listActiveApplicationsInternal([&callback](HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * appId) -> bool {
		return callback(hwnd, executablePath, isUWPApp, appId);
	}, lastActiveHandles, lastActiveHandlesSize, true);
}

void getActiveApplication(ActiveAppCallback callback) {
	HWND activeWindow = GetForegroundWindow();

	listActiveApplicationsInternal([&callback, activeWindow](HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * appId) -> bool {
		if (activeWindow == hwnd) {
			callback(hwnd, executablePath, isUWPApp, appId);
			return false;
		}
		
		return true;
	}, NULL, 0, false);
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

int focusApplication(const wchar_t * applicationId, HWND applicationHandle, HWND lastActiveHandles[], int lastActiveHandlesSize) {
	bool applicationFound{ false };
	bool applicationAlreadyFocused{ false };
	HWND targetWindow;

	// Save the initial focused window
	HWND initialWindow = GetForegroundWindow();

	listActiveApplicationsInternal([&applicationFound, &targetWindow, &applicationId, &initialWindow, &applicationAlreadyFocused,
									applicationHandle]
	(HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * appId) {		
		bool matches = 0;

		// Decide which parameter is needed for the comparison based on the presence of the applicationId
		if (applicationId == nullptr) {
			matches = (applicationHandle == hwnd);  // MATCHES BY HANDLE
		}
		else {
			matches = (wcscmp(appId, applicationId) == 0);  // MATCHES BY APP_ID
		}
		
		// Check if the current application is the desired one.
		// If so, save the window handle reference.
		if (matches) {
			applicationFound = true;
			targetWindow = hwnd;

			// Check if the application is already the one currently in focus
			if (hwnd == initialWindow) {
				applicationAlreadyFocused = true;
			}

			return false;  // Stop the enumeration
		}

		return true;
	}, lastActiveHandles, lastActiveHandlesSize, true);

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

/*
	UWP APP INFORMATION EXTRACTION
*/

std::unique_ptr<std::wstring> extractFamilyNameFromAppId(const std::wstring& appId) {
	UINT32 familyNameLen = 0;
	UINT32 relativeAppIdLen = 0;
	LONG rc = ParseApplicationUserModelId(appId.c_str(), &familyNameLen, NULL, &relativeAppIdLen, NULL);
	if (rc != ERROR_INSUFFICIENT_BUFFER)
	{
		return std::unique_ptr<std::wstring>(nullptr);
	}

	std::vector<WCHAR> familyNameBuffer(familyNameLen);
	std::vector<WCHAR> relativeAppIdBuffer(familyNameLen);
	rc = ParseApplicationUserModelId(appId.c_str(), &familyNameLen, familyNameBuffer.data(), &relativeAppIdLen, relativeAppIdBuffer.data());
	if (rc != ERROR_SUCCESS) {
		return std::unique_ptr<std::wstring>(nullptr);
	}

	// Convert the family name to a string
	std::wstring familyName(familyNameBuffer.data());
	auto familyNamePtr = std::make_unique<std::wstring>(familyName);

	return familyNamePtr;
}

std::unique_ptr<std::wstring> extractFullNameByFamilyName(const std::wstring& familyName) {
	UINT32 packagesCount = 0;
	UINT32 bufferLen = 0;
	UINT32 properties;
	LONG rc = FindPackagesByPackageFamily(familyName.c_str(), PACKAGE_FILTER_HEAD, &packagesCount, NULL, &bufferLen, NULL, &properties);
	if (rc != ERROR_INSUFFICIENT_BUFFER)
	{
		return std::unique_ptr<std::wstring>(nullptr);
	}

	// Check if there is any package
	if (packagesCount == 0) {
		return std::unique_ptr<std::wstring>(nullptr);
	}

	std::vector<PWSTR> fullNames(packagesCount);
	std::vector<WCHAR> buffer(bufferLen);
	rc = FindPackagesByPackageFamily(familyName.c_str(), PACKAGE_FILTER_HEAD, &packagesCount, fullNames.data(), &bufferLen, buffer.data(), &properties);
	if (rc != ERROR_SUCCESS) {
		return std::unique_ptr<std::wstring>(nullptr);
	}

	// Convert the family name to a string
	std::wstring fullName(fullNames[0]);
	auto fullNamePtr = std::make_unique<std::wstring>(fullName);

	return fullNamePtr;
}

std::unique_ptr<std::wstring> extractPathFromFullName(const std::wstring& fullName) {
	UINT32 pathLen = 0;
	LONG rc = GetPackagePathByFullName(fullName.c_str(), &pathLen, NULL);
	if (rc != ERROR_INSUFFICIENT_BUFFER)
	{
		return std::unique_ptr<std::wstring>(nullptr);
	}

	std::vector<WCHAR> pathBuffer(pathLen);
	rc = GetPackagePathByFullName(fullName.c_str(), &pathLen, pathBuffer.data());
	if (rc != ERROR_SUCCESS) {
		return std::unique_ptr<std::wstring>(nullptr);
	}

	// Convert the path to a string
	std::wstring path(pathBuffer.data());
	auto pathPtr = std::make_unique<std::wstring>(path);

	return pathPtr;
}

int extractUWPApplicationDirectory(const wchar_t * dokeyAppId, __out wchar_t * pathBuffer, int bufferSize) {
	// Remove the "store:" prefix from the string
	std::wstring appId{ dokeyAppId + 6 };

	// Extract the family name from the application id
	auto familyName = extractFamilyNameFromAppId(appId);
	if (!familyName) {
		return -1;
	}

	// Extract the full name from the family name
	auto fullName = extractFullNameByFamilyName(*familyName);
	if (!fullName) {
		return -1;
	}

	// Extract the application path from the full name
	auto path = extractPathFromFullName(*fullName);
	if (path) {
		// Copy the path to the output buffer
		wcsncpy_s(pathBuffer, bufferSize, path->c_str(), bufferSize-1);

		return 1;
	}
	else {
		return -1;
	}
}