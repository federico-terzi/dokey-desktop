//
//  main.m
//  Test
//
//  Created by FreddyT on 13/11/2018.
//  Copyright © 2018 Federico Terzi. All rights reserved.
//

#import <Cocoa/Cocoa.h>

int main(int argc, const char * argv[]) {
    @autoreleasepool {
        NSRunningApplication *frontApp = [[NSWorkspace sharedWorkspace] frontmostApplication];
        
        NSLog(@"%@", [frontApp bundleURL].path);
    }
    return 0;
}
