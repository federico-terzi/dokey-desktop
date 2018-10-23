// TestWinApplicationLib.cpp: definisce il punto di ingresso dell'applicazione console.
//

#include "stdafx.h"
#include <Windows.h>
#include "..\WinApplicationLib\WinApplicationLib.h"

int main()
{
	listActiveApplications([](HWND hwnd, const wchar_t * executablePath, int isUWPApp, const wchar_t * uwpId) {
		wprintf(L"%d %s %s\n", isUWPApp, executablePath, uwpId);

		/*if (wcsstr(executablePath, L"onenoteim.exe") != 0) {
			SetForegroundWindow(hwnd);
		}*/
	});
    return 0;
}

