//
//  main.m
//  Test
//
//  Created by Federico on 04/03/2019.
//  Copyright © 2019 Federico Terzi. All rights reserved.
//

#import "MACPhotoshopBindings.h"

int main(int argc, const char * argv[]) {
    double a[] = {20.0};
    executeJavascript("alert(arguments[0]);", a, 1);
    return 0;
}
