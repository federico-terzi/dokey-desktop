// JavaWinNativeUI.cpp: definisce le funzioni esportate per l'applicazione DLL.
//

#include "stdafx.h"
#include <iostream>
#include <Windows.h>
#include <CommCtrl.h>
#include <memory>
#include "JavaWinNativeUI.h"

#pragma comment(linker,"\"/manifestdependency:type='win32' \
name='Microsoft.Windows.Common-Controls' version='6.0.0.0' \
processorArchitecture='*' publicKeyToken='6595b64144ccf1df' language='*'\"")

#pragma comment(lib, "comctl32.lib")

void displayDialog(wchar_t * title, wchar_t * description, int isCritical,
	wchar_t * buttons[], int buttonCount, int includeCancel, int useCommandLinks, void(*callback)(int)) {
	
	// Setup the dialog to accept high DPI screens.
	SetProcessDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE);
	
	TASKDIALOGCONFIG config = { 0 };
	
	// Setup the button structs
	auto buttonStructArray = std::make_unique<TASKDIALOG_BUTTON[]>(buttonCount);
	for (int i = 0; i < buttonCount; i++) {
		// Add the button to the struct array and give it an ID starting from 100.
		buttonStructArray[i] = { 100 + i, buttons[i]};
	}

	config.cbSize = sizeof(config);
	config.hInstance = NULL;

	if (includeCancel > 0) {
		config.dwCommonButtons = TDCBF_CANCEL_BUTTON;
	}

	// Setup the critical status
	if (isCritical == 0) {
		config.pszMainIcon = TD_INFORMATION_ICON;
	}
	else {
		config.pszMainIcon = TD_WARNING_ICON;
	}
	
	// Setup text
	config.pszMainInstruction = title;
	config.pszContent = description;
	config.pszWindowTitle = L"Dokey";
	config.pButtons = buttonStructArray.get();
	config.cButtons = buttonCount;
	
	if (useCommandLinks > 0) {
		config.dwFlags = TDF_USE_COMMAND_LINKS;
	}

	// This variable will hold the result of the dialog
	int nButtonPressed = 0;

	TaskDialogIndirect(&config, &nButtonPressed, NULL, NULL);
	
	callback(nButtonPressed);
}

void displayInfo(wchar_t * title, wchar_t * description, int isCritical, void(*callback)(int)) {
	// Setup the dialog to accept high DPI screens.
	SetProcessDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE);

	TASKDIALOGCONFIG config = { 0 };

	config.cbSize = sizeof(config);
	config.hInstance = NULL;
	config.dwCommonButtons = TDCBF_OK_BUTTON;

	// Setup the critical status
	if (isCritical == 0) {
		config.pszMainIcon = TD_INFORMATION_ICON;
	}
	else {
		config.pszMainIcon = TD_WARNING_ICON;
	}

	// Setup text
	config.pszMainInstruction = title;
	config.pszContent = description;
	config.pszWindowTitle = L"Dokey";

	// This variable will hold the result of the dialog
	int nButtonPressed = 0;

	TaskDialogIndirect(&config, &nButtonPressed, NULL, NULL);

	callback(nButtonPressed);
}