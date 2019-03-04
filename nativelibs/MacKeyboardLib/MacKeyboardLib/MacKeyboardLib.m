//
//  MacKeyboardLib.m
//  MacKeyboardLib
//
//  Created by Federico Terzi on 18/10/2018.
//  Copyright © 2018 Federico Terzi. All rights reserved.
//

#import "MacKeyboardLib.h"
#import <Quartz/Quartz.h>
#include <IOKit/IOKitLib.h>
#include <IOKit/hidsystem/IOHIDLib.h>
#include <IOKit/hidsystem/IOHIDParameter.h>

// Keys that change based on the current keyboard layout
const int variableKeys[] = {
    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0B, 0x0C, 0x0D, 0x0E,
    0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C,
    0x1D, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x23, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B,
    0x2C, 0x2D, 0x2E, 0x2F, 0x32, 0x41, 0x43, 0x45, 0x47, 0x4B, 0x4C, 0x4E, 0x51, 0x52,
    0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5B, 0x5C
};

/*
 Search for the virtual key code of the given key using the current keyboard layout.
 Return the cirtual key code if found, -1 otherwise.
 */
int decodeVirtualKey(NSString *key, const UCKeyboardLayout *keyboardLayout,
                     uint8 keyboardType) {
    // Convert fixed keys that do not change with keyboard layout
    if ([key isEqualToString:@"ENTER"]) { return 0x24; } // kVK_Return
    if ([key isEqualToString:@"TAB"]) { return 0x30; } // kVK_Tab
    if ([key isEqualToString:@"SPACE"]) { return 0x31; } // kVK_Space
    if ([key isEqualToString:@"DELETE"]) { return 0x33; } // kVK_Delete
    if ([key isEqualToString:@"ESCAPE"]) { return 0x35; } // kVK_Escape
    if ([key isEqualToString:@"CMD"]) { return 0x37; } // kVK_Command
    if ([key isEqualToString:@"SHIFT"]) { return 0x38; } // kVK_Shift
    if ([key isEqualToString:@"CAPS"]) { return 0x39; } // kVK_CapsLock
    if ([key isEqualToString:@"ALT"]) { return 0x3A; } // kVK_Option
    if ([key isEqualToString:@"CTRL"]) { return 0x3B; } // kVK_Control
    if ([key isEqualToString:@"F17"]) { return 0x40; } // kVK_F17
    if ([key isEqualToString:@"VOL_UP"]) { return 0x48; } // kVK_VolumeUp
    if ([key isEqualToString:@"VOL_DOWN"]) { return 0x49; } // kVK_VolumeDown
    if ([key isEqualToString:@"VOL_MUTE"]) { return 0x4A; } // kVK_Mute
    if ([key isEqualToString:@"F18"]) { return 0x4F; } // kVK_F18
    if ([key isEqualToString:@"F19"]) { return 0x50; } // kVK_F19
    if ([key isEqualToString:@"F20"]) { return 0x5A; } // kVK_F20
    if ([key isEqualToString:@"F5"]) { return 0x60; } // kVK_F5
    if ([key isEqualToString:@"F6"]) { return 0x61; } // kVK_F6
    if ([key isEqualToString:@"F7"]) { return 0x62; } // kVK_F7
    if ([key isEqualToString:@"F3"]) { return 0x63; } // kVK_F3
    if ([key isEqualToString:@"F8"]) { return 0x64; } // kVK_F8
    if ([key isEqualToString:@"F9"]) { return 0x65; } // kVK_F9
    if ([key isEqualToString:@"F11"]) { return 0x67; } // kVK_F11
    if ([key isEqualToString:@"F13"]) { return 0x69; } // kVK_F13
    if ([key isEqualToString:@"F16"]) { return 0x6A; } // kVK_F16
    if ([key isEqualToString:@"F14"]) { return 0x6B; } // kVK_F14
    if ([key isEqualToString:@"F10"]) { return 0x6D; } // kVK_F10
    if ([key isEqualToString:@"F12"]) { return 0x6F; } // kVK_F12
    if ([key isEqualToString:@"F15"]) { return 0x71; } // kVK_F15
    if ([key isEqualToString:@"HELP"]) { return 0x72; } // kVK_Help
    if ([key isEqualToString:@"HOME"]) { return 0x73; } // kVK_Home
    if ([key isEqualToString:@"PAGE_UP"]) { return 0x74; } // kVK_PageUp
    if ([key isEqualToString:@"F4"]) { return 0x76; } // kVK_F4
    if ([key isEqualToString:@"END"]) { return 0x77; } // kVK_End
    if ([key isEqualToString:@"F2"]) { return 0x78; } // kVK_F2
    if ([key isEqualToString:@"PAGE_DOWN"]) { return 0x79; } // kVK_PageDown
    if ([key isEqualToString:@"F1"]) { return 0x7A; } // kVK_F1
    if ([key isEqualToString:@"LEFT"]) { return 0x7B; } // kVK_LeftArrow
    if ([key isEqualToString:@"RIGHT"]) { return 0x7C; } // kVK_RightArrow
    if ([key isEqualToString:@"DOWN"]) { return 0x7D; } // kVK_DownArrow
    if ([key isEqualToString:@"UP"]) { return 0x7E; } // kVK_UpArrow
    //if ([key isEqualToString:@"TODO"]) { return 0x75; } // kVK_ForwardDelete
    //if ([key isEqualToString:@"TODO"]) { return 0x3C; } // kVK_RightShift
    //if ([key isEqualToString:@"TODO"]) { return 0x3D; } // kVK_RightOption
    //if ([key isEqualToString:@"TODO"]) { return 0x3E; } // kVK_RightControl
    
    // Key is not in the constant list, try to find it dynamically
    UInt32 keysDown = 0;
    UniChar chars[10];
    UniCharCount realLength;
    
    int totalVariableKeys = sizeof(variableKeys) / sizeof(variableKeys[0]);
    
    for (int i = 0; i<totalVariableKeys; i++) {
        UCKeyTranslate(keyboardLayout,
                       variableKeys[i],
                       kUCKeyActionDisplay,
                       0,
                       keyboardType,
                       kUCKeyTranslateNoDeadKeysBit,
                       &keysDown,
                       sizeof(chars) / sizeof(chars[0]),
                       &realLength,
                       chars);
        
        // Convert the output chars to a NSString
        CFStringRef stringRef = CFStringCreateWithCharacters(kCFAllocatorDefault, chars, 1);
        NSString * unicodeKey = (NSString *)CFBridgingRelease(stringRef);
        
        // Check if it is the requested one
        if ([unicodeKey isEqualToString:key]) {
            return variableKeys[i];
        }
    }
    
    return -1;
}

/*
 Send the given keyboard shortcut to the system.
 The function is executed on the main thread and the "callback" will be called to
 receive the result. In particular, the first argument of the callback function will be:
 The number of keys if succeeded, -1 if a key wasn't found int the current keyboard layout.
 */
void sendShortcut(const char * keys[], int keyCount, void (*callback)(int)) {
    dispatch_async(dispatch_get_main_queue(), ^(void){
        // Initialize the keyboard layout
        TISInputSourceRef currentKeyboard = TISCopyCurrentKeyboardInputSource();
        CFDataRef layoutData = TISGetInputSourceProperty(currentKeyboard, kTISPropertyUnicodeKeyLayoutData);
        const UCKeyboardLayout *keyboardLayout = (const UCKeyboardLayout *)CFDataGetBytePtr(layoutData);
        uint8 keyboardType = LMGetKbdType();
        
        // Get all the virtual keys for the shortcut
        NSMutableArray * virtualKeys = [[NSMutableArray alloc] init];
        bool isValid = true;
        
        for (int i = 0; i<keyCount; i++) {
            NSString *currentKeyString = [NSString stringWithUTF8String:keys[i]];
            int currentVirtualKey = decodeVirtualKey(currentKeyString, keyboardLayout, keyboardType);
            
            if (currentVirtualKey != -1) {
                [virtualKeys addObject:[NSNumber numberWithInt:currentVirtualKey]];
            }else{
                isValid = false;
                break;
            }
        }
        
        CFRelease(currentKeyboard);
        
        if (!isValid) {
            if (callback) {
                callback(-1);
            }
            return;
        }
        
        // Send the key events
        
        // Send all the key presses
        for (id currentVirtualKey in virtualKeys) {
            int virtualKey = [currentVirtualKey intValue];
            CGEventRef keydown;
            keydown = CGEventCreateKeyboardEvent (NULL, (CGKeyCode)virtualKey, true);
            CGEventPost(kCGHIDEventTap, keydown);
            CFRelease(keydown);
            
            usleep(20000);
        }
        
        // Send all the key releases ( in reverse order )
        for (id currentVirtualKey in [virtualKeys reverseObjectEnumerator]) {
            int virtualKey = [currentVirtualKey intValue];
            CGEventRef keyup;
            keyup = CGEventCreateKeyboardEvent (NULL, (CGKeyCode)virtualKey, false);
            CGEventPost(kCGHIDEventTap, keyup);
            CFRelease(keyup);
            
            usleep(20000);
        }
        
        if (callback) {
            callback((int) [virtualKeys count]);
        }
    });
}

/*
 Transform the given key to the original key, removing the effects of the given modifier
 keys. The result is returned using the given callback.
 */
void removeModifiersFromKey(const char * key, int control, int alt, int shift, int command,
                            void (*callback)(const char * key)) {
    // Dispatch the command in the app main thread, because it's forbidden to obtain the current
    // layout source outside of it
    dispatch_async(dispatch_get_main_queue(), ^(void){
        NSString *keyString = [NSString stringWithUTF8String:key];
        
        // Initialize the keyboard layout
        TISInputSourceRef currentKeyboard = TISCopyCurrentKeyboardInputSource();
        CFDataRef layoutData = TISGetInputSourceProperty(currentKeyboard, kTISPropertyUnicodeKeyLayoutData);
        const UCKeyboardLayout *keyboardLayout = (const UCKeyboardLayout *)CFDataGetBytePtr(layoutData);
        uint8 keyboardType = LMGetKbdType();
        
        UInt32 keysDown = 0;
        UniChar chars[10];
        UniCharCount realLength;
        
        // Create the modifier keys mask
        UInt32 modifiers = 0;
        if (control) {
            modifiers |= (1 << 4);
        }
        if (shift) {
            modifiers |= (1 << 1);
        }
        if (alt) {
            modifiers |= (1 << 3);
        }
        if (command) {
            modifiers |= (1 << 0);
        }
        
        int totalVariableKeys = sizeof(variableKeys) / sizeof(variableKeys[0]);
        int resultKey = -1;
        
        // Cycle through all keys to find the one that produce the given string with the
        // given modifier keys
        for (int i = 0; i<totalVariableKeys; i++) {
            UCKeyTranslate(keyboardLayout,
                           variableKeys[i],
                           kUCKeyActionDisplay,
                           modifiers,
                           keyboardType,
                           kUCKeyTranslateNoDeadKeysBit,
                           &keysDown,
                           sizeof(chars) / sizeof(chars[0]),
                           &realLength,
                           chars);
            
            // Convert the output chars to a NSString
            CFStringRef stringRef = CFStringCreateWithCharacters(kCFAllocatorDefault, chars, 1);
            NSString * unicodeKey = (NSString *)CFBridgingRelease(stringRef);
            
            // Check if it is the requested one
            if ([unicodeKey isEqualToString:keyString]) {
                resultKey = variableKeys[i];
                break;
            }
        }
        
        // If the result key was found, extract the resulting key without modifiers
        if (resultKey >= 0) {
            UCKeyTranslate(keyboardLayout,
                           resultKey,
                           kUCKeyActionDisplay,
                           0,
                           keyboardType,
                           kUCKeyTranslateNoDeadKeysBit,
                           &keysDown,
                           sizeof(chars) / sizeof(chars[0]),
                           &realLength,
                           chars);
            
            // Convert the output chars to a NSString
            CFStringRef stringRef = CFStringCreateWithCharacters(kCFAllocatorDefault, chars, 1);
            NSString * unicodeKey = (NSString *)CFBridgingRelease(stringRef);
            
            if (callback) {
                const char * result = [unicodeKey UTF8String];
                callback(result);
            }
        }
        
        CFRelease(currentKeyboard);
    });
}

/*
 Disable the CAPS LOCK.
 Return 0 if correctly disabled, -1 if an error occurred
 */
int forceDisableCapsLock() {
    kern_return_t kr;
    io_service_t ios;
    io_connect_t ioc;
    CFMutableDictionaryRef mdict;
    mdict = IOServiceMatching(kIOHIDSystemClass);
    ios = IOServiceGetMatchingService(kIOMasterPortDefault, (CFDictionaryRef) mdict);
    if (!ios) {
        if (mdict) CFRelease(mdict);
        return -1;
    }
    kr = IOServiceOpen(ios, mach_task_self(), kIOHIDParamConnectType, &ioc);
    IOObjectRelease(ios);
    if (kr != KERN_SUCCESS) {
        return -1;
    }
    kr = IOHIDSetModifierLockState(ioc, kIOHIDCapsLockState, 0);
    if (kr != KERN_SUCCESS) {
        IOServiceClose(ioc);
        return -1;
    }
    
    IOServiceClose(ioc);
    
    return 0;
}

void _postMediaKey(int mediaKey, int isDown) {
    unsigned int modifierFlags = (isDown == 1) ? 0xa00 : 0xb00;
    unsigned int data = mediaKey << 16 | ((isDown == 1) ? 0xa : 0xb) << 8;
    NSEvent *event = [NSEvent otherEventWithType:NSEventTypeSystemDefined
                                        location:NSMakePoint(0, 0)
                                   modifierFlags:modifierFlags
                                       timestamp:0
                                    windowNumber:0
                                         context:0
                                         subtype:8
                                           data1:data
                                           data2:-1];
    CGEventRef cgEvent = [event CGEvent];
    CGEventPost(0, cgEvent);
}

/*
 Simulate the pressure of the given media key.
 */
void simulateMediaKey(int mediaKey) {
    _postMediaKey(mediaKey, 1);
    usleep(20000);
    _postMediaKey(mediaKey, 0);
}

/*
 Check if accessibility permissions have been granted to dokey.
 Return 0 if not authorized, 1 if authorized.
 */
int isAccessibilityEnabled() {
    if (AXIsProcessTrusted()) {
        return 1;
    }else{
        return 0;
    }
}

/*
 If Dokey does not have accessibility permissions, prompt the user with
 a dialog to enable them
 */
void showAccessibilityPrompt() {
    NSDictionary *options = @{(__bridge id)kAXTrustedCheckOptionPrompt: @YES};
    AXIsProcessTrustedWithOptions((CFDictionaryRef)options);
}
