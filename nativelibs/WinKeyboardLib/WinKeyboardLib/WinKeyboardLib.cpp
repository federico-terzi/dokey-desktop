// WinKeyboardLib.cpp: definisce le funzioni esportate per l'applicazione DLL.
//

#include "stdafx.h"
#include "WinKeyboardLib.h"

#define WINVER 0x0500
#include "Windows.h"
#include <array>
#include <vector>

// Virtual keys that vary between layouts
const int oemKeys[] = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75,
						76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 0x92, 0x93, 0x94, 0x95,
						0x96, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF, 0xC0, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF, 0xE1,
						0xE2, 0xE3, 0xE4, 0xE6, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF, 0xF0, 0xF1, 0xF2,
						0xF3, 0xF4, 0xF5, 0xFE,
						0x6A, 0x6B, 0x6D, 0x6F, // These are the +-/* keys on the numpad, put as last element because we
												// want to search for those keys in the main keyboard first.
												// If they're not found, we fallback to the numpad alternatives.
						0 };  // Terminator

/*
	Search for the virtual key code of the given key using the current keyboard layout.
	Return the virtual key code if found, -1 otherwise.
*/
int decodeVirtualKey(const wchar_t * key) {
	// Convert the Fixed keys
	if (wcscmp(key, L"CTRL") == 0) return VK_CONTROL;
	if (wcscmp(key, L"SHIFT") == 0) return VK_SHIFT;
	if (wcscmp(key, L"ALT") == 0) return VK_MENU;
	if (wcscmp(key, L"TAB") == 0) return VK_TAB;
	if (wcscmp(key, L"ENTER") == 0) return VK_RETURN;
	if (wcscmp(key, L"ESCAPE") == 0) return VK_ESCAPE;
	if (wcscmp(key, L"CAPS") == 0) return VK_CAPITAL;
	if (wcscmp(key, L"WIN") == 0) return VK_LWIN;
	if (wcscmp(key, L"SPACE") == 0) return VK_SPACE;
	if (wcscmp(key, L"BACK_SPACE") == 0) return VK_BACK;
	if (wcscmp(key, L"UP") == 0) return VK_UP;
	if (wcscmp(key, L"DOWN") == 0) return VK_DOWN;
	if (wcscmp(key, L"LEFT") == 0) return VK_LEFT;
	if (wcscmp(key, L"RIGHT") == 0) return VK_RIGHT;
	if (wcscmp(key, L"END") == 0) return VK_END;
	if (wcscmp(key, L"HOME") == 0) return VK_HOME;
	if (wcscmp(key, L"DELETE") == 0) return VK_DELETE;
	if (wcscmp(key, L"PAGE_UP") == 0) return VK_PRIOR;
	if (wcscmp(key, L"PAGE_DOWN") == 0) return VK_NEXT;
	if (wcscmp(key, L"INSERT") == 0) return VK_INSERT;
	if (wcscmp(key, L"NUMPAD0") == 0) return VK_NUMPAD0;
	if (wcscmp(key, L"NUMPAD1") == 0) return VK_NUMPAD1;
	if (wcscmp(key, L"NUMPAD2") == 0) return VK_NUMPAD2;
	if (wcscmp(key, L"NUMPAD3") == 0) return VK_NUMPAD3;
	if (wcscmp(key, L"NUMPAD4") == 0) return VK_NUMPAD4;
	if (wcscmp(key, L"NUMPAD5") == 0) return VK_NUMPAD5;
	if (wcscmp(key, L"NUMPAD6") == 0) return VK_NUMPAD6;
	if (wcscmp(key, L"NUMPAD7") == 0) return VK_NUMPAD7;
	if (wcscmp(key, L"NUMPAD8") == 0) return VK_NUMPAD8;
	if (wcscmp(key, L"NUMPAD9") == 0) return VK_NUMPAD9;
	if (wcscmp(key, L"PAUSE") == 0) return VK_PAUSE;
	if (wcscmp(key, L"MULTIPLY") == 0) return VK_MULTIPLY;
	if (wcscmp(key, L"ADD") == 0) return VK_ADD;
	if (wcscmp(key, L"SUBTRACT") == 0) return VK_SUBTRACT;
	if (wcscmp(key, L"DECIMAL") == 0) return VK_DECIMAL;
	if (wcscmp(key, L"DIVIDE") == 0) return VK_DIVIDE;
	if (wcscmp(key, L"F1") == 0) return VK_F1;
	if (wcscmp(key, L"F2") == 0) return VK_F2;
	if (wcscmp(key, L"F3") == 0) return VK_F3;
	if (wcscmp(key, L"F4") == 0) return VK_F4;
	if (wcscmp(key, L"F5") == 0) return VK_F5;
	if (wcscmp(key, L"F6") == 0) return VK_F6;
	if (wcscmp(key, L"F7") == 0) return VK_F7;
	if (wcscmp(key, L"F8") == 0) return VK_F8;
	if (wcscmp(key, L"F9") == 0) return VK_F9;
	if (wcscmp(key, L"F10") == 0) return VK_F10;
	if (wcscmp(key, L"F11") == 0) return VK_F11;
	if (wcscmp(key, L"F12") == 0) return VK_F12;
	if (wcscmp(key, L"F13") == 0) return VK_F13;
	if (wcscmp(key, L"F14") == 0) return VK_F14;
	if (wcscmp(key, L"F15") == 0) return VK_F15;
	if (wcscmp(key, L"F16") == 0) return VK_F16;
	if (wcscmp(key, L"F17") == 0) return VK_F17;
	if (wcscmp(key, L"F18") == 0) return VK_F18;
	if (wcscmp(key, L"F19") == 0) return VK_F19;
	if (wcscmp(key, L"F20") == 0) return VK_F20;
	if (wcscmp(key, L"F21") == 0) return VK_F21;
	if (wcscmp(key, L"F22") == 0) return VK_F22;
	if (wcscmp(key, L"F23") == 0) return VK_F23;
	if (wcscmp(key, L"F24") == 0) return VK_F24;
	if (wcscmp(key, L"NUM_LOCK") == 0) return VK_NUMLOCK;

	// Key is not int the constant list, try to find it dynamically

	BYTE keys[256];
	memset(keys, 0, sizeof(BYTE) * 256);

	std::array<WCHAR, 10> buffer;

	int i = 0;
	while (oemKeys[i] != 0) {
		// Get the key scan code
		int scanCode = MapVirtualKey(oemKeys[i], MAPVK_VK_TO_VSC);

		// Get the corresponding unicode string
		int result = ToUnicode(oemKeys[i], scanCode, keys, buffer.data(), buffer.size(), 0);

		// Check if the resulting unicode character is the requested key
		if (result > 0) {
			// Insert a null terminator to the string, because it could be missing
			buffer[result] = 0;

			if (wcscmp(key, buffer.data()) == 0) {
				return oemKeys[i];
			}
		}

		i++;
	}

	return -1;
}

/*
	Send the given keyboard shortcut to the system input queue.
	Return the number of keys if succeeded, -1 if a key wasn't found in the current keyboard layout.
*/
int sendShortcut(const wchar_t * keys[], int keyCount) {
	// Get all the virtual keys for the shortcut
	std::vector<int> virtualKeys;

	for (int i = 0; i < keyCount; i++) {
		int currentVirtualKey = decodeVirtualKey(keys[i]);

		if (currentVirtualKey != -1) {  // The key was valid, add it to the vector
			virtualKeys.push_back(currentVirtualKey);
		}
		else {  // The key was invalid, return an error code
			return -1;
		}
	}

	// Setup the INPUT structure used to send keyboard keystrokes
	INPUT ip;
	ip.type = INPUT_KEYBOARD;
	ip.ki.wScan = 0; // hardware scan code for key
	ip.ki.time = 0;
	ip.ki.dwExtraInfo = 0;

	// Press all the keys
	for (auto virtualKey : virtualKeys) {
		ip.ki.wVk = virtualKey; // virtual-key code 
		ip.ki.dwFlags = 0; // 0 for key press
		SendInput(1, &ip, sizeof(INPUT));

		Sleep(20);
	}

	// Release all the keys ( iterate in reverse order )
	for (unsigned i = virtualKeys.size(); i-- > 0; ) {
		ip.ki.wVk = virtualKeys[i]; // virtual-key code 
		ip.ki.dwFlags = KEYEVENTF_KEYUP; // 0 for key press
		SendInput(1, &ip, sizeof(INPUT));

		Sleep(20);
	}

	return virtualKeys.size();
}

/*
	Send a single key press, with the specified virtual key
*/
void sendKey(int virtualKey) {
	// Setup the INPUT structure used to send keyboard keystrokes
	INPUT ip;
	ip.type = INPUT_KEYBOARD;
	ip.ki.wScan = 0; // hardware scan code for key
	ip.ki.time = 0;
	ip.ki.dwExtraInfo = 0;

	ip.ki.wVk = virtualKey; // virtual-key code 
	ip.ki.dwFlags = 0; // 0 for key press
	SendInput(1, &ip, sizeof(INPUT));

	Sleep(20);

	ip.ki.wVk = virtualKey; // virtual-key code 
	ip.ki.dwFlags = KEYEVENTF_KEYUP; // 0 for key press
	SendInput(1, &ip, sizeof(INPUT));
}

/*
	Check if the CAPS LOCK key is pressed and, if so, disable it.
	Return 0 if the CAPS LOCK has been disabled, 1 if no action occurred.
*/
int forceDisableCapsLock() {
	// Check the state of the CAPS LOCK key
	SHORT result = GetKeyState(VK_CAPITAL);

	if ((result & 0x01) != 0) {  // CAPS LOCK is pressed
		// Disable it
		const wchar_t *capsKey[] = { L"CAPS" };
		sendShortcut(capsKey, 1);

		return 0;
	}

	return 1;
}