// TestWinApplicationLib.cpp: definisce il punto di ingresso dell'applicazione console.
//

#include "stdafx.h"
#include <Windows.h>
#include "..\WinApplicationLib\WinApplicationLib.h"

int main()
{
	listActiveApplications([](HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * appId) -> int {
		wprintf(L"%d %s %s\n", isUWPApp, executablePath, appId);
		return true;
	});

	//int result = focusApplication(L"store:Microsoft.Office.OneNote_8wekyb3d8bbwe!microsoft.onenoteim");
	int result = focusApplication(L"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
	Sleep(1000);
    return 0;
}

