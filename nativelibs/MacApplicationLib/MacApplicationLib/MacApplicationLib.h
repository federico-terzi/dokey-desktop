//
//  MacApplicationLib.h
//  MacApplicationLib
//
//  Created by FreddyT on 13/11/2018.
//  Copyright © 2018 Federico Terzi. All rights reserved.
//

#import <Cocoa/Cocoa.h>

/*
 Focus the Dokey window
 */
int focusDokey(void);

/*
 Extract the icon of the specified app bundle to the given targetFile,
 using the PNG format.
*/
int extractApplicationIcon(const char * appPath, const char * targetFile);

/*
 Get the bundle path of the currently active application, copying it to the
 given pathBuffer.
 */
void getActiveApplication(char * pathBuffer, int bufferSize);

/*
 Get the bundle path of the currently active applications.
 For each active application, the callback will be invoked
 */
void getActiveApplications(void (*callback)(const char * appPath));

/*
 Return the PID of the currently active application.
 */
int getActivePID(void);

/*
 Activate ( focus ) the given application.
 If succeeded, return 1.
 If the application is not running, nothing will occur and the method will return 0.
 */
int activateRunningApplication(const char * appPath);
