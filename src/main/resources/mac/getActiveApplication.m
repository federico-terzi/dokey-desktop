#import <Foundation/Foundation.h>
#import <AppKit/AppKit.h>

#define MAX_TRIES 10

int main(int argc, char const *argv[])
{
    // If an active application isn't found, try again MAX_TRIES times.
    int tries;

    for (tries = 0; tries<MAX_TRIES; tries++) {
        // Cycle through all open applications to find the one open
    	for (NSRunningApplication *currApp in [[NSWorkspace sharedWorkspace] runningApplications])
    	{
    	    // If the app is active
    		if ([currApp isActive]) {
    		    // Print the executable path followed by the PID
    			printf("%s\n%d\n",[[currApp executableURL].path UTF8String], [currApp processIdentifier]);
    			return 0;
    		}
    	}

    	// Sleep for a bit
    	[NSThread sleepForTimeInterval: 0.1];
    }

	return 0;
}