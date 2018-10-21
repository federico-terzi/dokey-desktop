// TestWinApplicationLib.cpp: definisce il punto di ingresso dell'applicazione console.
//

#include "stdafx.h"
#include "..\WinApplicationLib\WinApplicationLib.h"

int main()
{
	listActiveApplications([](const wchar_t * executablePath, int isUWPApp) {
		wprintf(L"%d %s\n", isUWPApp, executablePath);
	});
    return 0;
}

