#import <Foundation/Foundation.h>
#import <AppKit/AppKit.h>

// TO COMPILE: gcc -o getActiveApplications getActiveApplications.m -framework Cocoa

int main(int argc, char const *argv[])
{
    // Cycle through all open applications
	for (NSRunningApplication *currApp in [[NSWorkspace sharedWorkspace] runningApplications])
	{
		// Check if the app is visible in the dock
		if (currApp.activationPolicy == NSApplicationActivationPolicyRegular) {
			// Print the executable path followed by the PID
			printf("%s\n",[[currApp executableURL].path UTF8String]);
		}
	}

	return 0;
}