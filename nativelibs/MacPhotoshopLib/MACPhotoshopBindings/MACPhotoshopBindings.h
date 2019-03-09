//
//  MACPhotoshopBindings.h
//  MACPhotoshopBindings
//
//  Created by Federico on 04/03/2019.
//  Copyright © 2019 Federico Terzi. All rights reserved.
//

#import <AppKit/AppKit.h>
#import <ScriptingBridge/ScriptingBridge.h>

enum photoshopE940 {
    photoshopE940BeforeRunning = 'a942',
    photoshopE940Never = 'Nevr',
    photoshopE940OnRuntimeError = 'e941',
};
typedef enum photoshopE940 photoshopE940;

@interface photoshopApplication : SBApplication
- (NSString *) doJavascript:(id)x withArguments:(NSArray<id> *)withArguments showDebugger:(photoshopE940)showDebugger;
@end

extern photoshopApplication *reference;

void executeJavascript(const char *code, double arguments[], int argCount);
