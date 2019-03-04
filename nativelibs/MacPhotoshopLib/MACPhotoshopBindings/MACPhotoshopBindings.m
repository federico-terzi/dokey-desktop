//
//  MACPhotoshopBindings.m
//  MACPhotoshopBindings
//
//  Created by Federico on 04/03/2019.
//  Copyright © 2019 Federico Terzi. All rights reserved.
//

#import "MACPhotoshopBindings.h"

photoshopApplication *reference = NULL;

void executeJavascript(const char *code, double arguments[], int argCount) {
    if (!reference) {
        reference = [SBApplication applicationWithBundleIdentifier:@"com.adobe.Photoshop"];
    }
    
    NSString * codeString = [NSString stringWithUTF8String:code];
    NSMutableArray * argArray = [NSMutableArray arrayWithCapacity:argCount];
    for (int i = 0; i<argCount; i++) {
        argArray[i] = [NSNumber numberWithDouble:arguments[i]];
    }
    
    [reference doJavascript:codeString withArguments:argArray showDebugger:photoshopE940Never];
}
