#import <Foundation/Foundation.h>
#import <AppKit/AppKit.h>

// NOT USED ANYMORE BECAUSE IT DOESN'T REFRESH

// TO COMPILE: gcc -o getActiveApplications getActiveApplications.m -framework Cocoa

int main(int argc, char const *argv[])
{
    //runUntilDate:dateWithTimeIntervalSinceNow:1;
    //NSRunLoop *rl = [NSRunLoop currentRunLoop];
    //NSDate *date = [NSDate dateWithTimeIntervalSinceNow:1];
    //[rl runMode:NSDefaultRunLoopMode beforeDate:date];

    // Cycle through all open applications
	for (NSRunningApplication *currApp in [[NSWorkspace sharedWorkspace] runningApplications])
	{
		// Check if the app is visible in the dock
		if (currApp.activationPolicy == NSApplicationActivationPolicyRegular) {
			// Print the executable path followed by the PID
			printf("%s\n",[[currApp executableURL].path UTF8String]);
		}
	}

	printf("%d\n", NSApplicationActivateIgnoringOtherApps);

	return 0;
}