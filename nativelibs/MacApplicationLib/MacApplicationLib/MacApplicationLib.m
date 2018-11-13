//
//  MacApplicationLib.m
//  MacApplicationLib
//
//  Created by FreddyT on 13/11/2018.
//  Copyright © 2018 Federico Terzi. All rights reserved.
//

#import "MacApplicationLib.h"


int focusDokey() {
    int dokeyPid = getpid();
    
    NSArray *runningApps = [[NSWorkspace sharedWorkspace] runningApplications];
    for (id app in runningApps) {
        if ([app processIdentifier] == dokeyPid) {
            [app activateWithOptions: NSApplicationActivateIgnoringOtherApps];
            return 1;
        }
    }
    
    return 0;
}

int extractApplicationIcon(const char * appPath, const char * targetFile) {
    NSString *appPathString = [NSString stringWithUTF8String:appPath];
    NSString *targetFileString = [NSString stringWithUTF8String:targetFile];
    
    // Load the image
    NSImage *image = [[NSWorkspace sharedWorkspace] iconForFile:appPathString];
    
    // Save the image to file
    NSData *imageData = [image TIFFRepresentation];
    NSBitmapImageRep *imageRep = [NSBitmapImageRep imageRepWithData:imageData];
    NSDictionary *imageProps = [NSDictionary dictionaryWithObject:[NSNumber numberWithFloat:1.0] forKey:NSImageCompressionFactor];
    imageData = [imageRep representationUsingType:NSPNGFileType properties:imageProps];
    [imageData writeToFile:targetFileString atomically:NO];
    
    return 1;
}

void getActiveApplication(char * pathBuffer, int bufferSize) {
    // Get the active application
    NSRunningApplication *frontApp = [[NSWorkspace sharedWorkspace] frontmostApplication];
    NSString *bundlePath = [frontApp bundleURL].path;
    const char * path = [bundlePath UTF8String];
    strlcpy(pathBuffer, path, bufferSize);
}

void getActiveApplications(void (*callback)(const char * appPath)) {
    NSArray *runningApps = [[NSWorkspace sharedWorkspace] runningApplications];
    for (id app in runningApps) {
        if ([app activationPolicy] == NSApplicationActivationPolicyRegular) {
            NSString *bundlePath = [app bundleURL].path;
            const char * path = [bundlePath UTF8String];
            callback(path);
        }
    }
}

int getActivePID() {
    // Get the active application
    NSRunningApplication *frontApp = [[NSWorkspace sharedWorkspace] frontmostApplication];
    return [frontApp processIdentifier];
}

int activateRunningApplication(const char * appPath) {
    NSString *appPathString = [NSString stringWithUTF8String:appPath];
    NSArray *runningApps = [[NSWorkspace sharedWorkspace] runningApplications];
    for (id app in runningApps) {
        NSString *bundlePath = [app bundleURL].path;
        if ([bundlePath isEqualToString:appPathString]) {
            [app activateWithOptions: NSApplicationActivateIgnoringOtherApps];
            return 1;
        }
    }
    
    return 0;
}
