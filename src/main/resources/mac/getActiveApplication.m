#import <Foundation/Foundation.h>
#import <AppKit/AppKit.h>

int main(int argc, char const *argv[])
{
    // Cycle through all open applications to find the one open
	for (NSRunningApplication *currApp in [[NSWorkspace sharedWorkspace] runningApplications])
	{
	    // If the app is active
		if ([currApp isActive]) {
		    // Print the executable path followed by the PID
			printf("%s\n%d\n",[[currApp executableURL].path UTF8String], [currApp processIdentifier]);
		}
	}
	return 0;
}