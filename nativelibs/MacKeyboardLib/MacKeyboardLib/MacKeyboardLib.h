//
//  MacKeyboardLib.h
//  MacKeyboardLib
//
//  Created by Federico Terzi on 18/10/2018.
//  Copyright © 2018 Federico Terzi. All rights reserved.
//

#include <CoreFoundation/CoreFoundation.h>
#include <Carbon/Carbon.h> /* For kVK_ constants, and TIS functions. */

/*
 Search for the virtual key code of the given key using the current keyboard layout.
 Return the cirtual key code if found, -1 otherwise.
 */
int decodeVirtualKey(NSString *key, const UCKeyboardLayout *keyboardLayout,
                     uint8 keyboardType);

/*
 Transform the given key to the original key, removing the effects of the given modifier
 keys. The result is returned using the given callback.
 */
void removeModifiersFromKey(const char * key, int control, int alt, int shift, int command,
                           void (*callback)(const char * key));

/*
 Send the given keyboard shortcut to the system.
 The function is executed on the main thread and the "callback" will be called to
 receive the result. In particular, the first argument of the callback function will be:
 The number of keys if succeeded, -1 if a key wasn't found int the current keyboard layout.
 */
void sendShortcut(const char * keys[], int keyCount, void (*callback)(int));

/*
 Disable the CAPS LOCK.
 Return 0 if correctly disabled, -1 if an error occurred
 */
int forceDisableCapsLock(void);
