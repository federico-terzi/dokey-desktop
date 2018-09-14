//
//  JavaMacNativeUI.m
//  JavaMacNativeUI
//
//  Created by FreddyT on 14/09/2018.
//  Copyright © 2018 Federico Terzi. All rights reserved.
//

#import "JavaMacNativeUI.h"

@implementation JavaMacNativeUI

+ (void)displayDialogInternal:(NSString *)imagePath title:(NSString *)title description:(NSString *)description buttons:(NSArray *)buttons isCritical:(BOOL)isCritical callback:(void (^)(int))callback {
    
    NSAlert *alert = [[NSAlert alloc] init];
    
    // Setup title and description
    if (title) {
        [alert setMessageText:title];
    }
    if (description) {
        [alert setInformativeText:description];
    }
    
    // Add all the buttons
    for (id buttonText in buttons) {
        [alert addButtonWithTitle:buttonText];
    }
    
    // Add the image if present
    if (imagePath) {
        NSImage *icon = [[NSImage alloc] initWithContentsOfFile:imagePath];
        [alert setIcon:icon];
    }
    
    // Setup the style
    if (isCritical) {
        [alert setAlertStyle:NSAlertStyleCritical];
    }
    
    // Display the dialog
    NSModalResponse result = [alert runModal];
    
    // Check the result and call the callback
    if (result == NSAlertFirstButtonReturn) {
        callback(1);
    }else if (result == NSAlertSecondButtonReturn) {
        callback(2);
    }else if (result == NSAlertThirdButtonReturn) {
        callback(3);
    }else{
        callback(0);
    }
    
}

@end

// Native methods

void displayDialog(char* imagePath, char* title, char* description, char *buttons[],
                   int buttonsCount, int isCritical, void (*callbackPtr)(int)) {
    dispatch_async(dispatch_get_main_queue(), ^(void){
        NSString *imagePathString;
        if (imagePath) {
            imagePathString = [NSString stringWithUTF8String:imagePath];
        }
        
        NSString *titleString;
        if (title) {
            titleString = [NSString stringWithUTF8String:title];
        }
        
        NSString *descriptionString;
        if (description) {
            descriptionString = [NSString stringWithUTF8String:description];
        }
        
        BOOL isCriticalBool = NO;
        if (isCritical > 0) {
            isCriticalBool = YES;
        }
        
        NSMutableArray *buttonArray = [[NSMutableArray alloc] init];
        for (int i = 0; i<buttonsCount; i++) {
            [buttonArray addObject:[NSString stringWithUTF8String:buttons[i]]];
        }
        
        [JavaMacNativeUI displayDialogInternal:imagePathString title:titleString description:descriptionString buttons:buttonArray isCritical:isCriticalBool callback:^(int buttonNumber) {
            // Invoke the callback function pointer
            callbackPtr(buttonNumber);
        }];
    });
}
