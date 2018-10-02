//
//  JavaMacNativeUI.h
//  JavaMacNativeUI
//
//  Created by FreddyT on 14/09/2018.
//  Copyright © 2018 Federico Terzi. All rights reserved.
//

#import <AppKit/AppKit.h>

@interface JavaMacNativeUI : NSObject

+ (void) displayDialogInternal:(NSString *)imagePath
                         title:(NSString *)title
                   description:(NSString* )description
                       buttons:(NSArray *)buttons
                    isCritical:(BOOL)isCritical
                      callback:(void (^)(int number))callback;

@end

// Native method
void displayDialog(char* imagePath, char* title, char* description, char *buttons[],
                   int buttonsCount, int isCritical, void (*callback)(int));

/*
 Status ICON methods
 */

extern NSStatusItem *statusItem;
extern void (*statusItemClickCallback)(void);

void initializeStatusItem(void);
void setStatusItemImage(char *imagePath);
void setStatusItemTooltip(char *tooltip);
void setStatusItemHighlighted(int highlighted);
void setStatusItemAction(void (*callback)(void));
