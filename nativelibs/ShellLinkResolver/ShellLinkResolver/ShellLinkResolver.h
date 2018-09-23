#pragma once

#ifdef SHELLLINKRESOLVER_EXPORTS
#define SHELLLINKRESOLVER_API __declspec(dllexport)
#else
#define SHELLLINKRESOLVER_API __declspec(dllimport)
#endif

extern "C" SHELLLINKRESOLVER_API int resolveLnkTargetInternal(LPCSTR lnkFilePath, LPWSTR targetBuffer, int targetBufferSize);