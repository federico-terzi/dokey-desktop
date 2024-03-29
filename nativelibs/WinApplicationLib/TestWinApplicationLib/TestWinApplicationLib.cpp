// TestWinApplicationLib.cpp: definisce il punto di ingresso dell'applicazione console.
//

#include "stdafx.h"
#include <Windows.h>
#include "..\WinApplicationLib\WinApplicationLib.h"

int main()
{
	/*HWND lastHandles[] = { reinterpret_cast<HWND>(330502) };

	listActiveApplications([](HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * appId) -> int {
		wprintf(L"%d %d %s %s\n", hwnd, isUWPApp, executablePath, appId);
		return true;
	}, lastHandles, 1);

	WCHAR output[512];
	//int result = extractUWPApplicationDirectory(L"store:Microsoft.Office.OneNote_8wekyb3d8bbwe!microsoft.onenoteim", output, 512);
	//int result = extractUWPApplicationDirectory(L"store:windows.immersivecontrolpanel_cw5n1h2txyewy!microsoft.windows.immersivecontrolpanel", output, 512);
	int result = extractUWPApplicationDirectory(L"store:5319275A.WhatsAppDesktop_cv1g1gvanyjgm!WhatsAppDesktop", output, 512);

	wprintf(L"%d %s\n", result, output);

	*/
	
	while (1) {
		getActiveApplication([](HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * appId) -> int {
			wprintf(L"%d %s %s\n", isUWPApp, executablePath, appId);
			return true;
		});
		Sleep(1000);
	}
	

	//int result = focusApplication(L"store:Microsoft.Office.OneNote_8wekyb3d8bbwe!microsoft.onenoteim");
	//int result = focusApplication(L"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
	//Sleep(1000);
    return 0;
}

